package service;

import entite.Crop;
import entite.Farm;
import entite.Field;
import utils.Connections;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FieldService implements IService<Field> {

    private final Connection cnx;
    private PreparedStatement pst;
    private ResultSet rs;

    public FieldService() {
        cnx = Connections.getInstance().getConnection();
    }
    /**
     * Retrieves a list of all crop IDs that are assigned to fields
     * @return List of crop IDs (may contain duplicates if same crop is in multiple fields)
     */
    public List<Integer> getCropIds() {
        List<Integer> cropIds = new ArrayList<>();
        String query = "SELECT crop_id FROM field WHERE crop_id IS NOT NULL";

        try (PreparedStatement pst = cnx.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                cropIds.add(rs.getInt("crop_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving assigned crop IDs: " + e.getMessage(), e);
        }
        return cropIds;
    }
    @Override
    public void create(Field field) {
        String requete = "INSERT INTO field (farm_id, surface, name, budget, income, outcome, " +
                "profit, description, crop_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, field.getFarm().getId());
            pst.setDouble(2, field.getSurface());
            pst.setString(3, field.getName());
            pst.setDouble(4, field.getBudget());
            pst.setDouble(5, field.getIncome());
            pst.setDouble(6, field.getOutcome());
            pst.setDouble(7, field.getProfit());
            pst.setString(8, field.getDescription());
            if (field.getCrop() == null) {
                pst.setNull(9, Types.INTEGER);
            } else {
                pst.setInt(9, field.getCrop().getId());
            }


            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating field: " + e.getMessage(), e);
        } finally {
            closeResources();
        }
    }

    @Override
    public boolean update(Field field) {
        if (field.getFarm() == null) {
            throw new IllegalArgumentException("Le champ n'est pas associé à une ferme valide.");
        }

        String requete = "UPDATE field SET farm_id=?, surface=?, name=?, budget=?, " +
                "income=?, outcome=?, profit=?, description=?, crop_id=? WHERE id=?";

        try {
            pst = cnx.prepareStatement(requete);

            pst.setInt(1, field.getFarm().getId());
            pst.setDouble(2, field.getSurface());
            pst.setString(3, field.getName());
            pst.setDouble(4, field.getBudget());
            pst.setDouble(5, field.getIncome());
            pst.setDouble(6, field.getOutcome());
            pst.setDouble(7, field.getProfit());
            pst.setString(8, field.getDescription());
            pst.setObject(9, field.getCrop().getId());
            pst.setInt(10, field.getId());

            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating field: " + e.getMessage(), e);
        } finally {
            closeResources();
        }
        return false;
    }

    @Override
    public void delete(Field field) {
        String requete = "DELETE FROM field WHERE id=?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, field.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting field: " + e.getMessage(), e);
        } finally {
            closeResources();
        }
    }

    @Override
    public List<Field> readAll() {
        List<Field> fields = new ArrayList<>();
        String query = "SELECT * FROM field";

        try (Connection cnx = Connections.getInstance().getConnection();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                fields.add(mapResultSetToField(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading all fields: " + e.getMessage(), e);
        }
        return fields;
    }

    @Override
    public Field readById(int id) {
        String requete = "SELECT * FROM field WHERE id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            rs = pst.executeQuery();
            if(rs.next()) {
                return mapResultSetToField(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading field by id: " + e.getMessage(), e);
        } finally {
            closeResources();
        }
        return null;
    }
    public List<String> getCropList(Farm farm) {
        List<String> crops = new ArrayList<>();
        crops.add("Select Crop");
        String query = "SELECT DISTINCT c.type_crop " +
                "FROM crop c " +
                "INNER JOIN field f ON c.id = f.crop_id " +
                "WHERE f.farm_id = ?";

        try {
            pst = cnx.prepareStatement(query);
            pst.setInt(1, farm.getId()); // now it uses field id
            rs = pst.executeQuery();

            while (rs.next()) {
                String crop = rs.getString("type_crop");
                crops.add(crop);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading crops by field: " + e.getMessage(), e);
        } finally {
            closeResources();
        }
        return crops;
    }

    public List<Field> getFieldsByFarm(int farmId) {
        List<Field> fields = new ArrayList<>();
        String query = "SELECT * FROM field WHERE farm_id = ?";

        try {
            pst = cnx.prepareStatement(query);
            pst.setInt(1, farmId);
            rs = pst.executeQuery();

            while (rs.next()) {
                fields.add(mapResultSetToField(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading fields by farm: " + e.getMessage(), e);
        } finally {
            closeResources();
        }
        return fields;
    }


    private Field mapResultSetToField(ResultSet rs) throws SQLException {
        Field field = new Field();
        field.setId(rs.getInt("id"));
        field.setSurface(rs.getDouble("surface"));
        field.setName(rs.getString("name"));
        field.setBudget(rs.getDouble("budget"));
        field.setIncome(rs.getDouble("income"));
        field.setOutcome(rs.getDouble("outcome"));
        field.setProfit(rs.getDouble("profit"));
        field.setDescription(rs.getString("description"));
        field.setCrop(new CropCRUD().getCropById(rs.getInt("crop_id")));

        // You'll need to set the farm and crop relationships here
        // This would require additional queries or joins

        return field;
    }

    private void closeResources() {
        try {
            if (rs != null) {
                rs.close();
            }
            if (pst != null) {
                pst.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }
} 