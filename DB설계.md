# ReelView DB 설계

## 테이블 관계

```
user (1) ─── (N) review (N) ─── (1) content
content (N) ─── (N) genre  (content_genre 중간 테이블)
user (N) ─── (N) review  (review_rating 중간 테이블)
```

- `user` 1명이 `review`를 여러 개 작성/큐레이션 → **1:N**
- `content` 1개에 `review`가 여러 개 달림 → **1:N**
- `content`와 `genre`는 **N:M** (`content_genre` 중간 테이블로 해소 — 작품 1개가 여러 장르를 가질 수 있어야 해서 단일 VARCHAR 컬럼에서 분리)
- `user`와 `review`는 **N:M** (`review_rating` 중간 테이블로 해소 — ReelView의 평점은 1차 콘텐츠(작품)가 아니라 2차 콘텐츠인 리뷰 영상에 대한 평가이며, 한 사용자가 여러 리뷰를 평가하고 한 리뷰가 여러 사용자에게 평가받음)

---

## 테이블 1: `user` (회원)

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT (PK) | 고유 번호 (자동 증가) |
| username | VARCHAR(50) UNIQUE | 아이디 (중복 가입 방지) |
| password | VARCHAR(255) | 비밀번호 (암호화 저장) |
| email | VARCHAR(255) UNIQUE | 이메일 (중복 가입 방지, OAuth2 연동 시 사용자 식별에 사용) |
| nickname | VARCHAR(50) | 닉네임 |
| role | ENUM('USER','ADMIN') | 권한 (기본값 USER. ADMIN만 작품 등록/수정/삭제 가능) |
| created_at | DATETIME | 가입일 |

---

## 테이블 2: `content` (작품)

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT (PK) | 고유 번호 (자동 증가) |
| title | VARCHAR(200) | 작품 제목 |
| type | ENUM('MOVIE','DRAMA','ANIMATION') | 작품 유형 |
| release_year | YEAR | 개봉·방영 연도 |
| description | TEXT | 작품 소개 |
| created_at | DATETIME | 등록일 |

> 기존 `genre` VARCHAR 컬럼은 제거하고, 작품 1개가 여러 장르를 가질 수 있도록 아래 `genre`/`content_genre` 테이블로 분리했습니다.

---

## 테이블 3: `genre` (장르)

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT (PK) | 고유 번호 (자동 증가) |
| name | VARCHAR(50) UNIQUE | 장르명 (예: 액션, SF, 드라마) |

---

## 테이블 4: `content_genre` (작품-장르 매핑)

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| content_id | BIGINT (FK, PK) | content.id 참조 |
| genre_id | BIGINT (FK, PK) | genre.id 참조 |

- `(content_id, genre_id)` 복합 PK로 같은 작품에 같은 장르가 중복 등록되는 것을 방지

---

## 테이블 5: `review` (리뷰 영상)

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT (PK) | 고유 번호 (자동 증가) |
| user_id | BIGINT (FK, NULL 허용) | 작성자 (user.id 참조). 작성자가 탈퇴하면 NULL로 처리 (ON DELETE SET NULL) — 리뷰는 콘텐츠로서 보존 |
| content_id | BIGINT (FK) | 대상 작품 (content.id 참조). 리뷰가 달린 작품은 삭제 금지 (ON DELETE RESTRICT) |
| title | VARCHAR(200) | 리뷰 제목 |
| review_type | ENUM('UPLOAD','URL') | 직접 업로드 / URL 큐레이션 |
| video_url | VARCHAR(500) | 유튜브 영상 URL (review_type=URL일 때만 값, UPLOAD는 NULL) |
| video_file_path | VARCHAR(500) | 업로드된 영상 파일 경로 (review_type=UPLOAD일 때만 값, URL은 NULL) |
| description | TEXT | 리뷰 설명 |
| comment_summary | TEXT | 유튜브 댓글 AI 요약 (URL 큐레이션 타입만 해당) |
| created_at | DATETIME | 등록일 |
| updated_at | DATETIME | 수정일 |

> `rating` 컬럼은 없습니다. ReelView의 평점은 작품(1차 콘텐츠)이 아니라 리뷰 영상(2차 콘텐츠) 자체에 대한 평가이므로, 작성자 본인이 정하는 값이 아니라 아래 `review_rating`에서 다른 사용자들이 매기는 값들의 평균으로 계산됩니다.

---

## 테이블 6: `review_rating` (리뷰 영상 평가)

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT (PK) | 고유 번호 (자동 증가) |
| review_id | BIGINT (FK) | 평가 대상 리뷰 (review.id 참조) |
| user_id | BIGINT (FK) | 평가한 사용자 (user.id 참조) |
| rating | TINYINT | 별점 (1~5) |
| created_at | DATETIME | 평가 등록일 |

- `(review_id, user_id)` UNIQUE — 한 사용자가 같은 리뷰에 중복 평가하는 것을 방지
- 리뷰의 평점은 `AVG(rating) WHERE review_id = ?`로 계산 (컬럼이 아니라 조회 시 집계하는 값)

---

## 컬럼 타입 선정 이유

| 타입 | 선정 이유 |
|------|-----------|
| BIGINT | id는 데이터가 쌓일수록 계속 증가하므로 넉넉한 범위 확보 |
| VARCHAR(255) | BCrypt 암호화 결과가 60자 내외로 고정되므로 여유 있게 255로 지정 |
| ENUM | 정해진 값만 허용하므로 잘못된 값 입력을 DB 레벨에서 차단 |
| YEAR | 연도만 저장하면 충분한 경우 DATETIME보다 가벼운 YEAR 타입 사용 |
| TEXT | 리뷰 설명처럼 길이 제한 없이 작성할 수 있는 내용에 사용 |
| TINYINT | 별점은 1~5만 저장하면 되므로 가장 작은 정수 타입 사용 |
| DATETIME | 날짜와 시간을 함께 저장. 등록일·수정일 추적에 사용 |
| TEXT (comment_summary) | AI 요약 결과는 길이가 일정하지 않으므로 VARCHAR 대신 TEXT 사용. `review_type`이 URL인 경우에만 값이 채워지며, 업로드 타입은 NULL |
| VARCHAR(500) (video_url / video_file_path) | 외부 URL과 서버 파일 경로는 성격이 달라 컬럼을 분리. `review_type`에 따라 둘 중 하나만 값이 채워지고 나머지는 NULL |
| UNIQUE (username, genre.name) | 아이디 중복 가입과 동일 장르명 중복 등록을 DB 레벨에서 차단 |

---

## FK 삭제 정책 (ON DELETE)

| FK | 정책 | 이유 |
|----|------|------|
| review.user_id → user.id | SET NULL | 회원이 탈퇴해도 리뷰는 다른 사용자에게 유효한 콘텐츠이므로 보존. 화면에는 "알 수 없는 사용자"로 표시 |
| review.content_id → content.id | RESTRICT | 작품은 마스터 데이터이므로, 리뷰가 달려있으면 삭제를 막아 실수로 인한 데이터 유실 방지 |
| content_genre.content_id → content.id | CASCADE | 작품이 삭제되면 매핑도 함께 삭제 (위 RESTRICT 때문에 실제로는 거의 발생하지 않음) |
| content_genre.genre_id → genre.id | RESTRICT | 사용 중인 장르는 삭제 방지 |
| review_rating.review_id → review.id | CASCADE | 리뷰가 삭제되면 그 리뷰에 달린 평가도 의미가 없으므로 함께 삭제 |
| review_rating.user_id → user.id | CASCADE | 회원이 탈퇴하면 그 사람이 남긴 평가(투표 1건)도 함께 삭제. 평점은 남은 평가들로 재계산됨 |

---

## 시드 데이터: 장르 목록

영화/드라마/애니메이션에 공통으로 쓰는 일반 장르 22개를 초기 데이터로 등록합니다. 사용자가 임의로 새 장르를 추가하지 못하게 하고, 부족하면 운영자가 `genre`에 row를 추가하는 방식으로 운영합니다.

| id | name |
|----|------|
| 1 | 액션 |
| 2 | 모험 |
| 3 | SF |
| 4 | 판타지 |
| 5 | 스릴러 |
| 6 | 미스터리 |
| 7 | 범죄 |
| 8 | 호러 |
| 9 | 드라마 |
| 10 | 로맨스 |
| 11 | 코미디 |
| 12 | 가족 |
| 13 | 다큐멘터리 |
| 14 | 음악 |
| 15 | 전쟁 |
| 16 | 사극 |
| 17 | 느와르 |
| 18 | 무협 |
| 19 | 스포츠 |
| 20 | 청춘/학교 |
| 21 | 일상 |
| 22 | 이세계 |

실제 INSERT문은 `src/main/resources/data.sql`에 작성해두었습니다.
