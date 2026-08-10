# ReelView (Backend)

영화·드라마·애니메이션 리뷰 **영상**을 작품별로 모아볼 수 있는 리뷰 플랫폼의 백엔드 API 서버입니다.

기존 텍스트 리뷰 중심 플랫폼(왓챠피디아, 레터박스 등)과 달리, ReelView는 유튜브 등에 흩어진 리뷰 영상을 작품 단위로 큐레이션하고, **리뷰 영상 자체**를 사용자들이 평가하는 데 집중하는 버티컬 서비스를 목표로 합니다.

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.x, Spring Security |
| DB | MySQL, Spring Data JPA |
| 인증 | JWT (jjwt) |
| API 문서 | springdoc-openapi (Swagger UI) |
| 빌드 도구 | Gradle |

## 구현 현황

- 회원가입 / 로그인 / JWT 발급·재발급
- 작품(Content) 등록 · 조회, 장르(Genre) 필터링
- 리뷰(Review) 등록 · 수정 · 삭제
- 리뷰 평점(ReviewRating) 등록 (중복 평가 방지)
- 전역 예외 처리, Swagger API 문서화

## 실행 방법

### 1. 사전 준비
- Java 21
- 로컬 MySQL 서버 (DB `reelview` 생성)

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
```

### 3. 서버 실행

```bash
./gradlew bootRun
```

### 4. API 문서 확인
서버 실행 후 아래 주소에서 Swagger UI로 API 명세를 확인할 수 있습니다.

```
http://localhost:8080/swagger-ui/index.html
```

## 관련 저장소
- 프론트엔드: https://github.com/yjdev101/reelview-frontend
