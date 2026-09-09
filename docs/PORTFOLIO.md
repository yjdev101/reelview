# ReelView — 포트폴리오 샘플

> 이 문서는 포트폴리오/이력서에 프로젝트를 소개할 때 쓸 수 있는 요약본입니다. 실무 지원 시 이 내용을 바탕으로 자기소개서/면접 답변을 구성할 수 있습니다.

## 한 줄 소개

영화·드라마·애니메이션 **리뷰 영상**을 작품별로 모아보고, 리뷰 영상 자체를 평가하는 버티컬 서비스의 백엔드 API 서버.

## 문제의식

왓챠피디아, 레터박스처럼 "작품 자체"에 평점을 매기는 서비스는 이미 많다. 반면 유튜브 등에 흩어진 "좋은 리뷰 영상을 찾는 것" 자체는 여전히 검색에 의존해야 한다. ReelView는 리뷰 영상을 작품 단위로 큐레이션하고, **리뷰 영상의 품질**을 사용자들이 평가하도록 해서 이 문제를 풀려는 시도다. 이 문제의식이 도메인 모델 전체를 결정한다 — `Content → Review → ReviewRating`의 2단계 구조, 평점 관련 쿼리가 항상 Review-ReviewRating 조인을 거치는 이유가 여기서 나온다.

## 핵심 기술적 의사결정

### 1. 2단계 평점 도메인 모델
작품이 아닌 리뷰 영상에 평점을 매기는 구조를 선택하면서, 평균 평점·인기순 정렬 같은 흔한 쿼리도 Content 테이블만 보고 끝낼 수 없고 항상 `Review ⋈ ReviewRating` 조인이 필요해졌다. Spring Data 파생 쿼리로 표현이 안 되는 다단계 조인·집계는 `@Query`로 JPQL을 직접 작성해서 해결했다. → [ARCHITECTURE.md](ARCHITECTURE.md) 2절

### 2. 외부 API에 대한 Anti-Corruption Layer
TMDB 장르 체계, YouTube 응답 구조를 우리 도메인에 그대로 들이지 않고 `client.tmdb` / `client.youtube` / `client.llm` 패키지에서 우리 기준으로 변환한다. TMDB가 장르 ID 체계를 바꿔도 `Genre` 테이블 구조는 영향받지 않는다. → [ARCHITECTURE.md](ARCHITECTURE.md) 3절

### 3. 보안 필터 체인 분리 (내부 지표 vs 사용자 API)
초기에는 `/actuator/**`를 `hasRole("ADMIN")`으로 열려고 했으나, 이렇게 하면 일반 로그인 JWT 인증 체계와 내부 시스템 지표 접근이 같은 신뢰 경계를 공유하게 된다. 대신 `@Order(1)`로 `/actuator/**` 전용 `SecurityFilterChain`을 분리해 별도 Basic Auth 계정(`monitoring`)으로 완전히 격리했다. 메인 체인의 JWT 필터는 이 체인에 적용되지 않아, ADMIN 권한 JWT로도 접근이 불가능하다. → [ARCHITECTURE.md](ARCHITECTURE.md) 4절

### 4. 모니터링 스택 도입 (Actuator + Prometheus + Grafana)
"기능은 동작하지만 실제로 얼마나 호출되고 얼마나 버티는지 수치로 확인한 적이 없다"는 문제를 인식하고 도입. JMeter/Gatling/wrk 대비 k6를 부하테스트 도구로 검토 중이며, Prometheus/Grafana를 먼저 붙여서 부하테스트 결과를 같은 대시보드에서 관찰할 수 있는 기반을 만들었다. 대안 비교와 트레이드오프는 [Reelview-기술분석서.md](Reelview-기술분석서.md)에 의사결정 과정을 그대로 남겼다.

## 트러블슈팅 사례

코드 리뷰 과정에서 발견하고 수정한 이슈들:
- 관리자 API 인가 누락 — 특정 관리자 전용 엔드포인트에 권한 검사가 빠져있던 것을 발견하고 수정
- 파일 업로드 경로 순회(path traversal) 취약점 — 사용자가 지정한 파일명을 그대로 저장 경로에 사용하던 부분을 검증 로직 추가로 차단
- JWT 필터 NPE — 특정 조건에서 토큰 파싱 시 발생하던 널 포인터 예외 수정
- 평점 검증 누락, Content 삭제 시 FK 제약 위반 — 리뷰가 달린 작품을 삭제하려 할 때 발생하는 무결성 위반을 사전에 막도록 수정

## 사용 기술과 선택 이유

| 기술 | 왜 |
|---|---|
| Spring Boot / Spring Data JPA | 계층형 아키텍처와 도메인 모델을 명확히 분리해서 설계 연습을 하기 좋음 |
| JWT (jjwt) | Stateless 인증 흐름과 Access/Refresh 토큰 재발급 흐름을 직접 구현해보기 위해 |
| MySQL | 관계형 모델(Content-Review-ReviewRating, N:M 장르 매핑)을 다루는 데 적합 |
| Prometheus + Grafana | Spring Boot 표준 계측(Micrometer)과 통합이 쉽고, 업계에서 가장 널리 쓰이는 오픈소스 모니터링 조합 |

## 배운 점

- "평점을 무엇에 매기는가"라는 도메인 결정 하나가 쿼리 설계, 인덱스, API 응답 구조 전체에 영향을 준다는 것을 체감
- 외부 API를 도메인에 직접 노출시키지 않는 anti-corruption layer의 실익 — TMDB 응답 구조가 바뀌어도 우리 엔티티는 그대로인 구조를 직접 만들어봄
- 인증/인가를 "하나의 체인으로 다 처리"하는 것과 "관심사별로 체인을 분리"하는 것의 트레이드오프를 실제로 겪어봄 (특히 내부 시스템 엔드포인트와 사용자 API는 신뢰 경계가 다르다는 것)
