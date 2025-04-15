package service;

import entite.SoilData;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SoilDataCRUD {
    private Connection connection;

    public SoilDataCRUD() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/projet", "root", "");
            // Add date column if it doesn't exist
            addDateColumnIfNotExists();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addDateColumnIfNotExists() {
        try {
            // Check if date column exists
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "soildata", "date");
            
            if (!columns.next()) {
                // Column doesn't exist, add it
                String alterTableSQL = "ALTER TABLE soildata ADD COLUMN date DATE DEFAULT CURRENT_DATE";
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(alterTableSQL);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createSoilData(SoilData soilData) throws SQLException {
        String sql = "INSERT INTO soildata (humidite, niveau_ph, niveau_nutriment, type_sol, crop_id, date) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setDouble(1, soilData.getHumidite());
            statement.setDouble(2, soilData.getNiveau_ph());
            statement.setDouble(3, soilData.getNiveau_nutriment());
            statement.setString(4, soilData.getType_sol());
            statement.setInt(5, soilData.getCrop_id());
            statement.setString(6, soilData.getDate());
            statement.executeUpdate();
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    soilData.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<SoilData> getSoilDataByCropId(int cropId) throws SQLException {
        List<SoilData> soilDataList = new ArrayList<>();
        String sql = "SELECT * FROM soildata WHERE crop_id = ? ORDER BY date DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, cropId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    SoilData soilData = new SoilData(
                        resultSet.getInt("id"),
                        resultSet.getDouble("humidite"),
                        resultSet.getDouble("niveau_ph"),
                        resultSet.getDouble("niveau_nutriment"),
                        resultSet.getString("type_sol"),
                        resultSet.getInt("crop_id"),
                        resultSet.getString("date")
                    );
                    soilDataList.add(soilData);
                }
            }
        }
        return soilDataList;
    }

    public void updateSoilData(SoilData soilData) throws SQLException {
        String sql = "UPDATE soildata SET humidite = ?, niveau_ph = ?, niveau_nutriment = ?, type_sol = ?, crop_id = ?, date = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, soilData.getHumidite());
            statement.setDouble(2, soilData.getNiveau_ph());
            statement.setDouble(3, soilData.getNiveau_nutriment());
            statement.setString(4, soilData.getType_sol());
            statement.setInt(5, soilData.getCrop_id());
            statement.setString(6, soilData.getDate());
            statement.setInt(7, soilData.getId());
            statement.executeUpdate();
        }
    }

    public void deleteSoilData(int id) throws SQLException {
        String query = "DELETE FROM soildata WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }
} 