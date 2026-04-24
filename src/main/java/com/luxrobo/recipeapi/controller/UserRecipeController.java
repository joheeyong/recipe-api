package com.luxrobo.recipeapi.controller;

import com.luxrobo.recipeapi.entity.Recipe;
import com.luxrobo.recipeapi.entity.RecipeIngredient;
import com.luxrobo.recipeapi.entity.RecipeStep;
import com.luxrobo.recipeapi.repository.RecipeIngredientRepository;
import com.luxrobo.recipeapi.repository.RecipeRepository;
import com.luxrobo.recipeapi.repository.RecipeStepRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.luxrobo.recipeapi.entity.User;
import com.luxrobo.recipeapi.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/my-recipes")
public class UserRecipeController {

    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository ingredientRepository;
    private final RecipeStepRepository stepRepository;
    private final UserRepository userRepository;

    @Value("${upload.dir:./uploads}")
    private String uploadDir;

    public UserRecipeController(RecipeRepository recipeRepository,
                                 RecipeIngredientRepository ingredientRepository,
                                 RecipeStepRepository stepRepository,
                                 UserRepository userRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.stepRepository = stepRepository;
        this.userRepository = userRepository;
    }

    /** 모든 사용자 레시피 목록 (공개) */
    @GetMapping("/all")
    public ResponseEntity<?> allUserRecipes() {
        List<Recipe> recipes = recipeRepository.findByUserIdIsNotNullOrderByCreatedAtDesc();

        // 작성자 이름 매핑
        Set<Long> userIds = recipes.stream().map(Recipe::getUserId).collect(Collectors.toSet());
        Map<Long, String> userNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userRepository.findAllById(userIds).forEach(u -> userNameMap.put(u.getId(), u.getName()));
        }

        List<Map<String, Object>> result = recipes.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("title", r.getTitle());
            m.put("description", r.getDescription());
            m.put("category", r.getCategory());
            m.put("difficulty", r.getDifficulty());
            m.put("cookTimeMinutes", r.getCookTimeMinutes());
            m.put("imageUrl", r.getImageUrl());
            m.put("userId", r.getUserId());
            m.put("userName", userNameMap.getOrDefault(r.getUserId(), "익명"));
            m.put("createdAt", r.getCreatedAt());
            m.put("updatedAt", r.getUpdatedAt());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /** 내가 등록한 레시피 목록 */
    @GetMapping("/mine")
    public ResponseEntity<?> myList(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        }
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(recipeRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    /** 레시피 등록 */
    @PostMapping
    @Transactional
    public ResponseEntity<?> create(
            @RequestParam("title") String title,
            @RequestParam(value = "description", defaultValue = "") String description,
            @RequestParam(value = "category", defaultValue = "main") String category,
            @RequestParam(value = "difficulty", defaultValue = "2") int difficulty,
            @RequestParam(value = "cookTimeMinutes", defaultValue = "0") int cookTimeMinutes,
            @RequestParam(value = "servingSize", defaultValue = "2") int servingSize,
            @RequestParam(value = "calories", defaultValue = "0") int calories,
            @RequestParam(value = "spicyLevel", defaultValue = "0") int spicyLevel,
            @RequestParam(value = "tags", defaultValue = "") String tags,
            @RequestParam(value = "ingredientNames") List<String> ingredientNames,
            @RequestParam(value = "ingredientAmounts") List<String> ingredientAmounts,
            @RequestParam(value = "stepInstructions") List<String> stepInstructions,
            @RequestParam(value = "stepTips", required = false) List<String> stepTips,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Authentication auth) {

        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        }
        Long userId = (Long) auth.getPrincipal();

        // 레시피 저장
        Recipe recipe = new Recipe();
        recipe.setUserId(userId);
        recipe.setTitle(title);
        recipe.setDescription(description);
        recipe.setCuisine("korean");
        recipe.setCategory(category);
        recipe.setDifficulty(difficulty);
        recipe.setCookTimeMinutes(cookTimeMinutes);
        recipe.setServingSize(servingSize);
        recipe.setCalories(calories);
        recipe.setSpicyLevel(spicyLevel);
        recipe.setTags(tags);

        // 대표 이미지 업로드
        if (image != null && !image.isEmpty()) {
            String imageUrl = saveImage(image);
            recipe.setImageUrl(imageUrl);
        }

        recipeRepository.save(recipe);

        // 재료 저장
        for (int i = 0; i < ingredientNames.size(); i++) {
            String name = ingredientNames.get(i).trim();
            if (name.isEmpty()) continue;
            RecipeIngredient ing = new RecipeIngredient();
            ing.setRecipeId(recipe.getId());
            ing.setName(name);
            ing.setAmount(i < ingredientAmounts.size() ? ingredientAmounts.get(i).trim() : "");
            ing.setOptional(false);
            ingredientRepository.save(ing);
        }

        // 조리법 저장
        for (int i = 0; i < stepInstructions.size(); i++) {
            String instruction = stepInstructions.get(i).trim();
            if (instruction.isEmpty()) continue;
            RecipeStep step = new RecipeStep();
            step.setRecipeId(recipe.getId());
            step.setStepNumber(i + 1);
            step.setInstruction(instruction);
            if (stepTips != null && i < stepTips.size() && !stepTips.get(i).trim().isEmpty()) {
                step.setTip(stepTips.get(i).trim());
            }
            stepRepository.save(step);
        }

        return ResponseEntity.ok(recipe);
    }

    /** 내 레시피 수정 */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam(value = "description", defaultValue = "") String description,
            @RequestParam(value = "category", defaultValue = "main") String category,
            @RequestParam(value = "difficulty", defaultValue = "2") int difficulty,
            @RequestParam(value = "cookTimeMinutes", defaultValue = "0") int cookTimeMinutes,
            @RequestParam(value = "servingSize", defaultValue = "2") int servingSize,
            @RequestParam(value = "calories", defaultValue = "0") int calories,
            @RequestParam(value = "spicyLevel", defaultValue = "0") int spicyLevel,
            @RequestParam(value = "tags", defaultValue = "") String tags,
            @RequestParam(value = "ingredientNames") List<String> ingredientNames,
            @RequestParam(value = "ingredientAmounts") List<String> ingredientAmounts,
            @RequestParam(value = "stepInstructions") List<String> stepInstructions,
            @RequestParam(value = "stepTips", required = false) List<String> stepTips,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Authentication auth) {

        if (auth == null) return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        Long userId = (Long) auth.getPrincipal();

        Optional<Recipe> existing = recipeRepository.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        if (!userId.equals(existing.get().getUserId())) {
            return ResponseEntity.status(403).body(Map.of("error", "본인의 레시피만 수정할 수 있습니다"));
        }

        Recipe recipe = existing.get();
        recipe.setTitle(title);
        recipe.setDescription(description);
        recipe.setCategory(category);
        recipe.setDifficulty(difficulty);
        recipe.setCookTimeMinutes(cookTimeMinutes);
        recipe.setServingSize(servingSize);
        recipe.setCalories(calories);
        recipe.setSpicyLevel(spicyLevel);
        recipe.setTags(tags);

        if (image != null && !image.isEmpty()) {
            recipe.setImageUrl(saveImage(image));
        }
        recipeRepository.save(recipe);

        // 재료/조리법 교체
        ingredientRepository.deleteByRecipeId(id);
        for (int i = 0; i < ingredientNames.size(); i++) {
            String name = ingredientNames.get(i).trim();
            if (name.isEmpty()) continue;
            RecipeIngredient ing = new RecipeIngredient();
            ing.setRecipeId(id);
            ing.setName(name);
            ing.setAmount(i < ingredientAmounts.size() ? ingredientAmounts.get(i).trim() : "");
            ing.setOptional(false);
            ingredientRepository.save(ing);
        }

        stepRepository.deleteByRecipeId(id);
        for (int i = 0; i < stepInstructions.size(); i++) {
            String instruction = stepInstructions.get(i).trim();
            if (instruction.isEmpty()) continue;
            RecipeStep step = new RecipeStep();
            step.setRecipeId(id);
            step.setStepNumber(i + 1);
            step.setInstruction(instruction);
            if (stepTips != null && i < stepTips.size() && !stepTips.get(i).trim().isEmpty()) {
                step.setTip(stepTips.get(i).trim());
            }
            stepRepository.save(step);
        }

        return ResponseEntity.ok(recipe);
    }

    /** 내 레시피 삭제 */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        }
        Long userId = (Long) auth.getPrincipal();
        Optional<Recipe> recipe = recipeRepository.findById(id);
        if (recipe.isEmpty()) return ResponseEntity.notFound().build();
        if (!userId.equals(recipe.get().getUserId())) {
            return ResponseEntity.status(403).body(Map.of("error", "본인의 레시피만 삭제할 수 있습니다"));
        }

        ingredientRepository.deleteByRecipeId(id);
        stepRepository.deleteByRecipeId(id);
        recipeRepository.delete(recipe.get());
        return ResponseEntity.ok(Map.of("success", true));
    }

    private String saveImage(MultipartFile file) {
        Path uploadPath = Paths.get(uploadDir, "recipes");
        try {
            Files.createDirectories(uploadPath);
            String ext = "";
            String orig = file.getOriginalFilename();
            if (orig != null && orig.contains(".")) {
                ext = orig.substring(orig.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID() + ext;
            file.transferTo(uploadPath.resolve(fileName).toFile());
            return "/uploads/recipes/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }
}
