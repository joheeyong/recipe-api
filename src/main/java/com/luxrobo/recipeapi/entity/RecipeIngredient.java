package com.luxrobo.recipeapi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "recipe_ingredients")
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 50)
    private String amount;

    @Column(name = "is_optional")
    private boolean optional = false;

    /**
     * 인분 스케일링 유형:
     * - "linear": 정비례 (면, 고기, 채소, 양념) — 기본값
     * - "sublinear": 비선형 감소 (물, 육수, 국물 — ratio^0.75)
     * - "fixed": 고정 (볶음 기름, 삶는 물, 약간/적당량)
     */
    @Column(name = "scale_type", length = 20)
    private String scaleType = "linear";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
    public boolean isOptional() { return optional; }
    public void setOptional(boolean optional) { this.optional = optional; }
    public String getScaleType() { return scaleType; }
    public void setScaleType(String scaleType) { this.scaleType = scaleType; }
}
