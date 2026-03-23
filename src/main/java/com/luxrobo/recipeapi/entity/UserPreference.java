package com.luxrobo.recipeapi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(name = "spicy_level")
    private int spicyLevel = 3;

    @Column(name = "sweetness_level")
    private int sweetnessLevel = 3;

    @Column(name = "saltiness_level")
    private int saltinessLevel = 3;

    @Column(name = "dietary_restrictions", length = 500)
    private String dietaryRestrictions;

    @Column(name = "disliked_ingredients", length = 1000)
    private String dislikedIngredients;

    @Column(name = "preferred_cuisines", length = 500)
    private String preferredCuisines;

    @Column(name = "cooking_skill")
    private int cookingSkill = 2;

    public UserPreference() {}

    public UserPreference(Long userId) {
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public int getSpicyLevel() { return spicyLevel; }
    public void setSpicyLevel(int spicyLevel) { this.spicyLevel = spicyLevel; }
    public int getSweetnessLevel() { return sweetnessLevel; }
    public void setSweetnessLevel(int sweetnessLevel) { this.sweetnessLevel = sweetnessLevel; }
    public int getSaltinessLevel() { return saltinessLevel; }
    public void setSaltinessLevel(int saltinessLevel) { this.saltinessLevel = saltinessLevel; }
    public String getDietaryRestrictions() { return dietaryRestrictions; }
    public void setDietaryRestrictions(String dietaryRestrictions) { this.dietaryRestrictions = dietaryRestrictions; }
    public String getDislikedIngredients() { return dislikedIngredients; }
    public void setDislikedIngredients(String dislikedIngredients) { this.dislikedIngredients = dislikedIngredients; }
    public String getPreferredCuisines() { return preferredCuisines; }
    public void setPreferredCuisines(String preferredCuisines) { this.preferredCuisines = preferredCuisines; }
    public int getCookingSkill() { return cookingSkill; }
    public void setCookingSkill(int cookingSkill) { this.cookingSkill = cookingSkill; }
}
