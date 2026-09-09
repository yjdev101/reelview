# ReelView 아키텍처 설명서

## 1. 계층 구조

Controller → Service → Repository의 3계층 구조입니다. "MVC"라고 부르지 않는 이유는 View가 없는 순수 API 서버이기 때문입니다.

```
Controller   HTTP 요청/응답, DTO 변환만 담당. 도메인 로직 없음
   ↓
Service      도메인 로직, 트랜잭션 경계. 다른 Service를 참조할 수 있음
   ↓
Repository   Spring Data JPA. 파생 쿼리 메서드 또는 @Query(JPQL)
```

Service 간 의존은 허용하지만 **항상 단방향**입니다. 예를 들어 `ReviewService`는 `ContentService`를 참조하지만 반대로는 참조하지 않습니다. 순환 의존이 생기지 않도록 의도적으로 설계했습니다.

```
ReviewService ──▶ ContentService
CommentSummaryService ──▶ (ReviewRepository, YoutubeClient, AnthropicClient)
TmdbImportService ──▶ (TmdbClient, ContentService)
```

## 2. 핵심 도메인 모델: 2단계 평점 구조

ReelView의 평점은 **작품(Content)이 아니라 리뷰 영상(Review)에 대한 평가**입니다. 작품 자체 평점은 왓챠피디아 등 이미 잘하는 서비스가 많아서 의도적으로 다루지 않습니다. "양질의 리뷰 영상을 모아서 보고 그 리뷰를 평가"하는 것이 서비스의 본질입니다.

```
Content(작품) ──1:N──▶ Review(리뷰 영상) ──1:N──▶ ReviewRating(리뷰에 대한 평점)
```

이 구조 때문에 평점 관련 쿼리(평균, 인기순 정렬 등)는 항상 `Review ⋈ ReviewRating` 조인을 거칩니다. 예를 들어 "평점 높은 리뷰 목록"은 Content 테이블을 보지 않고 Review-ReviewRating을 집계해서 계산합니다. 자세한 테이블 관계는 [ERD.md](../ERD.md) 참고.

## 3. 외부 연동: Anti-Corruption Layer

외부 API 연동은 `client` 패키지 하위에 API별로 분리되어 있습니다 (`client.tmdb`, `client.youtube`, `client.llm`). 외부 시스템의 데이터 모델을 우리 도메인에 그대로 들이지 않고, 우리 기준으로 변환해서 받아들입니다.

```
client/
  tmdb/     TmdbClient(HTTP 호출) → TmdbMovieDto(TMDB 응답 그대로)
            → TmdbGenreMapper(TMDB 장르 ID → 우리 Genre 매핑)
            → TmdbImageMapper(TMDB 이미지 경로 → 우리가 쓰는 포스터 URL)
            → TmdbImportService(우리 Content 엔티티로 변환해서 저장)
  youtube/  YoutubeClient → YoutubeVideoCandidate / 댓글 리스트
  llm/      AnthropicClient → 댓글 리스트를 받아 요약 문자열 반환
```

예: TMDB는 장르를 자체 ID(`genre_ids: [28, 12]`)로 내려주는데, 이걸 그대로 저장하면 TMDB가 장르 체계를 바꾸는 순간 우리 DB가 깨집니다. `TmdbGenreMapper`가 이 변환을 전담해서, TMDB의 변경이 우리 `Genre` 테이블 구조에 영향을 주지 않도록 격리합니다.

**리뷰 댓글 요약 흐름** (`CommentSummaryService`):
```
YoutubeClient.extractVideoId(videoUrl)
  → YoutubeClient.getTopComments(videoId, 20)   # YouTube Data API
  → AnthropicClient.summarize(comments)         # Claude API로 2~3문장 요약
  → Review.commentSummary에 캐싱 (재요청 시 API 재호출 없이 반환)
```

## 4. 보안: 두 개의 독립된 SecurityFilterChain

`SecurityConfig`는 `@Order`로 우선순위가 다른 두 체인을 등록합니다.

| 체인 | 대상 경로 | 인증 방식 | 용도 |
|---|---|---|---|
| `actuatorSecurityFilterChain` (`@Order(1)`) | `/actuator/**` | HTTP Basic (`monitoring` 계정, `InMemoryUserDetailsManager`) | Prometheus 스크레이핑 전용 |
| `securityFilterChain` (기본) | 나머지 전체 | JWT (`JwtAuthenticationFilter`) | 일반 API 인증 |

두 체인은 완전히 격리되어 있습니다. `/actuator/**`에는 메인 체인의 `JwtAuthenticationFilter`가 적용되지 않으므로, ADMIN 권한의 로그인 JWT로도 접근할 수 없고 오직 `monitoring` 계정의 Basic Auth로만 접근 가능합니다. 반대로 `monitoring` 계정은 `/actuator/**` 외 다른 API에 대한 권한이 없습니다. 이렇게 분리한 이유는 내부 시스템 지표(`/actuator/prometheus` 등)를 일반 사용자 인증 체계와 섞지 않기 위함입니다.

```
요청 → /actuator/health
        └─ actuatorSecurityFilterChain 매칭 (@Order(1)이 먼저 평가됨)
             └─ Basic Auth 검증 (monitoring 계정) → 통과 시 hasRole("MONITORING") 확인

요청 → /reviews
        └─ actuatorSecurityFilterChain 매칭 안 됨 → 기본 체인으로
             └─ JwtAuthenticationFilter가 토큰 파싱 → SecurityContext에 인증 정보 설정
```

인가 규칙 중 작품 등록/수정/삭제(`POST/PUT/DELETE /contents/**`)와 `/admin/**`(TMDB 임포트, YouTube 검색)는 `hasRole("ADMIN")`으로 제한되어 있습니다. 리뷰·평점 작성은 일반 사용자도 가능합니다 (UGC).

예외 처리는 `GlobalExceptionHandler`가 전담합니다: `IllegalArgumentException`→400, `NoSuchElementException`→404, `JwtException`→401(라이브러리 원본 메시지 대신 고정 문자열 노출). 단건 조회(PK 기준)는 존재가 전제되므로 `orElseThrow()`로 없으면 에러, 목록/검색 조회는 없으면 빈 리스트가 정상입니다.

## 5. 모니터링

```
Spring Boot App (Micrometer 계측)
   → /actuator/prometheus 노출
        → Prometheus 컨테이너가 15초 주기로 스크레이핑 (Basic Auth)
             → Grafana가 Prometheus를 데이터소스로 조회/시각화
```

`docker-compose.yml`로 Prometheus + Grafana를 로컬에 띄우고, `monitoring/prometheus/prometheus.yml`(gitignore 처리, `.example` 파일로 대체)에 스크레이핑 대상과 인증 정보를 설정합니다. 도입 배경과 대안 비교는 [Reelview-기술분석서.md](Reelview-기술분석서.md) 참고.
