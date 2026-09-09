# ReelView (Backend)

영화·드라마·애니메이션 리뷰 **영상**을 작품별로 모아볼 수 있는 리뷰 플랫폼의 백엔드 API 서버입니다.

기존 텍스트 리뷰 중심 플랫폼(왓챠피디아, 레터박스 등)과 달리, ReelView는 유튜브 등에 흩어진 리뷰 영상을 작품 단위로 큐레이션하고, **리뷰 영상 자체**를 사용자들이 평가하는 데 집중하는 버티컬 서비스를 목표로 합니다. 작품(Content) 자체의 평점은 다루지 않고, `Content → Review → ReviewRating`의 2단계 구조로 "리뷰 영상"을 평가 대상으로 삼는 것이 핵심 도메인 설계입니다.

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.x, Spring Security |
| DB | MySQL, Spring Data JPA |
| 인증 | JWT (jjwt) |
| 외부 연동 | TMDB API(작품 메타데이터 수집), YouTube Data API(리뷰 영상 댓글 조회), Anthropic API(댓글 요약) |
| 모니터링 | Spring Actuator, Micrometer, Prometheus, Grafana |
| API 문서 | springdoc-openapi (Swagger UI) |
| 빌드 도구 | Gradle |

## 구현 현황

- 회원가입 / 로그인 / JWT 발급·재발급 (`/users`)
- 작품(Content) 등록 · 조회 · 수정 · 삭제, 장르(Genre) 필터링 (`/contents`, `/genres`)
- 리뷰(Review) 등록 · 수정 · 삭제, 영상 파일 업로드 또는 외부 URL 등록 방식 지원 (`/reviews`, `/files/videos`)
- 리뷰 평점(ReviewRating) 등록 · 수정 · 삭제 (중복 평가 방지), 평균 평점 조회 (`/reviews/{id}/ratings`)
- YouTube 리뷰 영상의 댓글을 모아 Anthropic API로 요약 (`/reviews/{id}/comment-summary`)
- TMDB API 연동으로 작품 메타데이터 일괄 등록 (`/admin/tmdb`, 관리자 전용)
- 전역 예외 처리(`GlobalExceptionHandler`), Swagger API 문서화
- Actuator + Prometheus + Grafana 기반 모니터링 인프라 (`/actuator/**`는 별도 Basic Auth 계정으로 격리)

## 아키텍처

계층 구조와 도메인 설계, 보안 필터 체인에 대한 자세한 설명은 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)를 참고하세요.
데이터 모델(ERD)은 [ERD.md](ERD.md)를 참고하세요.

## 실행 방법

### 1. 사전 준비
- Java 21
- 로컬 MySQL 서버 (DB `reelview` 생성)
- (선택) Docker — 모니터링 스택(Prometheus/Grafana)을 실행하려는 경우

### 2. 설정 파일 생성
비밀번호 등 민감 정보가 포함된 `application.yml`은 git에 포함되어 있지 않습니다. 예시 파일을 복사해 본인 환경에 맞게 값을 채워주세요.

```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

`application.yml`에서 아래 값을 본인 환경에 맞게 수정합니다.

```yaml
spring:
  datasource:
    username: <본인 MySQL 계정>
    password: <본인 MySQL 비밀번호>

jwt:
  secret: <임의의 문자열, 32자 이상 권장>

tmdb:
  api-key: <TMDB API 키>        # 없으면 /admin/tmdb 기능만 동작 안 함, 나머지 기능엔 영향 없음

youtube:
  api-key: <YouTube Data API 키> # 없으면 댓글 요약 기능만 동작 안 함

anthropic:
  api-key: <Anthropic API 키>    # 없으면 댓글 요약 기능만 동작 안 함

monitoring:
  username: <모니터링용 Basic Auth 계정명>
  password: <모니터링용 Basic Auth 비밀번호>
```

> TMDB / YouTube / Anthropic 키가 없어도 서버는 정상 기동되며, 회원가입·로그인·작품·리뷰·평점 등 핵심 기능은 그대로 테스트할 수 있습니다. 해당 외부 연동 기능만 동작하지 않습니다.

### 3. 서버 실행

```bash
./gradlew bootRun
```

정상 기동되면 `http://localhost:8080` 에서 서비스됩니다.

### 4. API 문서로 테스트하기

```
http://localhost:8080/swagger-ui/index.html
```

1. `/users/login`으로 로그인해서 토큰 발급 (테스트 계정: `testuser_0727` / `testpass1`, ADMIN 권한)
2. Swagger UI 우측 상단 `Authorize`에 `Bearer <발급받은 토큰>` 형식으로 입력
3. 이제 인증이 필요한 API(리뷰 등록, 평점 등록, 관리자 API 등)도 테스트 가능

### 5. (선택) 모니터링 대시보드 실행

Actuator가 노출하는 지표(JVM, HTTP 요청 등)를 Prometheus로 수집하고 Grafana로 시각화하려면:

```bash
cp monitoring/prometheus/prometheus.yml.example monitoring/prometheus/prometheus.yml
# prometheus.yml의 basic_auth 계정을 application.yml의 monitoring 계정과 동일하게 맞춘 뒤
docker-compose up -d
```

- Prometheus: `http://localhost:9090` (Status → Targets에서 `reelview` job이 `UP`이면 정상)
- Grafana: `http://localhost:3000` (기본 계정 `admin` / `admin`) — Prometheus를 데이터소스로 추가하면 지표 조회 가능

## 관련 저장소
- 프론트엔드: https://github.com/yjdev101/reelview-frontend
