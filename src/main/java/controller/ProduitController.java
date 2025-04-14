package controller;

import entite.Produit;
import utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitController {
    // Ne pas stocker la connexion comme attribut de classe
    
    // Méthode pour obtenir une connexion fraîche à chaque fois
    public Connection getConnection() {
        return DataSource.getInstance().getConnection();
    }

    // Suppression du constructeur qui stockait la connexion
    public ProduitController() {
        // Ne pas stocker la connexion
    }

    public boolean create(Produit produit) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        
        try {
            connection = getConnection();
            if (connection == null) {
                System.err.println("Cannot create product: Database connection is null");
                return false;
            }
            
            String sql = "INSERT INTO produit (nom, quantite, prix, categories, date_creation_produit, " +
                    "date_modification_produit, approved, description, image_file_name) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, produit.getNom());
            statement.setInt(2, produit.getQuantite());
            statement.setDouble(3, produit.getPrix());
            statement.setString(4, produit.getCategories());
            
            // Use current timestamp if date_creation_produit is null
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            statement.setTimestamp(5, produit.getDate_creation_produit() != null ? 
                    new Timestamp(produit.getDate_creation_produit().getTime()) : currentTimestamp);
            statement.setTimestamp(6, produit.getDate_modification_produit() != null ? 
                    new Timestamp(produit.getDate_modification_produit().getTime()) : currentTimestamp);
            
            statement.setBoolean(7, produit.isApproved());
            statement.setString(8, produit.getDescription());
            statement.setString(9, produit.getImage_file_name());
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    produit.setId(generatedKeys.getInt(1));
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error creating product: " + e.getMessage());
            return false;
        } finally {
            // Fermer uniquement les ressources statement et resultset, mais pas la connexion
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (statement != null) statement.close();
                // Ne pas fermer la connexion ici
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    public Produit read(int id) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        
        try {
            connection = getConnection();
            if (connection == null) {
                System.err.println("Cannot read product: Database connection is null");
                return null;
            }
            
            String sql = "SELECT * FROM produit WHERE id = ?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            rs = statement.executeQuery();
            
            if (rs.next()) {
                return extractProduitFromResultSet(rs);
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Error reading product: " + e.getMessage());
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
                // Ne pas fermer la connexion ici
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    public List<Produit> readAll() {
        List<Produit> produits = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        
        try {
            connection = getConnection();
            if (connection == null) {
                System.err.println("Cannot read all products: Database connection is null");
                return produits;
            }
            
            String sql = "SELECT * FROM produit";
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);
            
            while (rs.next()) {
                produits.add(extractProduitFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error reading all products: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
                // Ne pas fermer la connexion ici
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        
        return produits;
    }

    public boolean update(Produit produit) {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = getConnection();
            if (connection == null) {
                System.err.println("Cannot update product: Database connection is null");
                return false;
            }
            
            String sql = "UPDATE produit SET nom = ?, quantite = ?, prix = ?, categories = ?, " +
                    "date_modification_produit = ?, approved = ?, description = ?, image_file_name = ? " +
                    "WHERE id = ?";
            
            statement = connection.prepareStatement(sql);
            statement.setString(1, produit.getNom());
            statement.setInt(2, produit.getQuantite());
            statement.setDouble(3, produit.getPrix());
            statement.setString(4, produit.getCategories());
            statement.setTimestamp(5, new Timestamp(System.currentTimeMillis())); // Update modification date
            statement.setBoolean(6, produit.isApproved());
            statement.setString(7, produit.getDescription());
            statement.setString(8, produit.getImage_file_name());
            statement.setInt(9, produit.getId());
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
            return false;
        } finally {
            try {
                if (statement != null) statement.close();
                // Ne pas fermer la connexion ici
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    public boolean delete(int id) {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = getConnection();
            if (connection == null) {
                System.err.println("Cannot delete product: Database connection is null");
                return false;
            }
            
            String sql = "DELETE FROM produit WHERE id = ?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting product: " + e.getMessage());
            return false;
        } finally {
            try {
                if (statement != null) statement.close();
                // Ne pas fermer la connexion ici
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    public List<Produit> searchByName(String keyword) {
        List<Produit> produits = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        
        try {
            connection = getConnection();
            if (connection == null) {
                System.err.println("Cannot search products: Database connection is null");
                return produits;
            }
            
            String sql = "SELECT * FROM produit WHERE nom LIKE ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, "%" + keyword + "%");
            rs = statement.executeQuery();
            
            while (rs.next()) {
                produits.add(extractProduitFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching products: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
                // Ne pas fermer la connexion ici
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        
        return produits;
    }

    public List<Produit> filterByCategory(String category) {
        List<Produit> produits = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        
        try {
            connection = getConnection();
            if (connection == null) {
                System.err.println("Cannot filter products: Database connection is null");
                return produits;
            }
            
            String sql = "SELECT * FROM produit WHERE categories = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, category);
            rs = statement.executeQuery();
            
            while (rs.next()) {
                produits.add(extractProduitFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error filtering products by category: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
                // Ne pas fermer la connexion ici
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        
        return produits;
    }

    public List<Produit> searchByCategory(String category) {
        List<Produit> produits = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        
        try {
            connection = getConnection();
            if (connection == null) {
                System.err.println("Cannot search products by category: Database connection is null");
                return produits;
            }
            
            String sql = "SELECT * FROM produit WHERE categories LIKE ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, "%" + category + "%");
            rs = statement.executeQuery();
            
            while (rs.next()) {
                produits.add(extractProduitFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching products by category: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
                // Ne pas fermer la connexion ici
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        
        return produits;
    }

    public List<Produit> searchByApproved(boolean approved) {
        List<Produit> produits = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        
        try {
            connection = getConnection();
            if (connection == null) {
                System.err.println("Cannot search products by approval status: Database connection is null");
                return produits;
            }
            
            String sql = "SELECT * FROM produit WHERE approved = ?";
            statement = connection.prepareStatement(sql);
            statement.setBoolean(1, approved);
            rs = statement.executeQuery();
            
            while (rs.next()) {
                produits.add(extractProduitFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching products by approval status: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
                // Ne pas fermer la connexion ici
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        
        return produits;
    }

    private Produit extractProduitFromResultSet(ResultSet rs) throws SQLException {
        Produit produit = new Produit();
        produit.setId(rs.getInt("id"));
        produit.setNom(rs.getString("nom"));
        produit.setQuantite(rs.getInt("quantite"));
        produit.setPrix(rs.getDouble("prix"));
        produit.setCategories(rs.getString("categories"));
        produit.setDate_creation_produit(rs.getDate("date_creation_produit"));
        produit.setDate_modification_produit(rs.getDate("date_modification_produit"));
        produit.setApproved(rs.getBoolean("approved"));
        produit.setDescription(rs.getString("description"));
        produit.setImage_file_name(rs.getString("image_file_name"));
        return produit;
    }
} 