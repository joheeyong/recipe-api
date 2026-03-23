package com.luxrobo.recipeapi.controller;

import com.luxrobo.recipeapi.entity.UserPreference;
import com.luxrobo.recipeapi.repository.UserPreferenceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/preferences")
public class UserPreferenceController {

    private final UserPreferenceRepository preferenceRepository;

    public UserPreferenceController(UserPreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    @GetMapping
    public ResponseEntity<?> get(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        Long userId = (Long) auth.getPrincipal();
        UserPreference pref = preferenceRepository.findByUserId(userId)
                .orElse(new UserPreference(userId));
        return ResponseEntity.ok(pref);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody UserPreference body, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        Long userId = (Long) auth.getPrincipal();
        UserPreference pref = preferenceRepository.findByUserId(userId)
                .orElse(new UserPreference(userId));

        pref.setSpicyLevel(body.getSpicyLevel());
        pref.setSweetnessLevel(body.getSweetnessLevel());
        pref.setSaltinessLevel(body.getSaltinessLevel());
        pref.setSournessLevel(body.getSournessLevel());
        pref.setUmamiLevel(body.getUmamiLevel());
        pref.setOilinessLevel(body.getOilinessLevel());
        pref.setDietaryRestrictions(body.getDietaryRestrictions());
        pref.setDislikedIngredients(body.getDislikedIngredients());
        pref.setPreferredCuisines(body.getPreferredCuisines());
        pref.setCookingSkill(body.getCookingSkill());
        preferenceRepository.save(pref);

        return ResponseEntity.ok(pref);
    }
}
