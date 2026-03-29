package com.luxrobo.recipeapi.repository;

import com.luxrobo.recipeapi.entity.CollectionRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CollectionRecipeRepository extends JpaRepository<CollectionRecipe, Long> {

    List<CollectionRecipe> findByCollectionIdOrderByAddedAtDesc(Long collectionId);

    @Query("SELECT cr.recipeId FROM CollectionRecipe cr WHERE cr.collectionId = :collectionId ORDER BY cr.addedAt DESC")
    List<Long> findRecipeIdsByCollectionId(@Param("collectionId") Long collectionId);

    boolean existsByCollectionIdAndRecipeId(Long collectionId, Long recipeId);

    void deleteByCollectionIdAndRecipeId(Long collectionId, Long recipeId);

    void deleteByCollectionId(Long collectionId);

    @Query("SELECT cr.collectionId FROM CollectionRecipe cr WHERE cr.recipeId = :recipeId AND cr.collectionId IN :collectionIds")
    List<Long> findCollectionIdsByRecipeIdAndCollectionIdIn(@Param("recipeId") Long recipeId, @Param("collectionIds") List<Long> collectionIds);
}
