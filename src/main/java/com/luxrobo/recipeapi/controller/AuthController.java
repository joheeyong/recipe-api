package com.luxrobo.recipeapi.controller;

import com.luxrobo.recipeapi.entity.User;
import com.luxrobo.recipeapi.repository.UserRepository;
import com.luxrobo.recipeapi.service.OAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final OAuthService oAuthService;
    private final UserRepository userRepository;

    public AuthController(OAuthService oAuthService, UserRepository userRepository) {
        this.oAuthService = oAuthService;
        this.userRepository = userRepository;
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
        try {
            Map<String, Object> result = oAuthService.googleLogin(
                body.get("code"), body.get("redirectUri"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/naver")
    public ResponseEntity<?> naverLogin(@RequestBody Map<String, String> body) {
        try {
            Map<String, Object> result = oAuthService.naverLogin(
                body.get("code"), body.get("state"), body.get("redirectUri"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> body) {
        try {
            Map<String, Object> result = oAuthService.kakaoLogin(
                body.get("code"), body.get("redirectUri"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        Long userId = (Long) auth.getPrincipal();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not found"));
        }
        return ResponseEntity.ok(user);
    }
}
