package service;

import entite.Crop;
import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CropCRUD {

    // Create
    public void createCrop(Crop crop) throws SQLException {
        String sql = "INSERT INTO crop (crop_event, type_crop, methode_crop, date_plantation, heure_plantation, date_crop, heure_crop) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, crop.getCropEvent());
            pstmt.setString(2, crop.getTypeCrop());
            pstmt.setString(3, crop.getMethodCrop());
            pstmt.setString(4, crop.getPlantationDate());
            pstmt.setString(5, crop.getHourPlantation());
            pstmt.setString(6, crop.getCropDate());
            pstmt.setString(7, crop.getHourCrop());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    crop.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public Crop getLastAddedCrop() throws SQLException {
        String sql = "SELECT * FROM crop ORDER BY id DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return new Crop(
                        rs.getInt("id"),
                        rs.getString("crop_event"),
                        rs.getString("type_crop"),
                        rs.getString("methode_crop"),
                        rs.getString("date_plantation"),
                        rs.getString("heure_plantation"),
                        rs.getString("date_crop"),
                        rs.getString("heure_crop")
                );
            }
        }
        return null; // Return null if no crops exist
    }
    public List<Crop> getAllCrops() throws SQLException {
        List<Crop> crops = new ArrayList<>();
        String sql = "SELECT * FROM crop";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Crop crop = new Crop(
                    rs.getInt("id"),
                    rs.getString("crop_event"),
                    rs.getString("type_crop"),
                    rs.getString("methode_crop"),
                    rs.getString("date_plantation"),
                    rs.getString("heure_plantation"),
                    rs.getString("date_crop"),
                    rs.getString("heure_crop")
                );
                crops.add(crop);
            }
        }
        return crops;
    }

    public Crop getCropById(int id) throws SQLException {
        String sql = "SELECT * FROM crop WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Crop(
                        rs.getInt("id"),
                        rs.getString("crop_event"),
                        rs.getString("type_crop"),
                        rs.getString("methode_crop"),
                        rs.getString("date_plantation"),
                        rs.getString("heure_plantation"),
                        rs.getString("date_crop"),
                        rs.getString("heure_crop")
                    );
                }
            }
        }
        return null;
    }

    // Update
    public void updateCrop(Crop crop) throws SQLException {
        String sql = "UPDATE crop SET crop_event = ?, type_crop = ?, methode_crop = ?, date_plantation = ?, heure_plantation = ?, date_crop = ?, heure_crop = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, crop.getCropEvent());
            pstmt.setString(2, crop.getTypeCrop());
            pstmt.setString(3, crop.getMethodCrop());
            pstmt.setString(4, crop.getPlantationDate());
            pstmt.setString(5, crop.getHourPlantation());
            pstmt.setString(6, crop.getCropDate());
            pstmt.setString(7, crop.getHourCrop());
            pstmt.setInt(8, crop.getId());
            pstmt.executeUpdate();
        }
    }

    // Delete
    public void deleteCrop(int id) throws SQLException {
        // First delete all related soil data

        // Then delete the crop
        String sql = "DELETE FROM crop WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public List<Crop> searchByEvent(String keyword) throws SQLException {
        List<Crop> crops = new ArrayList<>();
        String sql = "SELECT * FROM crop WHERE crop_event LIKE ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + keyword + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    crops.add(new Crop(
                        rs.getInt("id"),
                        rs.getString("crop_event"),
                        rs.getString("type_crop"),
                        rs.getString("methode_crop"),
                        rs.getString("date_plantation"),
                        rs.getString("heure_plantation"),
                        rs.getString("date_crop"),
                        rs.getString("heure_crop")
                    ));
                }
            }
        }
        return crops;
    }

    public List<Crop> filterByType(String type) throws SQLException {
        List<Crop> crops = new ArrayList<>();
        String sql = "SELECT * FROM crop WHERE type_crop = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, type);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    crops.add(new Crop(
                        rs.getInt("id"),
                        rs.getString("crop_event"),
                        rs.getString("type_crop"),
                        rs.getString("methode_crop"),
                        rs.getString("date_plantation"),
                        rs.getString("heure_plantation"),
                        rs.getString("date_crop"),
                        rs.getString("heure_crop")
                    ));
                }
            }
        }
        return crops;
    }

    public List<Crop> searchCrops(String keyword) throws SQLException {
        List<Crop> crops = new ArrayList<>();
        String sql = "SELECT * FROM crop WHERE crop_event LIKE ? OR type_crop LIKE ? OR methode_crop LIKE ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    crops.add(new Crop(
                        rs.getInt("id"),
                        rs.getString("crop_event"),
                        rs.getString("type_crop"),
                        rs.getString("methode_crop"),
                        rs.getString("date_plantation"),
                        rs.getString("heure_plantation"),
                        rs.getString("date_crop"),
                        rs.getString("heure_crop")
                    ));
                }
            }
        }
        return crops;
    }

    public List<Crop> getCropsByType(String type) throws SQLException {
        List<Crop> crops = new ArrayList<>();
        String sql = "SELECT * FROM crop WHERE type_crop = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, type);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    crops.add(new Crop(
                        rs.getInt("id"),
                        rs.getString("crop_event"),
                        rs.getString("type_crop"),
                        rs.getString("methode_crop"),
                        rs.getString("date_plantation"),
                        rs.getString("heure_plantation"),
                        rs.getString("date_crop"),
                        rs.getString("heure_crop")
                    ));
                }
            }
        }
        return crops;
    }
} 