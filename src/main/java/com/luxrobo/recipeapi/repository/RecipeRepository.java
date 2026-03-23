package com.luxrobo.recipeapi.repository;

import com.luxrobo.recipeapi.entity.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Query("SELECT r FROM Recipe r WHERE " +
           "(:query IS NULL OR r.title LIKE %:query% OR r.description LIKE %:query% OR r.tags LIKE %:query%) AND " +
           "(:cuisine IS NULL OR r.cuisine = :cuisine) AND " +
           "(:category IS NULL OR r.category = :category) AND " +
           "(:difficulty IS NULL OR r.difficulty = :difficulty)")
    Page<Recipe> search(@Param("query") String query,
                        @Param("cuisine") String cuisine,
                        @Param("category") String category,
                        @Param("difficulty") Integer difficulty,
                        Pageable pageable);

    List<Recipe> findByCuisine(String cuisine);

    List<Recipe> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT r FROM Recipe r WHERE r.id NOT IN :excludeIds ORDER BY r.createdAt DESC")
    List<Recipe> findExcluding(@Param("excludeIds") List<Long> excludeIds, Pageable pageable);
}
