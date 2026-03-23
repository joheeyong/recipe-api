package com.luxrobo.recipeapi.repository;

import com.luxrobo.recipeapi.entity.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    List<BlogPost> findByRecipeIdOrderByCreatedAtDesc(Long recipeId);

    List<BlogPost> findAllByOrderByCreatedAtDesc();
}
