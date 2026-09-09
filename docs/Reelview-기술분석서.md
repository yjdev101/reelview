# ReelView 기술분석서

새로운 기술/도구를 도입할 때마다, 왜 필요했는지 · 어떤 대안을 검토했는지 · 왜 이걸 골랐는지 · 어떻게 적용했는지를 기록하는 누적 문서. 항목은 도입 순서대로 아래에 추가된다.

---

## 2026-09-07 · 모니터링 & 부하테스트 (Actuator + Prometheus + Grafana, k6)

### 배경 / 문제 정의
지금까지는 기능 구현과 버그 수정 위주로 진행해서, API가 실제로 얼마나 호출되는지, 응답 시간이 어떤지, 에러율이 얼마인지, 동시 요청이 몰렸을 때 얼마나 버티는지 **수치로 확인한 적이 없다.** 같은 날 진행한 코드리뷰에서 발견한 이슈들(예: `JwtAuthenticationFilter`가 요청마다 JWT를 3번 중복 파싱하는 문제)도 "이론적으로 비효율적이다"까지는 확인했지만, 실제 부하 상황에서 얼마나 영향을 주는지는 모른다. 모니터링과 부하테스트를 붙이면 이런 질문에 근거 있는 답을 할 수 있다.

### 1. 모니터링

**후보 비교**

| 후보 | 장점 | 단점 |
|---|---|---|
| Actuator만 사용 | 설정 간단, 추가 의존성/인프라 없음 | 시계열 저장이 안 됨 — 지금 값만 보임, 과거 추이 파악 불가, 그래프 없음 |
| **Actuator + Prometheus + Grafana** | Spring Boot 표준 계측(Micrometer)과 통합 쉬움, 업계에서 가장 널리 쓰이는 오픈소스 모니터링 조합, Grafana로 시계열 대시보드 구성 가능, k6 부하테스트 결과도 같은 화면에서 연계 가능 | Prometheus/Grafana를 별도 인프라(Docker 등)로 띄워야 함, 설정에 러닝커브 있음 |
| ELK(Elasticsearch+Logstash+Kibana) | 로그 분석에 강점 | 이번 목적(수치 지표 중심 모니터링)과는 결이 다르고, 인프라가 더 무거움 |
| AWS CloudWatch | EC2에 이미 배포돼 있어 자연스러운 선택지, 별도 인프라 불필요 | 애플리케이션 레벨 세부 메트릭을 보려면 추가 설정 필요, 커스텀 메트릭에 비용 발생, 오픈소스 스택만큼 학습/이력서 가치가 크지 않음 |

**선택: Actuator + Prometheus + Grafana** — Micrometer가 Spring Boot 표준 계측 라이브러리라 의존성만 추가하면 바로 지표가 나오고, k6 결과를 Prometheus로 내보내면 모니터링 지표와 부하테스트 결과를 같은 Grafana 대시보드에서 동시에 확인할 수 있다는 게 핵심 이유.

**적용 설계 (계획 — 아직 미구현)**
1. `build.gradle`에 `spring-boot-starter-actuator`, `micrometer-registry-prometheus` 의존성 추가
2. `application.yml`에 `management.endpoints.web.exposure.include`로 `health`, `prometheus` 등 필요한 엔드포인트만 노출
3. `SecurityConfig`에 `/actuator/**` 인가 규칙 추가 — 오늘 고친 `/admin/**` 패턴처럼, 내부 지표 엔드포인트를 인증 없이 그대로 열어두면 안 됨
4. Docker Compose로 Prometheus + Grafana 실행 (로컬 우선)
5. `prometheus.yml`에서 `/actuator/prometheus`를 스크레이핑 대상으로 등록
6. Grafana에 Prometheus를 데이터소스로 연결하고, JVM 메모리/HTTP 요청 수·응답시간 기본 대시보드 구성

**리스크 / 트레이드오프**
- `/actuator/prometheus`는 내부 시스템 지표를 그대로 노출하는 엔드포인트라, 인가 규칙을 빠뜨리면 오늘 발견했던 `/admin/**` 이슈와 같은 종류의 보안 구멍이 됨 — 설계 단계부터 반영 필요
- Docker 인프라가 하나 늘어나 로컬 개발 환경이 복잡해짐
- EC2 프리티어 인스턴스에 Prometheus+Grafana까지 같이 올리기엔 메모리가 부족할 수 있음 — 로컬 전용으로 할지, EC2에도 배포할지는 별도 결정 필요

### 2. 부하테스트

**후보 비교**

| 후보 | 장점 | 단점 |
|---|---|---|
| Apache JMeter | 업계에서 오래 쓰인 도구, GUI로 시나리오 구성 | 스크립트가 XML이라 git 버전관리 지저분함, GUI 학습곡선, 상대적으로 무거움 |
| **k6** | 시나리오를 JS 코드로 작성해 git 친화적, 가볍고 CLI 중심, Prometheus/Grafana 연동 지원이 잘 되어 있음, 최근 채택이 느는 추세 | JS로 로그인→토큰 획득 같은 멀티스텝 흐름을 직접 코딩해야 함 |
| Gatling | 성능이 뛰어남 | Scala DSL을 새로 배워야 해서 학습 비용이 큼 |
| wrk / Apache Bench | 아주 가볍고 간단 | 로그인 → 토큰 획득 → 인증 필요한 API 호출 같은 멀티스텝 시나리오 표현이 어려움. ReelView는 JWT 인증이 필수 흐름이라 그대로 쓰기 부적합 |

**선택: k6** — ReelView의 대표 사용자 흐름(로그인 → 토큰 획득 → 인증 필요한 API 호출)을 JS 스크립트로 자연스럽게 표현할 수 있고, 결과를 Prometheus로 remote-write하면 부하를 주는 동안 JVM 힙 사용률·응답시간 변화를 모니터링 대시보드에서 같이 볼 수 있음. 스크립트가 코드라 git으로 버전관리·리뷰 가능.

**적용 설계 (계획 — 아직 미구현)**
1. k6 설치 (바이너리 또는 Docker 이미지)
2. 로그인 → 토큰 획득 → 인증 필요한 API(예: 리뷰 목록 조회, 평점 등록) 호출 시나리오를 `.js` 스크립트로 작성
3. **로컬 서버 대상으로 먼저** 낮은 동시 사용자 수부터 시작해서 점진적으로 늘려가며(ramping) 테스트
4. EC2(운영 배포 서버) 대상 테스트는 별도 승인 후 진행
5. k6 결과는 Prometheus remote-write 또는 k6 자체 웹 대시보드로 확인

**리스크 / 트레이드오프**
- EC2 프리티어 인스턴스에 강한 부하를 주면 실제 배포된 서비스가 느려지거나 다운될 수 있음. 반드시 로컬에서 먼저 검증하고, EC2 테스트는 트래픽이 적은 시간대에 낮은 강도로 시작해야 함
- k6 자체 JS 문법을 새로 익혀야 함 (간단하지만 학습 비용 존재)
- 부하테스트는 수치를 뽑는 것 자체가 목적이 아니라 병목을 찾고 개선하는 데까지 이어져야 의미가 있음 — 오늘 발견한 JWT 3중 파싱 이슈가 실제 부하 상황에서 얼마나 영향을 주는지 확인하는 용도로 연결할 예정

**상태 (2026-09-09 갱신)**: 모니터링 적용 설계 1~3번 완료. 3번은 애초 계획했던 `hasRole("ADMIN")` 대신, `/actuator/**`를 `@Order(1)` 전용 `SecurityFilterChain`으로 분리해 `httpBasic()` + 별도 `monitoring` 계정(`InMemoryUserDetailsManager`)으로 인증하는 방식으로 변경 — Prometheus 스크레이핑 트레이드오프(아래)를 4단계까지 안 미루고 여기서 바로 해결.

이 전용 체인에는 메인 체인의 `jwtAuthenticationFilter`가 적용되지 않으므로(필터는 체인별로 등록됨), 기존 로그인 JWT로는 ADMIN 계정이라도 `/actuator/**`에 접근 불가 — `monitoring` 계정의 Basic Auth로만 접근 가능하도록 완전히 격리됨. 로컬 검증 완료:

| 시나리오 | 결과 |
|---|---|
| 인증 없음 | 401 |
| `monitoring` 계정 Basic Auth | 200, `{"status":"UP"}` |
| 기존 ADMIN JWT | 401 (이 체인엔 JWT 필터가 없어서) |

다음 단계: 4번(Docker Compose로 Prometheus + Grafana 실행)부터 이어서 진행. Prometheus가 `/actuator/prometheus`를 스크레이핑할 땐 `prometheus.yml`의 `basic_auth` 설정으로 `monitoring` 계정 자격증명을 넣어주면 됨.

---
