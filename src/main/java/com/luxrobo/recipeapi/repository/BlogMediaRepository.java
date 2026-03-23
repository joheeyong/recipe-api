package com.luxrobo.recipeapi.repository;

import com.luxrobo.recipeapi.entity.BlogMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogMediaRepository extends JpaRepository<BlogMedia, Long> {

    List<BlogMedia> findByBlogPostIdOrderByOrderIndex(Long blogPostId);

    List<BlogMedia> findByBlogPostIdInOrderByOrderIndex(List<Long> blogPostIds);

    void deleteByBlogPostId(Long blogPostId);
}
