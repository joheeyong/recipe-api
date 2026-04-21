# Backend Architecture

## Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | Spring Boot | 3.4.1 |
| Language | Java | 17 |
| ORM | Spring Data JPA + Hibernate | - |
| Database | MySQL 8.0 (AWS RDS) | - |
| Auth | JWT (jjwt 0.12.5) + OAuth 2.0 | - |
| API Docs | SpringDoc OpenAPI (Swagger) | 2.8.4 |
| HTTP Client | Spring WebFlux WebClient | - |
| Build | Gradle | - |
| CI/CD | GitHub Actions | - |
| Hosting | AWS EC2 (Ubuntu) | - |

## Package Structure

```
com.luxrobo.recipeapi/
├── config/                     # 설정 (Security, Web, OpenAPI, Exception Handler)
├── security/                   # JWT 인증 필터, 토큰 관리
├── controller/                 # REST API 엔드포인트 (11개)
├── service/                    # 비즈니스 로직 (5개)
├── repository/                 # 데이터 접근 계층 (11개)
├── entity/                     # JPA 엔티티 (11개)
└── RecipeApiApplication.java   # 진입점
```

## Architecture Pattern

**3-Tier Layered Architecture:**

```
[Client] → [Controller] → [Service] → [Repository] → [MySQL]
```

- **Controller**: HTTP 요청/응답 처리, 인증 확인
- **Service**: 비즈니스 로직 (추천 알고리즘, 입맛 조정, OAuth)
- **Repository**: Spring Data JPA 인터페이스 + 커스텀 @Query

## Database Schema

```
┌─────────────┐     ┌──────────────────┐     ┌───────────────────┐
│   users      │     │   recipes         │     │ recipe_ingredients │
│─────────────│     │──────────────────│     │───────────────────│
│ id (PK)      │     │ id (PK)           │     │ id (PK)            │
│ name         │◄────│ user_id (FK, null)│     │ recipe_id (FK)     │
│ email        │     │ title             │     │ name               │
│ provider     │     │ cuisine           │     │ amount             │
│ provider_id  │     │ category          │     │ optional           │
│ profile_image│     │ difficulty        │     └───────────────────┘
│ created_at   │     │ spicy_level       │
└─────────────┘     │ cook_time_minutes │     ┌───────────────────┐
       │             │ calories          │     │   recipe_steps     │
       │             │ image_url         │     │───────────────────│
       │             │ tags              │     │ id (PK)            │
       │             │ created_at        │     │ recipe_id (FK)     │
       │             │ updated_at        │     │ step_number        │
       │             └──────────────────┘     │ instruction        │
       │                      │               │ tip                │
       ▼                      │               └───────────────────┘
┌──────────────────┐          │
│ user_preferences  │          │
│──────────────────│          ▼
│ id (PK)           │   ┌──────────────────┐
│ user_id (FK, UQ)  │   │ user_recipe_history│   ┌───────────────┐
│ spicy_level       │   │──────────────────│   │   reviews       │
│ sweetness_level   │   │ id (PK)           │   │───────────────│
│ saltiness_level   │   │ user_id (FK)      │   │ id (PK)        │
│ sourness_level    │   │ recipe_id (FK)    │   │ user_id (FK)   │
│ umami_level       │   │ action            │   │ recipe_id (FK) │
│ oiliness_level    │   │ rating            │   │ rating (1-5)   │
│ preferred_cuisines│   │ created_at        │   │ comment        │
│ cooking_skill     │   └──────────────────┘   │ image_url      │
└──────────────────┘                           │ created_at     │
                                               └───────────────┘

┌───────────────┐     ┌───────────────┐
│  blog_posts    │────▶│  blog_media    │
│───────────────│     │───────────────│
│ id (PK)        │     │ id (PK)        │
│ user_id (FK)   │     │ blog_post_id   │
│ recipe_id (FK) │     │ media_url      │
│ content        │     │ media_type     │
│ created_at     │     │ order_index    │
└───────────────┘     └───────────────┘

┌───────────────┐     ┌───────────────────────┐
│  collections   │────▶│  collection_recipes    │
│───────────────│     │───────────────────────│
│ id (PK)        │     │ id (PK)                │
│ user_id (FK)   │     │ collection_id (FK)     │
│ name           │     │ recipe_id (FK)         │
│ emoji          │     │ added_at               │
│ description    │     └───────────────────────┘
│ created_at     │
└───────────────┘

┌───────────────┐
│ notifications  │
│───────────────│
│ id (PK)        │
│ user_id (FK)   │  ← 알림 받는 레시피 소유자
│ type           │  ← "REVIEW" | "BLOG"
│ recipe_id      │
│ recipe_title   │
│ actor_name     │  ← 리뷰/블로그 작성자 이름
│ is_read        │
│ created_at     │
└───────────────┘
```

**Key Design Decisions:**
- `recipes.user_id`: NULL = 시스템(공식) 레시피, NOT NULL = 사용자 등록 레시피
- `recipes.updated_at`: `@PreUpdate`로 수정 시 자동 갱신
- `user_recipe_history.action`: `"bookmark"`, `"view"`, `"rate"` 다목적 이력 테이블
- `reviews`: user_id + recipe_id에 유니크 제약 → 사용자당 레시피 1개 리뷰
- `reviews.image_url`: 리뷰 사진 첨부 경로
- `notifications`: 리뷰/블로그 작성 시 트리거, 폴링 방식으로 프론트에서 수신

## Core Services

### RecommendationService
사용자 입맛 설정 기반 점수제 추천.

```
Score = cuisine_match(+3) + spicy_similarity(+2) + difficulty_fit(+2)
```

이미 본 레시피는 제외. 점수 내림차순으로 상위 N개 반환.

### TasteAdjustmentService
레시피 상세 조회 시 사용자 입맛에 맞게 재료 양/조리 팁을 자동 조정.

- 50+ 한국 재료의 맛 카테고리 매핑 (매운맛, 단맛, 짠맛 등)
- 조정 비율: `0.5 (level=1) ~ 1.5 (level=10)`, 기본값(level=5) = 1.0x
- 각 조리 단계에 입맛 맞춤 팁 생성

### OAuthService
Google, Naver, Kakao OAuth 2.0 인증 코드 → 토큰 교환 → 사용자 upsert → JWT 발급.
WebClient (WebFlux)로 OAuth 프로바이더와 비동기 통신.

### IngredientScaleService
레시피 인분 수 변경 시 재료 양 비례 스케일링.

## Notification Flow

```
[리뷰 작성] → ReviewController.create()
  → reviewRepository.save(review)
  → isNew == true && recipe.userId != reviewerId
  → Notification { type="REVIEW", userId=recipe.userId, actorName=reviewer.name }
  → notificationRepository.save(noti)

[블로그 작성] → BlogController.create()
  → blogPostRepository.save(post)
  → recipe.userId != posterId
  → Notification { type="BLOG", userId=recipe.userId, actorName=poster.name }
  → notificationRepository.save(noti)

[프론트 폴링] → GET /api/notifications/unread-count (30초마다)
  → 알림 페이지 진입 → GET /api/notifications
  → 자동 전체 읽음 처리 → PUT /api/notifications/read-all
```

## Authentication Flow

```
[Client] → POST /api/auth/google { code, redirectUri }
         → [AuthController] → [OAuthService]
           → Google Token Endpoint (code → access_token)
           → Google UserInfo (access_token → user info)
           → Upsert User (find or create by provider+providerId)
           → Generate JWT (userId as subject, 24h expiry)
         ← { token, user }
```

이후 모든 인증 요청:
```
[Client] → Authorization: Bearer <jwt>
         → [JwtAuthenticationFilter]
           → Validate token, extract userId
           → Set SecurityContext
         → [Controller] → Authentication.getPrincipal() = userId (Long)
```

## File Upload

- 저장 경로:
  - `${upload.dir}/recipes/` — 레시피 대표 이미지
  - `${upload.dir}/reviews/` — 리뷰 첨부 사진
  - `${upload.dir}/blog/` — 블로그 미디어
- 파일명: UUID + 원본 확장자
- 서빙: `/uploads/**` → WebMvcConfigurer 리소스 핸들러
- 제한: 파일 10MB, 요청 50MB

## Deployment

```
[GitHub Push to main] → [GitHub Actions]
  → Build JAR (./gradlew bootJar -x test)
  → SCP JAR to EC2 (/home/ubuntu/recipe-api/)
  → SSH: sudo systemctl restart recipe
  → Health check: curl http://localhost:8081/health
```

- 프로파일: `prod` (port 8081, SQL 로그 비활성화)
- 서비스: systemd `recipe.service`
- 환경변수: `/home/ubuntu/recipe-api/.env`
- 업로드 디렉토리: `/home/ubuntu/recipe-api/uploads/`
