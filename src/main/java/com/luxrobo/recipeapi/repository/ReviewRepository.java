package com.luxrobo.recipeapi.repository;

import com.luxrobo.recipeapi.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRecipeIdOrderByCreatedAtDesc(Long recipeId);

    Optional<Review> findByUserIdAndRecipeId(Long userId, Long recipeId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.recipeId = :recipeId")
    double avgRatingByRecipeId(@Param("recipeId") Long recipeId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.recipeId = :recipeId")
    long countByRecipeId(@Param("recipeId") Long recipeId);
}
