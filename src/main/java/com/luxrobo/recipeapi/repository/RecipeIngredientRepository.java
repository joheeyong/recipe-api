package com.luxrobo.recipeapi.repository;

import com.luxrobo.recipeapi.entity.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {
    List<RecipeIngredient> findByRecipeIdOrderById(Long recipeId);
}
