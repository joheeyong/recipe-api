# Recipe API Reference

> Base URL: `http://54.180.179.231:8081`
> Swagger UI: `/swagger-ui.html`
> OpenAPI Spec: `/v3/api-docs`

## Authentication

JWT Bearer 토큰 방식. `Authorization: Bearer <token>` 헤더로 전달.
토큰 유효기간: 24시간.

---

## Auth

### `POST /api/auth/google`
Google OAuth 로그인.

**Request Body:**
```json
{ "code": "authorization_code", "redirectUri": "https://..." }
```

**Response:**
```json
{ "token": "jwt_token", "user": { "id": 1, "name": "...", "email": "..." } }
```

### `POST /api/auth/naver`
Naver OAuth 로그인.

**Request Body:**
```json
{ "code": "...", "state": "...", "redirectUri": "https://..." }
```

### `POST /api/auth/kakao`
Kakao OAuth 로그인.

**Request Body:**
```json
{ "code": "...", "redirectUri": "https://..." }
```

### `GET /api/auth/me`
현재 로그인한 사용자 정보 조회.

**Auth:** Required

**Response:**
```json
{ "id": 1, "name": "조희용", "email": "...", "provider": "google", "profileImage": "..." }
```

---

## Recipes

### `GET /api/recipes`
레시피 목록 조회 (검색, 필터링, 페이지네이션).

**Auth:** Optional

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `query` | string | - | 제목/설명/태그 검색 |
| `cuisine` | string | - | 카테고리 필터 (`korean`, `western`, `chinese`, `japanese`, `southeast_asian`, `mexican`) |
| `category` | string | - | 요리 분류 (`main`, `side`, `soup`, `dessert`) |
| `difficulty` | int | - | 난이도 (1: 쉬움, 2: 보통, 3: 어려움) |
| `userRecipe` | boolean | - | `true`: 사용자 레시피만, `null/미지정`: 시스템 레시피만 |
| `page` | int | 0 | 페이지 번호 (0부터 시작) |
| `size` | int | 20 | 페이지 크기 |

**Response:** `Page<Recipe>`
```json
{
  "content": [{ "id": 1, "title": "김치찌개", "cuisine": "korean", ... }],
  "totalElements": 34,
  "totalPages": 2,
  "number": 0,
  "size": 20
}
```

### `GET /api/recipes/{id}`
레시피 상세 조회. 로그인 시 사용자 입맛 설정에 따라 재료 양과 조리 팁이 자동 조정됨.

**Auth:** Optional (입맛 조정은 로그인 시에만)

**Response:**
```json
{
  "recipe": { "id": 1, "title": "김치찌개", "cuisine": "korean", ... },
  "ingredients": [
    { "name": "김치", "amount": "200g", "originalAmount": "300g", "tasteCategories": ["spicy", "sour"] }
  ],
  "steps": [
    { "stepNumber": 1, "instruction": "김치를 썰어...", "tip": "매운맛을 줄이려면 물에 한번 헹궈주세요" }
  ],
  "adjustmentNotes": ["매운맛 재료를 줄였습니다 (레벨 3/10)"],
  "tasteAdjusted": true
}
```

### `POST /api/recipes/{id}/history`
사용자 레시피 열람/평가 기록.

**Auth:** Required

**Request Body:**
```json
{ "action": "view" }
```
또는
```json
{ "action": "rate", "rating": 4 }
```

---

## Recommendations

### `GET /api/recommendations`
사용자 입맛 설정 기반 맞춤 레시피 추천.

**Auth:** Required (미인증 시 빈 배열 반환)

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `size` | int | 20 | 추천 개수 |

**추천 알고리즘 (점수 기반):**
- 선호 카테고리 매칭: +3점
- 매운맛 레벨 유사도: 최대 +2점
- 요리 난이도 적합: +2점
- 이미 본 레시피는 제외

**Response:** `Recipe[]`

### `GET /api/recommendations/similar/{recipeId}`
동일 cuisine의 유사 레시피 추천.

**Auth:** Not required

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `size` | int | 10 | 추천 개수 |

---

## User Recipes

### `GET /api/my-recipes/all`
모든 사용자 등록 레시피 (공개).

**Auth:** Not required

**Response:**
```json
[
  { "id": 31, "title": "밥피자", "userName": "조희용", "userId": 1, ... }
]
```

### `GET /api/my-recipes/mine`
내가 등록한 레시피만 조회.

**Auth:** Required

### `POST /api/my-recipes`
레시피 등록 (multipart/form-data).

**Auth:** Required

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `title` | string | Yes | 레시피 제목 |
| `description` | string | No | 설명 |
| `category` | string | No | 분류 (default: main) |
| `difficulty` | int | No | 난이도 1-3 (default: 2) |
| `cookTimeMinutes` | int | No | 조리 시간(분) |
| `servingSize` | int | No | 인분 (default: 2) |
| `calories` | int | No | 칼로리 |
| `spicyLevel` | int | No | 매운맛 0-5 |
| `tags` | string | No | 태그 (쉼표 구분) |
| `ingredientNames` | string[] | Yes | 재료 이름 배열 |
| `ingredientAmounts` | string[] | Yes | 재료 양 배열 |
| `stepInstructions` | string[] | Yes | 조리 순서 배열 |
| `stepTips` | string[] | No | 각 단계 팁 |
| `image` | file | No | 대표 이미지 (최대 10MB) |

### `DELETE /api/my-recipes/{id}`
내 레시피 삭제 (본인만 가능).

**Auth:** Required

---

## Reviews

### `GET /api/recipes/{recipeId}/reviews`
레시피 리뷰 목록 조회.

**Auth:** Not required

**Response:**
```json
{
  "reviews": [
    { "id": 1, "rating": 5, "comment": "맛있어요!", "userName": "조희용", ... }
  ],
  "avgRating": 4.5,
  "reviewCount": 12
}
```

### `POST /api/recipes/{recipeId}/reviews`
리뷰 작성 (레시피당 1개, 이미 있으면 수정).

**Auth:** Required

**Request Body:**
```json
{ "rating": 5, "comment": "진짜 맛있어요!" }
```

### `DELETE /api/recipes/{recipeId}/reviews/{reviewId}`
리뷰 삭제 (본인만 가능).

**Auth:** Required

---

## Bookmarks

### `GET /api/bookmarks`
저장한 레시피 목록 (최신 순).

**Auth:** Required

**Response:** `Recipe[]`

### `POST /api/bookmarks/{recipeId}`
레시피 북마크 추가.

**Auth:** Required

**Response:** `{ "bookmarked": true }`

### `DELETE /api/bookmarks/{recipeId}`
북마크 해제.

**Auth:** Required

**Response:** `{ "bookmarked": false }`

### `GET /api/bookmarks/{recipeId}/check`
북마크 여부 확인.

**Auth:** Optional (미인증 시 false)

**Response:** `{ "bookmarked": true }`

---

## Blog

### `GET /api/blog/recipe/{recipeId}`
레시피별 블로그 글 목록.

**Auth:** Not required

### `GET /api/blog/{id}`
블로그 글 상세.

**Auth:** Not required

**Response:**
```json
{
  "id": 1,
  "content": "오늘 김치찌개를 만들었는데...",
  "userName": "조희용",
  "recipeName": "김치찌개",
  "mediaUrls": ["/uploads/blog/abc123.jpg"],
  "createdAt": "2026-03-25T12:00:00"
}
```

### `POST /api/blog`
블로그 글 작성 (multipart/form-data).

**Auth:** Required

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `recipeId` | long | Yes | 레시피 ID |
| `content` | string | Yes | 글 내용 |
| `files` | file[] | No | 이미지/동영상 (최대 10MB/개) |

### `DELETE /api/blog/{id}`
블로그 글 삭제 (본인만).

**Auth:** Required

---

## User Preferences

### `GET /api/preferences`
입맛 설정 조회 (없으면 기본값 생성).

**Auth:** Required

**Response:**
```json
{
  "spicyLevel": 5,
  "sweetnessLevel": 5,
  "saltinessLevel": 5,
  "sournessLevel": 5,
  "umamiLevel": 5,
  "oilinessLevel": 5,
  "preferredCuisines": "korean,japanese",
  "cookingSkill": 2
}
```

### `PUT /api/preferences`
입맛 설정 저장.

**Auth:** Required

**Request Body:** (위와 동일 형식)

---

## Health Check

### `GET /health`
서버 상태 확인. 배포 후 자동 호출됨.

**Response:** `{ "status": "ok" }`

---

## Error Response Format

모든 에러 응답은 일관된 형식:

```json
{
  "timestamp": "2026-03-26T12:00:00.000",
  "status": 400,
  "error": "Validation failed",
  "details": { "title": "must not be blank" }
}
```

| Status | Description |
|--------|-------------|
| 400 | 잘못된 요청 (검증 실패, 잘못된 파라미터) |
| 401 | 인증 필요 |
| 403 | 권한 없음 (다른 사용자의 리소스) |
| 404 | 리소스 없음 |
| 413 | 파일 크기 초과 (10MB) |
| 500 | 서버 내부 오류 |
