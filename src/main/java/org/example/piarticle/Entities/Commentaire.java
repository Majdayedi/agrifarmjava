package org.example.piarticle.Entities;

import java.sql.Timestamp;

public class Commentaire {
    private Integer id;
    private Integer articleId;
    private Integer rate;
    private String commentaire;
    private Timestamp createdAt;

    // Constructor with all fields
    public Commentaire(Integer id, Integer articleId, Integer rate, String commentaire, Timestamp createdAt) {
        this.id = id;
        this.articleId = articleId;
        this.rate = rate;
        this.commentaire = commentaire;
        this.createdAt = createdAt;
    }

    // Constructor without id (for new comments)
    public Commentaire(Integer articleId, Integer rate, String commentaire) {
        this.articleId = articleId;
        this.rate = rate;
        this.commentaire = commentaire;
    }

    public Commentaire() {

    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getArticleId() {
        return articleId;
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        if (rate < 1 || rate > 5) {
            throw new IllegalArgumentException("Rate must be between 1 and 5");
        }
        this.rate = rate;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        if (commentaire == null || commentaire.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }
        this.commentaire = commentaire;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Commentaire{" +
                "id=" + id +
                ", articleId=" + articleId +
                ", rate=" + rate +
                ", commentaire='" + commentaire + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
