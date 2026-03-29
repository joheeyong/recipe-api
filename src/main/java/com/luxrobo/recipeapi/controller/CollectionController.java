package com.luxrobo.recipeapi.controller;

import com.luxrobo.recipeapi.entity.Collection;
import com.luxrobo.recipeapi.entity.CollectionRecipe;
import com.luxrobo.recipeapi.entity.Recipe;
import com.luxrobo.recipeapi.repository.CollectionRecipeRepository;
import com.luxrobo.recipeapi.repository.CollectionRepository;
import com.luxrobo.recipeapi.repository.RecipeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {

    private final CollectionRepository collectionRepository;
    private final CollectionRecipeRepository collectionRecipeRepository;
    private final RecipeRepository recipeRepository;

    public CollectionController(CollectionRepository collectionRepository,
                                CollectionRecipeRepository collectionRecipeRepository,
                                RecipeRepository recipeRepository) {
        this.collectionRepository = collectionRepository;
        this.collectionRecipeRepository = collectionRecipeRepository;
        this.recipeRepository = recipeRepository;
    }

    /** 컬렉션 목록 조회 */
    @GetMapping
    public ResponseEntity<?> list(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        }
        Long userId = (Long) auth.getPrincipal();
        List<Collection> collections = collectionRepository.findByUserIdOrderByCreatedAtDesc(userId);

        // 각 컬렉션의 대표 이미지 (첫 번째 레시피 이미지)
        List<Map<String, Object>> result = new ArrayList<>();
        for (Collection c : collections) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", c.getId());
            item.put("name", c.getName());
            item.put("description", c.getDescription());
            item.put("emoji", c.getEmoji());
            item.put("recipeCount", c.getRecipeCount());
            item.put("createdAt", c.getCreatedAt());

            // 대표 썸네일 (최근 추가된 레시피 최대 4개)
            List<Long> recipeIds = collectionRecipeRepository.findRecipeIdsByCollectionId(c.getId());
            List<String> thumbnails = new ArrayList<>();
            if (!recipeIds.isEmpty()) {
                List<Long> topIds = recipeIds.subList(0, Math.min(4, recipeIds.size()));
                List<Recipe> recipes = recipeRepository.findAllById(topIds);
                Map<Long, Recipe> recipeMap = recipes.stream().collect(Collectors.toMap(Recipe::getId, r -> r));
                for (Long rid : topIds) {
                    Recipe r = recipeMap.get(rid);
                    if (r != null && r.getImageUrl() != null) {
                        thumbnails.add(r.getImageUrl());
                    }
                }
            }
            item.put("thumbnails", thumbnails);
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    /** 컬렉션 생성 */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        }
        Long userId = (Long) auth.getPrincipal();
        String name = body.get("name");
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "컬렉션 이름을 입력해주세요"));
        }
        name = name.trim();
        if (collectionRepository.existsByUserIdAndName(userId, name)) {
            return ResponseEntity.badRequest().body(Map.of("error", "이미 같은 이름의 컬렉션이 있습니다"));
        }

        Collection c = new Collection();
        c.setUserId(userId);
        c.setName(name);
        c.setDescription(body.getOrDefault("description", ""));
        c.setEmoji(body.getOrDefault("emoji", "📁"));
        collectionRepository.save(c);
        return ResponseEntity.ok(c);
    }

    /** 컬렉션 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        }
        Long userId = (Long) auth.getPrincipal();
        Collection c = collectionRepository.findByIdAndUserId(id, userId).orElse(null);
        if (c == null) {
            return ResponseEntity.status(404).body(Map.of("error", "컬렉션을 찾을 수 없습니다"));
        }

        if (body.containsKey("name")) {
            String newName = body.get("name").trim();
            if (!newName.equals(c.getName()) && collectionRepository.existsByUserIdAndName(userId, newName)) {
                return ResponseEntity.badRequest().body(Map.of("error", "이미 같은 이름의 컬렉션이 있습니다"));
            }
            c.setName(newName);
        }
        if (body.containsKey("description")) c.setDescription(body.get("description"));
        if (body.containsKey("emoji")) c.setEmoji(body.get("emoji"));

        collectionRepository.save(c);
        return ResponseEntity.ok(c);
    }

    /** 컬렉션 삭제 */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        }
        Long userId = (Long) auth.getPrincipal();
        Collection c = collectionRepository.findByIdAndUserId(id, userId).orElse(null);
        if (c == null) {
            return ResponseEntity.status(404).body(Map.of("error", "컬렉션을 찾을 수 없습니다"));
        }
        collectionRecipeRepository.deleteByCollectionId(id);
        collectionRepository.delete(c);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    /** 컬렉션 내 레시피 목록 */
    @GetMapping("/{id}/recipes")
    public ResponseEntity<?> recipes(@PathVariable Long id, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        }
        Long userId = (Long) auth.getPrincipal();
        Collection c = collectionRepository.findByIdAndUserId(id, userId).orElse(null);
        if (c == null) {
            return ResponseEntity.status(404).body(Map.of("error", "컬렉션을 찾을 수 없습니다"));
        }

        List<Long> recipeIds = collectionRecipeRepository.findRecipeIdsByCollectionId(id);
        if (recipeIds.isEmpty()) {
            return ResponseEntity.ok(Map.of("collection", c, "recipes", List.of()));
        }
        List<Recipe> recipes = recipeRepository.findAllById(recipeIds);
        Map<Long, Recipe> recipeMap = recipes.stream().collect(Collectors.toMap(Recipe::getId, r -> r));
        List<Recipe> ordered = recipeIds.stream().map(recipeMap::get).filter(Objects::nonNull).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("collection", c, "recipes", ordered));
    }

    /** 컬렉션에 레시피 추가 */
    @PostMapping("/{id}/recipes/{recipeId}")
    @Transactional
    public ResponseEntity<?> addRecipe(@PathVariable Long id, @PathVariable Long recipeId, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        }
        Long userId = (Long) auth.getPrincipal();
        Collection c = collectionRepository.findByIdAndUserId(id, userId).orElse(null);
        if (c == null) {
            return ResponseEntity.status(404).body(Map.of("error", "컬렉션을 찾을 수 없습니다"));
        }
        if (collectionRecipeRepository.existsByCollectionIdAndRecipeId(id, recipeId)) {
            return ResponseEntity.ok(Map.of("added", true));
        }

        CollectionRecipe cr = new CollectionRecipe();
        cr.setCollectionId(id);
        cr.setRecipeId(recipeId);
        collectionRecipeRepository.save(cr);

        c.setRecipeCount(c.getRecipeCount() + 1);
        collectionRepository.save(c);
        return ResponseEntity.ok(Map.of("added", true));
    }

    /** 컬렉션에서 레시피 제거 */
    @DeleteMapping("/{id}/recipes/{recipeId}")
    @Transactional
    public ResponseEntity<?> removeRecipe(@PathVariable Long id, @PathVariable Long recipeId, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        }
        Long userId = (Long) auth.getPrincipal();
        Collection c = collectionRepository.findByIdAndUserId(id, userId).orElse(null);
        if (c == null) {
            return ResponseEntity.status(404).body(Map.of("error", "컬렉션을 찾을 수 없습니다"));
        }
        if (!collectionRecipeRepository.existsByCollectionIdAndRecipeId(id, recipeId)) {
            return ResponseEntity.ok(Map.of("removed", true));
        }

        collectionRecipeRepository.deleteByCollectionIdAndRecipeId(id, recipeId);
        c.setRecipeCount(Math.max(0, c.getRecipeCount() - 1));
        collectionRepository.save(c);
        return ResponseEntity.ok(Map.of("removed", true));
    }

    /** 특정 레시피가 어떤 컬렉션에 포함되어 있는지 확인 */
    @GetMapping("/check/{recipeId}")
    public ResponseEntity<?> checkRecipe(@PathVariable Long recipeId, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.ok(Map.of("collectionIds", List.of()));
        }
        Long userId = (Long) auth.getPrincipal();
        List<Collection> collections = collectionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (collections.isEmpty()) {
            return ResponseEntity.ok(Map.of("collectionIds", List.of()));
        }
        List<Long> collectionIds = collections.stream().map(Collection::getId).collect(Collectors.toList());
        List<Long> containingIds = collectionRecipeRepository.findCollectionIdsByRecipeIdAndCollectionIdIn(recipeId, collectionIds);
        return ResponseEntity.ok(Map.of("collectionIds", containingIds));
    }
}
