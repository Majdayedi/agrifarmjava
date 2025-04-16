package service;

import utils.Connections;
import entite.Commentaire;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.sql.DatabaseMetaData;

public class CommentaireService {
    private static final Logger logger = Logger.getLogger(CommentaireService.class.getName());
    private final Connection connection;

    public CommentaireService() throws SQLException {
        this.connection = Connections.getConnection();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() throws SQLException {
        // First, create the table if it doesn't exist
        String createTableSQL = "CREATE TABLE IF NOT EXISTS commentaire (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "article_id INT NOT NULL, " +
                "rate INT NOT NULL CHECK (rate BETWEEN 1 AND 5), " +
                "commentaire TEXT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE)";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            logger.info("✅ Commentaire table verified");
            
            // Now check if the created_at column exists, and add it if it doesn't
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                ResultSet columns = metaData.getColumns(null, null, "commentaire", "created_at");
                
                // If the column doesn't exist, add it
                if (!columns.next()) {
                    logger.info("Adding missing created_at column to commentaire table");
                    stmt.execute("ALTER TABLE commentaire ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                    logger.info("✅ Added created_at column to commentaire table");
                }
                columns.close();
            } catch (SQLException e) {
                logger.warning("Failed to check or add created_at column: " + e.getMessage());
                // Try direct approach if metadata approach fails
                try {
                    stmt.execute("ALTER TABLE commentaire ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                    logger.info("✅ Added created_at column to commentaire table (direct approach)");
                } catch (SQLException e2) {
                    // If this also fails, it might be because the column already exists or another error
                    logger.warning("Failed to add created_at column (direct approach): " + e2.getMessage());
                }
            }
        } catch (SQLException e) {
            logger.severe("❌ Failed to create or modify commentaire table: " + e.getMessage());
            throw e;
        }
    }

    // 🔹 CREATE
    public void add(Commentaire commentaire) throws SQLException {
        if (commentaire == null) {
            throw new IllegalArgumentException("Commentaire cannot be null");
        }

        String query = "INSERT INTO commentaire (article_id, rate, commentaire) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);
            
            ps.setInt(1, commentaire.getArticleId());
            ps.setInt(2, commentaire.getRate());
            ps.setString(3, commentaire.getCommentaire());
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating commentaire failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    commentaire.setId(generatedKeys.getInt(1));
                    // Get the created_at value
                    try (PreparedStatement psSelect = connection.prepareStatement("SELECT created_at FROM commentaire WHERE id = ?")) {
                        psSelect.setInt(1, commentaire.getId());
                        try (ResultSet rs = psSelect.executeQuery()) {
                            if (rs.next()) {
                                commentaire.setCreatedAt(rs.getTimestamp("created_at"));
                            }
                        }
                    }
                } else {
                    throw new SQLException("Creating commentaire failed, no ID obtained.");
                }
            }
            
            connection.commit();
            logger.info("✅ Commentaire added successfully with ID: " + commentaire.getId());
        } catch (SQLException e) {
            connection.rollback();
            logger.severe("❌ Failed to add commentaire: " + e.getMessage());
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // 🔹 READ ALL COMMENTS FOR AN ARTICLE
    public List<Commentaire> getAllByArticleId(int articleId) throws SQLException {
        List<Commentaire> commentaires = new ArrayList<>();
        String query = "SELECT id, article_id, rate, commentaire, created_at FROM commentaire WHERE article_id = ? ORDER BY created_at DESC";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, articleId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Commentaire commentaire = new Commentaire(
                        rs.getInt("id"),
                        rs.getInt("article_id"),
                        rs.getInt("rate"),
                        rs.getString("commentaire"),
                        rs.getTimestamp("created_at")
                    );
                    commentaires.add(commentaire);
                }
            }
            logger.info("Retrieved " + commentaires.size() + " comments for article ID: " + articleId);
        } catch (SQLException e) {
            logger.severe("❌ Failed to retrieve comments: " + e.getMessage());
            throw e;
        }
        return commentaires;
    }

    // 🔹 UPDATE
    public void update(Commentaire commentaire) throws SQLException {
        if (commentaire == null) {
            throw new IllegalArgumentException("Commentaire cannot be null");
        }

        String query = "UPDATE commentaire SET rate = ?, commentaire = ? WHERE id = ? AND article_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            
            ps.setInt(1, commentaire.getRate());
            ps.setString(2, commentaire.getCommentaire());
            ps.setInt(3, commentaire.getId());
            ps.setInt(4, commentaire.getArticleId());
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Updating commentaire failed, no rows affected.");
            }
            
            connection.commit();
            logger.info("✅ Commentaire updated successfully with ID: " + commentaire.getId());
        } catch (SQLException e) {
            connection.rollback();
            logger.severe("❌ Failed to update commentaire: " + e.getMessage());
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // 🔹 DELETE
    public void delete(int id, int articleId) throws SQLException {
        String query = "DELETE FROM commentaire WHERE id = ? AND article_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            
            ps.setInt(1, id);
            ps.setInt(2, articleId);
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Deleting commentaire failed, no rows affected.");
            }
            
            connection.commit();
            logger.info("🗑️ Commentaire deleted successfully with ID: " + id);
        } catch (SQLException e) {
            connection.rollback();
            logger.severe("❌ Failed to delete commentaire: " + e.getMessage());
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // 🔹 GET AVERAGE RATING FOR AN ARTICLE
    public double getAverageRating(int articleId) throws SQLException {
        String query = "SELECT COALESCE(AVG(rate), 0) as avg_rating FROM commentaire WHERE article_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, articleId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_rating");
                }
            }
        } catch (SQLException e) {
            logger.severe("❌ Failed to get average rating: " + e.getMessage());
            throw e;
        }
        return 0.0;
    }

    // 🔹 GET COMMENT COUNT FOR AN ARTICLE
    public int getCommentCount(int articleId) throws SQLException {
        String query = "SELECT COUNT(*) as comment_count FROM commentaire WHERE article_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, articleId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("comment_count");
                }
            }
        } catch (SQLException e) {
            logger.severe("❌ Failed to get comment count: " + e.getMessage());
            throw e;
        }
        return 0;
    }

    public List<Commentaire> getAll() throws SQLException {
        List<Commentaire> comments = new ArrayList<>();
        String query = "SELECT * FROM commentaire ORDER BY created_at DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Commentaire comment = new Commentaire();
                comment.setId(rs.getInt("id"));
                comment.setArticleId(rs.getInt("article_id"));
                comment.setRate(rs.getInt("rate"));
                comment.setCommentaire(rs.getString("commentaire"));
                comment.setCreatedAt(rs.getTimestamp("created_at"));
                comments.add(comment);
            }
        }

        return comments;
    }
}

