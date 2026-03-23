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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/my-recipes")
public class UserRecipeController {

    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository ingredientRepository;
    private final RecipeStepRepository stepRepository;

    @Value("${upload.dir:./uploads}")
    private String uploadDir;

    public UserRecipeController(RecipeRepository recipeRepository,
                                 RecipeIngredientRepository ingredientRepository,
                                 RecipeStepRepository stepRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.stepRepository = stepRepository;
    }

    /** 내가 등록한 레시피 목록 */
    @GetMapping
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
