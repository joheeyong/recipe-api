package com.luxrobo.recipeapi.service;

import com.luxrobo.recipeapi.entity.Recipe;
import com.luxrobo.recipeapi.entity.UserPreference;
import com.luxrobo.recipeapi.repository.RecipeRepository;
import com.luxrobo.recipeapi.repository.UserPreferenceRepository;
import com.luxrobo.recipeapi.repository.UserRecipeHistoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final RecipeRepository recipeRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final UserRecipeHistoryRepository historyRepository;

    public RecommendationService(RecipeRepository recipeRepository,
                                  UserPreferenceRepository preferenceRepository,
                                  UserRecipeHistoryRepository historyRepository) {
        this.recipeRepository = recipeRepository;
        this.preferenceRepository = preferenceRepository;
        this.historyRepository = historyRepository;
    }

    public List<Recipe> recommend(Long userId, int size) {
        Optional<UserPreference> prefOpt = preferenceRepository.findByUserId(userId);

        if (prefOpt.isEmpty()) {
            return recipeRepository.findAll(PageRequest.of(0, size)).getContent();
        }

        UserPreference pref = prefOpt.get();
        Set<String> cuisines = parseCommaSeparated(pref.getPreferredCuisines());
        Set<String> disliked = parseCommaSeparated(pref.getDislikedIngredients());

        List<Long> viewedIds = historyRepository.findRecipeIdsByUserId(userId);
        List<Recipe> candidates;
        if (viewedIds.isEmpty()) {
            candidates = recipeRepository.findAll();
        } else {
            candidates = recipeRepository.findExcluding(viewedIds, PageRequest.of(0, 200));
        }

        return candidates.stream()
            .map(r -> new AbstractMap.SimpleEntry<>(r, score(r, pref, cuisines, disliked)))
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .limit(size)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    public List<Recipe> similar(Long recipeId, int size) {
        Recipe recipe = recipeRepository.findById(recipeId).orElse(null);
        if (recipe == null) return Collections.emptyList();

        List<Recipe> sameCuisine = recipeRepository.findByCuisine(recipe.getCuisine());
        return sameCuisine.stream()
            .filter(r -> !r.getId().equals(recipeId))
            .limit(size)
            .collect(Collectors.toList());
    }

    private double score(Recipe recipe, UserPreference pref,
                         Set<String> cuisines, Set<String> disliked) {
        double score = 0;

        if (cuisines.contains(recipe.getCuisine())) score += 3;
        score += 2 - Math.abs(recipe.getSpicyLevel() - pref.getSpicyLevel());
        if (recipe.getDifficulty() <= pref.getCookingSkill()) score += 2;

        return score;
    }

    private Set<String> parseCommaSeparated(String value) {
        if (value == null || value.isBlank()) return Collections.emptySet();
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
    }
}
