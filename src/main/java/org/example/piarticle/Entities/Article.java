package org.example.piarticle.Entities;

import java.sql.Timestamp;

public class Article {
    private int id;
    private String title;
    private String content;
    private String featuredText;
    private String image;
    private String slug;
    private Timestamp createdAt;

    // Default constructor
    public Article() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    // Constructor with all fields
    public Article(int id, String title, String content, String featuredText, String image, String slug, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.featuredText = featuredText;
        this.image = image;
        this.slug = slug;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFeaturedText() {
        return featuredText;
    }

    public void setFeaturedText(String featuredText) {
        this.featuredText = featuredText;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // Alias for featuredText to maintain compatibility with database column name
    public String getFeatured_text() {
        return featuredText;
    }

    public void setFeatured_text(String featuredText) {
        this.featuredText = featuredText;
    }
}
