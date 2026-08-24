# CLAUDE.md — ReelView Backend

영화/드라마/애니메이션 **리뷰 영상**을 모아보는 플랫폼의 백엔드. Spring Boot 4.1.0, Java 21, MySQL, Spring Data JPA, Gradle. 취업 대비 학습 목적 프로젝트.

## 핵심 도메인 개념
평점은 **작품(Content)이 아니라 리뷰 영상(Review)에 대한 평가**다. 작품 자체 평점은 다른 서비스에 이미 많아서 의도적으로 다루지 않는다 — "양질의 리뷰 영상을 모아서 보고 그 리뷰를 평가"하는 게 서비스 본질. 그래서 관계가 `Content → Review → ReviewRating` 2단계이고, 평점 관련 쿼리(평균, 정렬 등)는 항상 이 조인을 거친다.

## 실행 방법
- `src/main/resources/application.yml.example`을 복사해 `application.yml`로 만들고 DB 비밀번호/JWT secret/TMDB API key를 채운다. `application.yml`은 gitignore 처리되어 있음 — 절대 커밋하지 않는다.
- `./gradlew bootRun`으로 실행 (포트 8080).
- Swagger UI: `http://localhost:8080/swagger-ui/index.html` — 로그인 후 받은 토큰을 Authorize에 넣으면 인증 필요한 API도 테스트 가능.
- 테스트 계정: `testuser_0727` / `testpass1` (ADMIN 권한)

## 아키텍처
- Controller → Service → Repository 계층 구조. "MVC"라고 부르지 않는다 (View가 없는 API 서버).
- Service가 다른 Service를 의존하는 것은 허용 — 단, **방향이 항상 단방향이고 순환 의존이 없어야** 한다 (예: `ReviewService`가 `ContentService`를 참조, `TmdbImportService`가 `TmdbClient`+`ContentService`를 참조).
- 외부 API 연동은 별도 패키지로 분리한다 (`client.tmdb` 등) — 외부 시스템의 데이터 모델을 우리 도메인에 그대로 들이지 않고, 우리 기준으로 변환해서 받아들인다(anti-corruption layer). TMDB 장르 → 우리 genre 테이블 매핑이 그 예.

## 컨벤션
- DI가 필요한 클래스(Controller/Service 등)는 Lombok 생성자 어노테이션 대신 **수동 생성자**를 작성한다. DTO는 예외 — Lombok `@Getter`/`@Setter`/`@AllArgsConstructor` 사용.
- 단건 조회(PK 기준)는 없으면 에러(`orElseThrow()`) — 존재가 전제된 조회이기 때문. 목록/검색 조회는 없으면 빈 리스트가 정상이며 에러가 아니다 (다른 시맨틱).
- `GlobalExceptionHandler`가 처리하는 예외: `IllegalArgumentException`→400(항상 의미있는 메시지를 담아서 던질 것), `NoSuchElementException`→404, `JwtException`→401(라이브러리 원본 메시지 노출 안 함, 고정 문자열 사용).
- 새 필터링/정렬/파생 쿼리를 추가할 때, Spring Data 파생 쿼리 메서드로 표현 안 되면(집계함수, 다단계 조인 등) `@Query`로 JPQL을 직접 쓴다.

## 작업 방식 (Claude와 함께 작업할 때)
- **새로운 개념/핵심 로직**(예: JPQL 집계 쿼리, JWT 흐름, 예외 처리 설계)은 설명 → 사용자가 직접 작성 → 리뷰 순서로 진행한다. 면접에서 본인이 직접 설명할 수 있어야 하는 게 목적.
- **반복되는 패턴**(이미 이 프로젝트에서 여러 번 써본 CRUD/필터링/정렬 wiring, 컨트롤러 boilerplate)은 Claude가 바로 작성해도 된다.
- 이 구분은 2026년 8월 말 마감 압박 때문에 도입된 한시적 조정이다. 마감 이후엔 전체를 다시 설명→직접작성→리뷰 방식으로 되돌릴 수 있다.
