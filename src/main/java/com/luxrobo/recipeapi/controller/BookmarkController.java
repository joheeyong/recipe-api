package com.luxrobo.recipeapi.controller;

import com.luxrobo.recipeapi.entity.Recipe;
import com.luxrobo.recipeapi.entity.UserRecipeHistory;
import com.luxrobo.recipeapi.repository.RecipeRepository;
import com.luxrobo.recipeapi.repository.UserRecipeHistoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final UserRecipeHistoryRepository historyRepository;
    private final RecipeRepository recipeRepository;

    public BookmarkController(UserRecipeHistoryRepository historyRepository,
                               RecipeRepository recipeRepository) {
        this.historyRepository = historyRepository;
        this.recipeRepository = recipeRepository;
    }

    /** 북마크 목록 조회 */
    @GetMapping
    public ResponseEntity<?> list(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        }
        Long userId = (Long) auth.getPrincipal();
        List<Long> recipeIds = historyRepository.findRecipeIdsByUserIdAndAction(userId, "bookmark");
        if (recipeIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<Recipe> recipes = recipeRepository.findAllById(recipeIds);
        // 순서 유지 (최신 북마크 순)
        Map<Long, Recipe> recipeMap = recipes.stream().collect(Collectors.toMap(Recipe::getId, r -> r));
        List<Recipe> ordered = recipeIds.stream()
                .map(recipeMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ordered);
    }

    /** 북마크 추가 */
    @PostMapping("/{recipeId}")
    public ResponseEntity<?> add(@PathVariable Long recipeId, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        }
        Long userId = (Long) auth.getPrincipal();
        if (historyRepository.existsByUserIdAndRecipeIdAndAction(userId, recipeId, "bookmark")) {
            return ResponseEntity.ok(Map.of("bookmarked", true));
        }
        UserRecipeHistory h = new UserRecipeHistory();
        h.setUserId(userId);
        h.setRecipeId(recipeId);
        h.setAction("bookmark");
        historyRepository.save(h);
        return ResponseEntity.ok(Map.of("bookmarked", true));
    }

    /** 북마크 삭제 */
    @DeleteMapping("/{recipeId}")
    @Transactional
    public ResponseEntity<?> remove(@PathVariable Long recipeId, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        }
        Long userId = (Long) auth.getPrincipal();
        historyRepository.deleteByUserIdAndRecipeIdAndAction(userId, recipeId, "bookmark");
        return ResponseEntity.ok(Map.of("bookmarked", false));
    }

    /** 북마크 여부 확인 */
    @GetMapping("/{recipeId}/check")
    public ResponseEntity<?> check(@PathVariable Long recipeId, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.ok(Map.of("bookmarked", false));
        }
        Long userId = (Long) auth.getPrincipal();
        boolean exists = historyRepository.existsByUserIdAndRecipeIdAndAction(userId, recipeId, "bookmark");
        return ResponseEntity.ok(Map.of("bookmarked", exists));
    }
}
