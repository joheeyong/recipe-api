package com.luxrobo.recipeapi.controller;

import com.luxrobo.recipeapi.entity.Notification;
import com.luxrobo.recipeapi.entity.Review;
import com.luxrobo.recipeapi.entity.ReviewLike;
import com.luxrobo.recipeapi.entity.User;
import com.luxrobo.recipeapi.repository.NotificationRepository;
import com.luxrobo.recipeapi.repository.RecipeRepository;
import com.luxrobo.recipeapi.repository.ReviewLikeRepository;
import com.luxrobo.recipeapi.repository.ReviewRepository;
import com.luxrobo.recipeapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recipes/{recipeId}/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final NotificationRepository notificationRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    @Value("${upload.dir:./uploads}")
    private String uploadDir;

    public ReviewController(ReviewRepository reviewRepository, UserRepository userRepository,
                             RecipeRepository recipeRepository, NotificationRepository notificationRepository,
                             ReviewLikeRepository reviewLikeRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
        this.notificationRepository = notificationRepository;
        this.reviewLikeRepository = reviewLikeRepository;
    }

    /** 리뷰 목록 조회 (비로그인도 가능) */
    @GetMapping
    public ResponseEntity<?> list(@PathVariable Long recipeId, Authentication auth) {
        List<Review> reviews = reviewRepository.findByRecipeIdOrderByCreatedAtDesc(recipeId);

        Set<Long> userIds = new HashSet<>();
        for (Review r : reviews) userIds.add(r.getUserId());
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userRepository.findAllById(userIds).forEach(u -> userMap.put(u.getId(), u));
        }

        // 좋아요 수 + 내가 좋아요 눌렀는지
        List<Long> reviewIds = new ArrayList<>();
        for (Review r : reviews) reviewIds.add(r.getId());
        Set<Long> likedByMe = new HashSet<>();
        if (auth != null && !reviewIds.isEmpty()) {
            Long userId = (Long) auth.getPrincipal();
            reviewLikeRepository.findByReviewIdInAndUserId(reviewIds, userId)
                .forEach(l -> likedByMe.add(l.getReviewId()));
        }

        for (Review r : reviews) {
            User u = userMap.get(r.getUserId());
            if (u != null) {
                r.setUserName(u.getName());
                r.setUserProfileImage(u.getProfileImage());
            }
            r.setLikeCount(reviewLikeRepository.countByReviewId(r.getId()));
            r.setLikedByMe(likedByMe.contains(r.getId()));
        }

        double avg = reviewRepository.avgRatingByRecipeId(recipeId);
        long count = reviewRepository.countByRecipeId(recipeId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reviews", reviews);
        result.put("avgRating", Math.round(avg * 10.0) / 10.0);
        result.put("reviewCount", count);
        return ResponseEntity.ok(result);
    }

    /** 리뷰 좋아요 토글 */
    @PostMapping("/{reviewId}/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long recipeId,
                                         @PathVariable Long reviewId,
                                         Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        }
        Long userId = (Long) auth.getPrincipal();

        if (!reviewRepository.existsById(reviewId)) {
            return ResponseEntity.notFound().build();
        }

        return reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId)
            .map(like -> {
                reviewLikeRepository.delete(like);
                long count = reviewLikeRepository.countByReviewId(reviewId);
                return ResponseEntity.ok(Map.of("liked", false, "likeCount", count));
            })
            .orElseGet(() -> {
                ReviewLike like = new ReviewLike();
                like.setReviewId(reviewId);
                like.setUserId(userId);
                reviewLikeRepository.save(like);
                long count = reviewLikeRepository.countByReviewId(reviewId);
                return ResponseEntity.ok(Map.of("liked", true, "likeCount", count));
            });
    }

    /** 리뷰 작성/수정 (로그인 필수, 레시피당 1개) */
    @PostMapping(consumes = {"multipart/form-data", "application/x-www-form-urlencoded", "application/json"})
    public ResponseEntity<?> create(@PathVariable Long recipeId,
                                     @RequestParam(required = false) Integer rating,
                                     @RequestParam(required = false) String comment,
                                     @RequestParam(required = false) MultipartFile image,
                                     Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        }
        if (rating == null || rating < 1 || rating > 5) {
            return ResponseEntity.badRequest().body(Map.of("error", "별점은 1~5 사이여야 합니다"));
        }
        Long userId = (Long) auth.getPrincipal();

        Optional<Review> existing = reviewRepository.findByUserIdAndRecipeId(userId, recipeId);
        Review review = existing.orElseGet(Review::new);
        if (existing.isEmpty()) {
            review.setUserId(userId);
            review.setRecipeId(recipeId);
        }
        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : "");

        if (image != null && !image.isEmpty()) {
            String imageUrl = saveImage(image);
            review.setImageUrl(imageUrl);
        }

        boolean isNew = existing.isEmpty();
        reviewRepository.save(review);

        userRepository.findById(userId).ifPresent(u -> {
            review.setUserName(u.getName());
            review.setUserProfileImage(u.getProfileImage());
        });

        // 새 리뷰일 때만 레시피 소유자에게 알림 (자기 레시피 제외)
        if (isNew) {
            recipeRepository.findById(recipeId).ifPresent(recipe -> {
                if (recipe.getUserId() != null && !recipe.getUserId().equals(userId)) {
                    String actorName = userRepository.findById(userId)
                        .map(User::getName).orElse("누군가");
                    Notification noti = new Notification();
                    noti.setUserId(recipe.getUserId());
                    noti.setType("REVIEW");
                    noti.setRecipeId(recipeId);
                    noti.setRecipeTitle(recipe.getTitle());
                    noti.setActorName(actorName);
                    notificationRepository.save(noti);
                }
            });
        }

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

    private String saveImage(MultipartFile file) {
        Path uploadPath = Paths.get(uploadDir, "reviews");
        try {
            Files.createDirectories(uploadPath);
            String ext = "";
            String orig = file.getOriginalFilename();
            if (orig != null && orig.contains(".")) {
                ext = orig.substring(orig.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID() + ext;
            file.transferTo(uploadPath.resolve(fileName).toFile());
            return "/uploads/reviews/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }
}
