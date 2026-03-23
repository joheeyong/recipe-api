package com.luxrobo.recipeapi.controller;

import com.luxrobo.recipeapi.entity.Review;
import com.luxrobo.recipeapi.entity.User;
import com.luxrobo.recipeapi.repository.ReviewRepository;
import com.luxrobo.recipeapi.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/recipes/{recipeId}/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ReviewController(ReviewRepository reviewRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    /** 리뷰 목록 조회 (비로그인도 가능) */
    @GetMapping
    public ResponseEntity<?> list(@PathVariable Long recipeId) {
        List<Review> reviews = reviewRepository.findByRecipeIdOrderByCreatedAtDesc(recipeId);

        // 사용자 정보 채우기
        Set<Long> userIds = new HashSet<>();
        for (Review r : reviews) userIds.add(r.getUserId());
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userRepository.findAllById(userIds).forEach(u -> userMap.put(u.getId(), u));
        }
        for (Review r : reviews) {
            User u = userMap.get(r.getUserId());
            if (u != null) {
                r.setUserName(u.getName());
                r.setUserProfileImage(u.getProfileImage());
            }
        }

        double avg = reviewRepository.avgRatingByRecipeId(recipeId);
        long count = reviewRepository.countByRecipeId(recipeId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reviews", reviews);
        result.put("avgRating", Math.round(avg * 10.0) / 10.0);
        result.put("reviewCount", count);
        return ResponseEntity.ok(result);
    }

    /** 리뷰 작성 (로그인 필수, 레시피당 1개) */
    @PostMapping
    public ResponseEntity<?> create(@PathVariable Long recipeId,
                                     @RequestBody Map<String, Object> body,
                                     Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        }
        Long userId = (Long) auth.getPrincipal();

        int rating = ((Number) body.get("rating")).intValue();
        if (rating < 1 || rating > 5) {
            return ResponseEntity.badRequest().body(Map.of("error", "별점은 1~5 사이여야 합니다"));
        }
        String comment = (String) body.getOrDefault("comment", "");

        // 이미 리뷰가 있으면 수정
        Optional<Review> existing = reviewRepository.findByUserIdAndRecipeId(userId, recipeId);
        Review review;
        if (existing.isPresent()) {
            review = existing.get();
            review.setRating(rating);
            review.setComment(comment);
        } else {
            review = new Review();
            review.setUserId(userId);
            review.setRecipeId(recipeId);
            review.setRating(rating);
            review.setComment(comment);
        }
        reviewRepository.save(review);

        // 사용자 정보 채우기
        userRepository.findById(userId).ifPresent(u -> {
            review.setUserName(u.getName());
            review.setUserProfileImage(u.getProfileImage());
        });

        return ResponseEntity.ok(review);
    }

    /** 리뷰 삭제 (본인 리뷰만) */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> delete(@PathVariable Long recipeId,
                                     @PathVariable Long reviewId,
                                     Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        }
        Long userId = (Long) auth.getPrincipal();

        Optional<Review> review = reviewRepository.findById(reviewId);
        if (review.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!review.get().getUserId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "본인의 리뷰만 삭제할 수 있습니다"));
        }
        reviewRepository.delete(review.get());
        return ResponseEntity.ok(Map.of("success", true));
    }
}
