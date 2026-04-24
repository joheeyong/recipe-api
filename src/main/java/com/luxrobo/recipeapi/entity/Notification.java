package com.luxrobo.recipeapi.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // 알림 받는 레시피 소유자

    @Column(nullable = false, length = 20)
    private String type; // "REVIEW" | "BLOG"

    @Column(name = "recipe_id")
    private Long recipeId;

    @Column(name = "recipe_title", length = 200)
    private String recipeTitle;

    @Column(name = "actor_name", length = 100)
    private String actorName; // 리뷰/블로그 작성자 이름

    @Column(name = "is_read")
    private boolean isRead = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }
    public String getRecipeTitle() { return recipeTitle; }
    public void setRecipeTitle(String recipeTitle) { this.recipeTitle = recipeTitle; }
    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
