package com.luxrobo.recipeapi.repository;

import com.luxrobo.recipeapi.entity.UserRecipeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRecipeHistoryRepository extends JpaRepository<UserRecipeHistory, Long> {

    List<UserRecipeHistory> findByUserIdAndAction(Long userId, String action);

    @Query("SELECT h.recipeId FROM UserRecipeHistory h WHERE h.userId = :userId")
    List<Long> findRecipeIdsByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndRecipeIdAndAction(Long userId, Long recipeId, String action);

    void deleteByUserIdAndRecipeIdAndAction(Long userId, Long recipeId, String action);

    @Query("SELECT h.recipeId FROM UserRecipeHistory h WHERE h.userId = :userId AND h.action = :action ORDER BY h.createdAt DESC")
    List<Long> findRecipeIdsByUserIdAndAction(@Param("userId") Long userId, @Param("action") String action);
}
