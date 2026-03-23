#!/bin/bash
# EC2 초기 설정 스크립트 - EC2에서 한 번만 실행하면 됩니다
# Usage: bash setup.sh

set -e

echo "=== Recipe API EC2 Setup ==="

# 디렉토리 생성
mkdir -p /home/ubuntu/recipe-api

# .env 파일 생성 (값은 직접 채워주세요)
if [ ! -f /home/ubuntu/recipe-api/.env ]; then
cat > /home/ubuntu/recipe-api/.env << 'ENVEOF'
DB_URL=jdbc:mysql://localhost:3306/recipe_db?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
DB_USERNAME=root
DB_PASSWORD=YOUR_DB_PASSWORD
JWT_SECRET=YOUR_JWT_SECRET_AT_LEAST_32_CHARS_LONG
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
NAVER_CLIENT_ID=
NAVER_CLIENT_SECRET=
KAKAO_CLIENT_ID=
KAKAO_CLIENT_SECRET=
ENVEOF
echo "Created .env file - please edit /home/ubuntu/recipe-api/.env with your values"
fi

# systemd 서비스 등록
sudo cp /home/ubuntu/recipe-api/recipe.service /etc/systemd/system/recipe.service 2>/dev/null || \
cat << 'SERVICEEOF' | sudo tee /etc/systemd/system/recipe.service
[Unit]
Description=Recipe API Spring Boot Application
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/recipe-api
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod /home/ubuntu/recipe-api/recipe-api-0.0.1-SNAPSHOT.jar
EnvironmentFile=/home/ubuntu/recipe-api/.env
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
SERVICEEOF

sudo systemctl daemon-reload
sudo systemctl enable recipe

echo "=== Setup complete ==="
echo "1. Edit /home/ubuntu/recipe-api/.env with your credentials"
echo "2. Push to main branch to trigger deployment"
