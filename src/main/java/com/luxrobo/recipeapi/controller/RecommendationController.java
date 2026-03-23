package com.luxrobo.recipeapi.controller;

import com.luxrobo.recipeapi.entity.Recipe;
import com.luxrobo.recipeapi.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ResponseEntity<List<Recipe>> recommend(
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        Long userId = auth != null ? (Long) auth.getPrincipal() : null;
        if (userId == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(recommendationService.recommend(userId, size));
    }

    @GetMapping("/similar/{recipeId}")
    public ResponseEntity<List<Recipe>> similar(
            @PathVariable Long recipeId,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(recommendationService.similar(recipeId, size));
    }
}
