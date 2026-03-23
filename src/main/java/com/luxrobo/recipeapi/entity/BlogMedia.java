package com.luxrobo.recipeapi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "blog_media")
public class BlogMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blog_post_id", nullable = false)
    private Long blogPostId;

    @Column(name = "media_url", length = 500, nullable = false)
    private String mediaUrl;

    @Column(name = "media_type", length = 20)
    private String mediaType; // "image" or "video"

    @Column(name = "order_index")
    private int orderIndex;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBlogPostId() { return blogPostId; }
    public void setBlogPostId(Long blogPostId) { this.blogPostId = blogPostId; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}
