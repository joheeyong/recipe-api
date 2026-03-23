package com.luxrobo.recipeapi.service;

import com.luxrobo.recipeapi.entity.Recipe;
import com.luxrobo.recipeapi.entity.RecipeIngredient;
import com.luxrobo.recipeapi.entity.RecipeStep;
import com.luxrobo.recipeapi.repository.RecipeIngredientRepository;
import com.luxrobo.recipeapi.repository.RecipeRepository;
import com.luxrobo.recipeapi.repository.RecipeStepRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository ingredientRepository;
    private final RecipeStepRepository stepRepository;

    public RecipeService(RecipeRepository recipeRepository,
                         RecipeIngredientRepository ingredientRepository,
                         RecipeStepRepository stepRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.stepRepository = stepRepository;
    }

    public Page<Recipe> search(String query, String cuisine, String category,
                               Integer difficulty, Boolean userRecipe, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return recipeRepository.search(query, cuisine, category, difficulty, userRecipe, pageRequest);
    }

    public Map<String, Object> getDetail(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
        List<RecipeIngredient> ingredients = ingredientRepository.findByRecipeIdOrderById(recipeId);
        List<RecipeStep> steps = stepRepository.findByRecipeIdOrderByStepNumber(recipeId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recipe", recipe);
        result.put("ingredients", ingredients);
        result.put("steps", steps);
        return result;
    }
}
