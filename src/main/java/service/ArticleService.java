package service;

import entite.Article;
import utils.DatabaseConnection;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ArticleService {
    private static final Logger logger = Logger.getLogger(ArticleService.class.getName());
    private final Connection connection;

    public ArticleService() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    // Helper method to generate slug from title
    private String generateSlug(String title) {
        if (title == null || title.isEmpty()) {
            return "";
        }
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "") // Remove all special characters except spaces and hyphens
                .replaceAll("\\s+", "-")         // Replace spaces with hyphens
                .replaceAll("-+", "-")           // Replace multiple hyphens with single hyphen
                .trim();
    }

    // 🔹 CREATE
    public void add(Article article) throws SQLException {
        if (article == null) {
            throw new IllegalArgumentException("Article cannot be null");
        }

        // Generate slug from title if not set
        if (article.getSlug() == null || article.getSlug().isEmpty()) {
            article.setSlug(generateSlug(article.getTitle()));
        }

        String query = "INSERT INTO article (title, content, featured_text, image, slug, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);

            ps.setString(1, article.getTitle());
            ps.setString(2, article.getContent());
            ps.setString(3, article.getFeaturedText());
            ps.setString(4, article.getImage());
            ps.setString(5, article.getSlug());
            ps.setTimestamp(6, article.getCreatedAt());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating article failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    article.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating article failed, no ID obtained.");
                }
            }

            connection.commit();
            logger.info("✅ Article added successfully with ID: " + article.getId());
        } catch (SQLException e) {
            connection.rollback();
            logger.severe("❌ Failed to add article: " + e.getMessage());
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // 🔹 READ ALL
    public List<Article> getAll() throws SQLException {
        List<Article> articles = new ArrayList<>();
        String query = "SELECT * FROM article ORDER BY created_at DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Article article = new Article(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("featured_text"),
                        rs.getString("image"),
                        rs.getString("slug"),
                        rs.getTimestamp("created_at")
                );
                articles.add(article);
            }
            logger.info("Retrieved " + articles.size() + " articles");
        } catch (SQLException e) {
            logger.severe("❌ Failed to retrieve articles: " + e.getMessage());
            throw e;
        }
        return articles;
    }

    // 🔹 UPDATE
    public void update(Article article) throws SQLException {
        if (article == null) {
            throw new IllegalArgumentException("Article cannot be null");
        }

        // Update slug if title has changed
        if (article.getSlug() == null || article.getSlug().isEmpty()) {
            article.setSlug(generateSlug(article.getTitle()));
        }

        String query = "UPDATE article SET title = ?, content = ?, featured_text = ?, image = ?, slug = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);

            ps.setString(1, article.getTitle());
            ps.setString(2, article.getContent());
            ps.setString(3, article.getFeaturedText());
            ps.setString(4, article.getImage());
            ps.setString(5, article.getSlug());
            ps.setInt(6, article.getId());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Updating article failed, no rows affected.");
            }

            connection.commit();
            logger.info("✅ Article updated successfully with ID: " + article.getId());
        } catch (SQLException e) {
            connection.rollback();
            logger.severe("❌ Failed to update article: " + e.getMessage());
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // 🔹 DELETE
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM article WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);

            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Deleting article failed, no rows affected.");
            }

            connection.commit();
            logger.info("🗑️ Article deleted successfully with ID: " + id);
        } catch (SQLException e) {
            connection.rollback();
            logger.severe("❌ Failed to delete article: " + e.getMessage());
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // 🔹 READ BY ID
    public Article getById(int id) throws SQLException {
        String query = "SELECT * FROM article WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Article article = new Article(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getString("featured_text"),
                            rs.getString("image"),
                            rs.getString("slug"),
                            rs.getTimestamp("created_at")
                    );
                    logger.info("Retrieved article with ID: " + id);
                    return article;
                }
            }
        } catch (SQLException e) {
            logger.severe("❌ Failed to retrieve article with ID " + id + ": " + e.getMessage());
            throw e;
        }
        return null;
    }

    public void updateArticle(Article currentArticle) {
    }

    private String serializeArticle(Article article) {
        // Tu peux utiliser ceci pour l'afficher localement, mais pas dans l'URL Facebook
        return "Date: " + article.getCreatedAt() +
                ", Content: " + article.getContent() +
                ", Title: " + article.getTitle();
    }

    public void shareGoogle(Article article) {
        String facebookUrl = "https://www.google.com/search?q=";

        String articleString = serializeArticle(article);

        try {
            String encodedContent = URLEncoder.encode(articleString, "UTF-8");
            facebookUrl += encodedContent;
            Desktop.getDesktop().browse(new URI(facebookUrl));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void sharePintrest(Article article) {
        String facebookUrl = "https://www.pinterest.com/pin/create/button/?url=";

        String articleString = serializeArticle(article);

        try {
            String encodedContent = URLEncoder.encode(articleString, "UTF-8");
            facebookUrl += encodedContent;
            Desktop.getDesktop().browse(new URI(facebookUrl));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
    public void shareFacebook(Article article) {
        // Construire une recherche Facebook avec le titre de l'article
        String baseSearchUrl = "https://www.facebook.com/search/top?q=";
        String query = article.getTitle(); // ou autre champ

        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String finalUrl = baseSearchUrl + encodedQuery;
            Desktop.getDesktop().browse(new URI(finalUrl));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }



}