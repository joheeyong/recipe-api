package com.luxrobo.recipeapi.controller;

import com.luxrobo.recipeapi.entity.BlogMedia;
import com.luxrobo.recipeapi.entity.BlogPost;
import com.luxrobo.recipeapi.entity.Recipe;
import com.luxrobo.recipeapi.entity.User;
import com.luxrobo.recipeapi.repository.BlogMediaRepository;
import com.luxrobo.recipeapi.repository.BlogPostRepository;
import com.luxrobo.recipeapi.repository.RecipeRepository;
import com.luxrobo.recipeapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/blog")
public class BlogController {

    private final BlogPostRepository blogPostRepository;
    private final BlogMediaRepository blogMediaRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;

    @Value("${upload.dir:./uploads}")
    private String uploadDir;

    public BlogController(BlogPostRepository blogPostRepository,
                          BlogMediaRepository blogMediaRepository,
                          UserRepository userRepository,
                          RecipeRepository recipeRepository) {
        this.blogPostRepository = blogPostRepository;
        this.blogMediaRepository = blogMediaRepository;
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
    }

    /** 특정 레시피의 블로그 글 목록 */
    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<?> listByRecipe(@PathVariable Long recipeId) {
        List<BlogPost> posts = blogPostRepository.findByRecipeIdOrderByCreatedAtDesc(recipeId);
        enrichPosts(posts);
        return ResponseEntity.ok(posts);
    }

    /** 블로그 글 상세 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        Optional<BlogPost> post = blogPostRepository.findById(id);
        if (post.isEmpty()) return ResponseEntity.notFound().build();
        enrichPosts(List.of(post.get()));
        return ResponseEntity.ok(post.get());
    }

    /** 블로그 글 작성 (multipart: files + JSON fields) */
    @PostMapping
    public ResponseEntity<?> create(
            @RequestParam("recipeId") Long recipeId,
            @RequestParam("content") String content,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        }
        Long userId = (Long) auth.getPrincipal();

        // 레시피 존재 확인
        if (!recipeRepository.existsById(recipeId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "존재하지 않는 레시피입니다"));
        }

        BlogPost post = new BlogPost();
        post.setUserId(userId);
        post.setRecipeId(recipeId);
        post.setContent(content);
        blogPostRepository.save(post);

        // 파일 업로드
        if (files != null && !files.isEmpty()) {
            saveMedia(post.getId(), files);
        }

        enrichPosts(List.of(post));
        return ResponseEntity.ok(post);
    }

    /** 블로그 글 삭제 (본인만) */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        }
        Long userId = (Long) auth.getPrincipal();
        Optional<BlogPost> post = blogPostRepository.findById(id);
        if (post.isEmpty()) return ResponseEntity.notFound().build();
        if (!post.get().getUserId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "본인의 글만 삭제할 수 있습니다"));
        }
        blogMediaRepository.deleteByBlogPostId(id);
        blogPostRepository.delete(post.get());
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** 미디어 파일 업로드 처리 */
    private void saveMedia(Long blogPostId, List<MultipartFile> files) {
        Path uploadPath = Paths.get(uploadDir, "blog");
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("업로드 디렉토리 생성 실패", e);
        }

        int idx = 0;
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String originalName = file.getOriginalFilename();
            String ext = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : "";
            String fileName = UUID.randomUUID() + ext;

            try {
                file.transferTo(uploadPath.resolve(fileName).toFile());
            } catch (IOException e) {
                continue;
            }

            String contentType = file.getContentType();
            String mediaType = contentType != null && contentType.startsWith("video") ? "video" : "image";

            BlogMedia media = new BlogMedia();
            media.setBlogPostId(blogPostId);
            media.setMediaUrl("/uploads/blog/" + fileName);
            media.setMediaType(mediaType);
            media.setOrderIndex(idx++);
            blogMediaRepository.save(media);
        }
    }

    /** 블로그 글에 사용자 정보, 미디어 URL 채우기 */
    private void enrichPosts(List<BlogPost> posts) {
        if (posts.isEmpty()) return;

        // 사용자 정보
        Set<Long> userIds = posts.stream().map(BlogPost::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = new HashMap<>();
        userRepository.findAllById(userIds).forEach(u -> userMap.put(u.getId(), u));

        // 레시피 이름
        Set<Long> recipeIds = posts.stream().map(BlogPost::getRecipeId).collect(Collectors.toSet());
        Map<Long, String> recipeNameMap = new HashMap<>();
        recipeRepository.findAllById(recipeIds).forEach(r -> recipeNameMap.put(r.getId(), r.getTitle()));

        // 미디어
        List<Long> postIds = posts.stream().map(BlogPost::getId).collect(Collectors.toList());
        List<BlogMedia> allMedia = blogMediaRepository.findByBlogPostIdInOrderByOrderIndex(postIds);
        Map<Long, List<String>> mediaMap = new HashMap<>();
        for (BlogMedia m : allMedia) {
            mediaMap.computeIfAbsent(m.getBlogPostId(), k -> new ArrayList<>()).add(m.getMediaUrl());
        }

        for (BlogPost post : posts) {
            User u = userMap.get(post.getUserId());
            if (u != null) {
                post.setUserName(u.getName());
                post.setUserProfileImage(u.getProfileImage());
            }
            post.setRecipeName(recipeNameMap.get(post.getRecipeId()));
            post.setMediaUrls(mediaMap.getOrDefault(post.getId(), List.of()));
        }
    }
}
