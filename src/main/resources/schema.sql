-- MySQLWorkbench에서 이 스크립트를 실행하세요
CREATE DATABASE IF NOT EXISTS recipe_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE recipe_db;

-- 테이블은 Spring JPA ddl-auto=update가 자동 생성합니다.
-- 수동 생성이 필요하면 아래 SQL을 사용하세요.

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255),
    provider VARCHAR(50),
    provider_id VARCHAR(255),
    profile_image VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_provider (provider, provider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    spicy_level INT DEFAULT 3,
    sweetness_level INT DEFAULT 3,
    saltiness_level INT DEFAULT 3,
    dietary_restrictions VARCHAR(500),
    disliked_ingredients VARCHAR(1000),
    preferred_cuisines VARCHAR(500),
    cooking_skill INT DEFAULT 2,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS recipes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    cuisine VARCHAR(50),
    category VARCHAR(50),
    difficulty INT DEFAULT 2,
    cook_time_minutes INT,
    serving_size INT DEFAULT 2,
    calories INT,
    image_url VARCHAR(500),
    spicy_level INT DEFAULT 0,
    tags VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS recipe_ingredients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    amount VARCHAR(50),
    is_optional BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (recipe_id) REFERENCES recipes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS recipe_steps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    step_number INT NOT NULL,
    instruction TEXT NOT NULL,
    image_url VARCHAR(500),
    tip TEXT,
    FOREIGN KEY (recipe_id) REFERENCES recipes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_recipe_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    rating INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_action (user_id, action),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (recipe_id) REFERENCES recipes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
