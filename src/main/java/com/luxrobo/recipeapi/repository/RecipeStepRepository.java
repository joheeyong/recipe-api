package com.luxrobo.recipeapi.repository;

import com.luxrobo.recipeapi.entity.RecipeStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeStepRepository extends JpaRepository<RecipeStep, Long> {
    List<RecipeStep> findByRecipeIdOrderByStepNumber(Long recipeId);
}
