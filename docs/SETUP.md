# Development Setup

## Prerequisites

- Java 17+
- Gradle 8+ (wrapper 포함)
- MySQL 8.0
- OAuth 클라이언트 등록 (Google, Naver, Kakao)

## Local Development

### 1. Clone

```bash
git clone https://github.com/joheeyong/recipe-api.git
cd recipe-api
```

### 2. Database

```sql
CREATE DATABASE recipe_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Environment Variables

환경변수를 설정하거나, IDE Run Configuration에 직접 입력:

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_URL` | JDBC 연결 URL | `jdbc:mysql://localhost:3306/recipe_db?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true&characterEncoding=UTF-8` |
| `DB_USERNAME` | DB 사용자 | `root` |
| `DB_PASSWORD` | DB 비밀번호 | - |
| `JWT_SECRET` | JWT 서명 키 (32자 이상) | - |
| `GOOGLE_CLIENT_ID` | Google OAuth Client ID | - |
| `GOOGLE_CLIENT_SECRET` | Google OAuth Client Secret | - |
| `NAVER_CLIENT_ID` | Naver OAuth Client ID | - |
| `NAVER_CLIENT_SECRET` | Naver OAuth Client Secret | - |
| `KAKAO_CLIENT_ID` | Kakao OAuth REST API Key | - |
| `KAKAO_CLIENT_SECRET` | Kakao OAuth Client Secret | - |

### 4. Run

```bash
./gradlew bootRun
```

서버가 `http://localhost:8080`에서 시작됩니다.

### 5. Verify

```bash
curl http://localhost:8080/health
# → {"status":"ok"}

# Swagger UI
open http://localhost:8080/swagger-ui.html
```

## Build

```bash
./gradlew bootJar -x test
# → build/libs/recipe-api-0.0.1-SNAPSHOT.jar
```

## Production Deployment (EC2)

### Initial Setup

```bash
# EC2에 SSH 접속
ssh -i ~/.ssh/dadoc-key.pem ec2-user@54.180.179.231

# 설정 스크립트 실행 (1회)
bash deploy/setup.sh

# .env 파일 편집
vi /home/ec2-user/recipe-api/.env
```

### Auto Deploy

`main` 브랜치에 push하면 GitHub Actions가 자동으로:
1. JAR 빌드
2. EC2에 SCP 전송
3. systemd 서비스 재시작
4. 헬스체크 실행

**GitHub Secrets 필요:**
- `EC2_HOST`: EC2 퍼블릭 IP
- `EC2_SSH_KEY`: SSH 프라이빗 키 (PEM)

### Manual Deploy

```bash
# 로컬에서 빌드
./gradlew bootJar -x test

# EC2에 복사
scp -i ~/.ssh/dadoc-key.pem build/libs/recipe-api-0.0.1-SNAPSHOT.jar \
  ec2-user@54.180.179.231:/home/ec2-user/recipe-api/

# EC2에서 재시작
ssh -i ~/.ssh/dadoc-key.pem ec2-user@54.180.179.231 \
  "sudo systemctl restart recipe"
```

### Logs

```bash
# 실시간 로그
ssh -i ~/.ssh/dadoc-key.pem ec2-user@54.180.179.231 \
  "sudo journalctl -u recipe -f"

# 최근 100줄
ssh -i ~/.ssh/dadoc-key.pem ec2-user@54.180.179.231 \
  "sudo journalctl -u recipe -n 100"
```

## Database Access

### AWS RDS (Production)

```
Host: dadoc-db.c5co4yqs8q2i.ap-northeast-2.rds.amazonaws.com
Port: 3306
Database: recipe_db
```

MySQL Workbench 접속 시 RDS 보안 그룹에 IP 허용 필요.

## Project Structure

```
recipe-api/
├── .github/workflows/deploy.yml    # CI/CD 파이프라인
├── deploy/
│   ├── recipe.service              # systemd 서비스 파일
│   └── setup.sh                    # EC2 초기 설정 스크립트
├── docs/
│   ├── API.md                      # API 레퍼런스
│   ├── ARCHITECTURE.md             # 아키텍처 문서
│   └── SETUP.md                    # 이 파일
├── src/main/java/com/luxrobo/recipeapi/
│   ├── config/                     # SecurityConfig, WebConfig, OpenApiConfig, GlobalExceptionHandler
│   ├── security/                   # JwtProvider, JwtAuthenticationFilter
│   ├── controller/                 # REST 컨트롤러 (9개)
│   ├── service/                    # 비즈니스 서비스 (4개)
│   ├── repository/                 # JPA 리포지토리 (9개)
│   └── entity/                     # JPA 엔티티 (9개)
├── src/main/resources/
│   ├── application.properties      # 기본 설정
│   └── application-prod.properties # 운영 설정
├── build.gradle
└── settings.gradle
```
