package com.luxrobo.recipeapi.repository;

import com.luxrobo.recipeapi.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    long countByReviewId(Long reviewId);
    Optional<ReviewLike> findByReviewIdAndUserId(Long reviewId, Long userId);
    List<ReviewLike> findByReviewIdInAndUserId(List<Long> reviewIds, Long userId);
}
