# TMDB 연동 계획 (작품 데이터 대량 등록)

## 목표
지금은 작품(Content)을 Swagger/API로 하나씩 수동 등록하고 있음. TMDB(The Movie Database) API를 연동해서 실제 영화 데이터를 대량으로 채워 넣는다.

## 왜 크롤링이 아니라 TMDB API인가
- 네이버 영화, 왓챠 등을 크롤링하면 이용약관 위반 소지가 있고 페이지 구조 변경에 취약함
- TMDB는 무료 공식 API로 학습/포트폴리오 목적에 적합, 데이터가 구조화된 JSON으로 제공됨

## 진행 순서

### 1. API 키 발급
- themoviedb.org 가입 → 계정 설정에서 API 키 신청
- 참고 문서: https://developer.themoviedb.org/docs/getting-started (개요만 있음, 실제 인증 방식은 하위 Authentication 문서에서 v3 key 방식 확인)

### 2. 사용할 엔드포인트
- `GET /movie/popular` — 인기 영화 목록 (1차로 이걸로 대량 등록)
- `GET /genre/movie/list` — TMDB 장르 ID ↔ 이름 매핑용
- `language=ko-KR` 파라미터로 한국어 제목/줄거리 수신

### 3. 매핑 설계
| TMDB 응답 필드 | 우리 Content 필드 |
|---|---|
| `title` | `title` |
| `overview` | `description` |
| `release_date` (연도만 추출) | `releaseYear` |
| `genre_ids` → 장르명 변환 | `genreNames` |
| — (고정값) | `type` = `MOVIE` |

- `genre_ids`는 숫자 배열이라, 2번에서 받은 TMDB 장르 목록으로 이름을 찾아 우리 DB의 장르명과 매칭 필요 (TMDB 장르명이 우리 `genre` 테이블 값과 다를 수 있어 수동 매핑 테이블 필요할 수 있음)
- 포스터 이미지가 필요해지면 `poster_path`를 `https://image.tmdb.org/t/p/w500` + `poster_path`로 조합

### 4. 구현 방식 후보
- **후보 A**: `TmdbClient` 서비스 신설 (RestTemplate/WebClient로 TMDB 호출) → 응답을 `CreateContentRequest`로 변환 → 기존 `ContentService.createContent()` 재사용해서 저장
- **후보 B**: 관리자 전용 `POST /admin/contents/import` 엔드포인트로 온디맨드 실행 (재사용/재실행 가능, 추천)
- 일회성 배치 스크립트(`CommandLineRunner`)는 재실행이 번거로워서 후보 B가 더 나을 것으로 판단, 실제 구현 시점에 결정

## 남은 리스크 / 확인 필요
- TMDB API rate limit (요청 빈도 제한) 확인 필요
- 중복 등록 방지 — 이미 등록된 작품(제목+연도 기준?) 재실행 시 중복 안 되게 처리 필요 (아직 미구현, 같은 페이지 재요청 시 중복 저장됨)
- TMDB 이용 시 어트리뷰션 문구("This product uses the TMDB API but is not endorsed or certified by TMDB") 표시 필요 여부 확인
- **[실제 발견, 2026-08-19] 성인/선정적 콘텐츠 필터링 안 됨**: `/movie/popular` 결과에 성인물 성격의 작품이 섞여 들어옴. TMDB의 `adult` 필드와 `softcore` 필드 둘 다 `false`로 표시되어 있어 두 필드로는 걸러지지 않음 — 실제로 겪은 뒤 수동으로 DB에서 삭제 처리함(1건). 추후 개선 필요: `vote_count` 최소 기준 추가, 또는 장르 조합(예: 로맨스+드라마 저인기작) 휴리스틱, 또는 TMDB `certification` 관련 API 조사.

## 상태
**구현 완료 (2026-08-19)**: `TmdbMovieDto`/`TmdbMovieListResponse`(응답 DTO), `TmdbClient`(RestClient로 `/movie/popular` 호출), `TmdbGenreMapper`(TMDB 장르 ID → 우리 장르명, 17/19 커버 — Animation/TV Movie 제외), `TmdbImportService`(페이지 반복 호출 + `ContentService.createContent()` 저장), `POST /admin/tmdb/import?pages=` 엔드포인트. 실제로 1페이지(20개) import 성공 검증 완료(성인물 1건 발견해 수동 삭제).

다음 개선 과제: 중복 방지, 성인 콘텐츠 필터링, 프로덕션에서 쓸 거면 어트리뷰션 문구.
