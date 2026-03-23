package com.luxrobo.recipeapi.controller;

import com.luxrobo.recipeapi.entity.Recipe;
import com.luxrobo.recipeapi.entity.UserPreference;
import com.luxrobo.recipeapi.entity.UserRecipeHistory;
import com.luxrobo.recipeapi.repository.UserPreferenceRepository;
import com.luxrobo.recipeapi.repository.UserRecipeHistoryRepository;
import com.luxrobo.recipeapi.service.RecipeService;
import com.luxrobo.recipeapi.service.TasteAdjustmentService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final UserRecipeHistoryRepository historyRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final TasteAdjustmentService tasteAdjustmentService;

    public RecipeController(RecipeService recipeService,
                            UserRecipeHistoryRepository historyRepository,
                            UserPreferenceRepository preferenceRepository,
                            TasteAdjustmentService tasteAdjustmentService) {
        this.recipeService = recipeService;
        this.historyRepository = historyRepository;
        this.preferenceRepository = preferenceRepository;
        this.tasteAdjustmentService = tasteAdjustmentService;
    }

    @GetMapping
    public ResponseEntity<Page<Recipe>> list(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Boolean userRecipe,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(recipeService.search(query, cuisine, category, difficulty, userRecipe, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id, Authentication auth) {
        try {
            Map<String, Object> detail = recipeService.getDetail(id);

            // 로그인한 사용자의 입맛 설정이 있으면 재료/조리법 조정
            if (auth != null) {
                Long userId = (Long) auth.getPrincipal();
                preferenceRepository.findByUserId(userId).ifPresent(pref -> {
                    @SuppressWarnings("unchecked")
                    var ingredients = (java.util.List<com.luxrobo.recipeapi.entity.RecipeIngredient>) detail.get("ingredients");
                    @SuppressWarnings("unchecked")
                    var steps = (java.util.List<com.luxrobo.recipeapi.entity.RecipeStep>) detail.get("steps");

                    Map<String, Object> adjusted = tasteAdjustmentService.adjust(ingredients, steps, pref);
                    detail.put("ingredients", adjusted.get("ingredients"));
                    detail.put("steps", adjusted.get("steps"));
                    detail.put("adjustmentNotes", adjusted.get("adjustmentNotes"));
                    detail.put("tasteAdjusted", true);
                });
            }

            return ResponseEntity.ok(detail);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/history")
    public ResponseEntity<?> recordHistory(@PathVariable Long id,
                                           @RequestBody Map<String, Object> body,
                                           Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        Long userId = (Long) auth.getPrincipal();

        UserRecipeHistory history = new UserRecipeHistory();
        history.setUserId(userId);
        history.setRecipeId(id);
        history.setAction((String) body.get("action"));
        if (body.containsKey("rating")) {
            history.setRating((Integer) body.get("rating"));
        }
        historyRepository.save(history);

        return ResponseEntity.ok(Map.of("success", true));
    }
}
