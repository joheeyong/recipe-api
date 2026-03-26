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
| Hosting | AWS EC2 (Amazon Linux 2) | - |

## Package Structure

```
com.luxrobo.recipeapi/
├── config/                     # 설정 (Security, Web, OpenAPI, Exception Handler)
├── security/                   # JWT 인증 필터, 토큰 관리
├── controller/                 # REST API 엔드포인트 (9개)
├── service/                    # 비즈니스 로직 (4개)
├── repository/                 # 데이터 접근 계층 (9개)
├── entity/                     # JPA 엔티티 (9개)
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
       │             └──────────────────┘     │ step_number        │
       │                      │                │ instruction        │
       ▼                      │                │ tip                │
┌──────────────────┐          │                └───────────────────┘
│ user_preferences  │          │
│──────────────────│          │
│ id (PK)           │          ▼
│ user_id (FK, UQ)  │   ┌──────────────────┐
│ spicy_level       │   │ user_recipe_history│   ┌───────────────┐
│ sweetness_level   │   │──────────────────│   │   reviews       │
│ saltiness_level   │   │ id (PK)           │   │───────────────│
│ sourness_level    │   │ user_id (FK)      │   │ id (PK)        │
│ umami_level       │   │ recipe_id (FK)    │   │ user_id (FK)   │
│ oiliness_level    │   │ action            │   │ recipe_id (FK) │
│ preferred_cuisines│   │ rating            │   │ rating (1-5)   │
│ cooking_skill     │   │ created_at        │   │ comment        │
└──────────────────┘   └──────────────────┘   │ created_at     │
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
```

**Key Design Decisions:**
- `recipes.user_id`: NULL = 시스템(공식) 레시피, NOT NULL = 사용자 등록 레시피
- `user_recipe_history.action`: `"bookmark"`, `"view"`, `"rate"` 등 다목적 이력 테이블
- `reviews`: user_id + recipe_id에 유니크 제약 → 사용자당 레시피 1개 리뷰

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

- 저장 경로: `${upload.dir}/recipes/` (이미지), `${upload.dir}/blog/` (블로그 미디어)
- 파일명: UUID + 원본 확장자
- 서빙: `/uploads/**` → WebMvcConfigurer 리소스 핸들러
- 제한: 파일 10MB, 요청 50MB

## Deployment

```
[GitHub Push] → [GitHub Actions]
  → Build JAR (gradlew bootJar -x test)
  → SCP to EC2 (/home/ec2-user/recipe-api/)
  → SSH: systemctl restart recipe
  → Health check: curl http://localhost:8081/health
```

- 프로파일: `prod` (port 8081, SQL 로그 비활성화)
- 서비스: systemd `recipe.service`
- 환경변수: `/home/ec2-user/recipe-api/.env`
