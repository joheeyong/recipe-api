package com.luxrobo.recipeapi.controller;

import com.luxrobo.recipeapi.entity.Recipe;
import com.luxrobo.recipeapi.entity.UserRecipeHistory;
import com.luxrobo.recipeapi.repository.UserRecipeHistoryRepository;
import com.luxrobo.recipeapi.service.RecipeService;
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

    public RecipeController(RecipeService recipeService,
                            UserRecipeHistoryRepository historyRepository) {
        this.recipeService = recipeService;
        this.historyRepository = historyRepository;
    }

    @GetMapping
    public ResponseEntity<Page<Recipe>> list(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(recipeService.search(query, cuisine, category, difficulty, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(recipeService.getDetail(id));
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
