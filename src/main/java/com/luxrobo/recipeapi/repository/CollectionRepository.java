package com.luxrobo.recipeapi.repository;

import com.luxrobo.recipeapi.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    List<Collection> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Collection> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndName(Long userId, String name);
}
