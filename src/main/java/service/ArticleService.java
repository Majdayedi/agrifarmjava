package service;

import utils.Connections;
import entite.Article;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ArticleService {
    private static final Logger logger = Logger.getLogger(ArticleService.class.getName());
    private final Connection connection;

    public ArticleService() throws SQLException {
        this.connection = Connections.getConnection();
    }

    // Check if slug exists in database
    private boolean slugExists(String slug) throws SQLException {
        String query = "SELECT COUNT(*) FROM article WHERE slug = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, slug);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Helper method to generate unique slug from title
    private String generateSlug(String title) throws SQLException {
        if (title == null || title.isEmpty()) {
            return "";
        }
        
        // Create base slug
        String baseSlug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "") // Remove all special characters except spaces and hyphens
                .replaceAll("\\s+", "-")         // Replace spaces with hyphens
                .replaceAll("-+", "-")           // Replace multiple hyphens with single hyphen
                .trim();
        
        // If base slug is empty after cleanup, use a default
        if (baseSlug.isEmpty()) {
            baseSlug = "article";
        }
        
        // Check if the slug already exists
        String uniqueSlug = baseSlug;
        int counter = 1;
        
        while (slugExists(uniqueSlug)) {
            // Append counter to make slug unique
            uniqueSlug = baseSlug + "-" + counter;
            counter++;
        }
        
        return uniqueSlug;
    }

    // 🔹 CREATE
    public void add(Article article) throws SQLException {
        if (article == null) {
            throw new IllegalArgumentException("Article cannot be null");
        }

        // Generate slug from title if not set
        if (article.getSlug() == null || article.getSlug().isEmpty()) {
            article.setSlug(generateSlug(article.getTitle()));
        } else {
            // Even if slug is provided, ensure it's unique
            String providedSlug = article.getSlug();
            if (slugExists(providedSlug)) {
                // Generate a unique version of the provided slug
                String baseSlug = providedSlug;
                int counter = 1;
                String uniqueSlug = baseSlug;
                
                while (slugExists(uniqueSlug)) {
                    uniqueSlug = baseSlug + "-" + counter;
                    counter++;
                }
                
                article.setSlug(uniqueSlug);
            }
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
        } else {
            // Check if there's another article with this slug (not this one)
            String query = "SELECT COUNT(*) FROM article WHERE slug = ? AND id != ?";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, article.getSlug());
                ps.setInt(2, article.getId());
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        // Another article has this slug, generate a unique one
                        article.setSlug(generateSlug(article.getTitle()));
                    }
                }
            }
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
}
