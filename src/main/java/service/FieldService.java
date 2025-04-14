package service;

import entite.Field;
import utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FieldService implements IService<Field> {

    private final Connection cnx;
    private PreparedStatement pst;
    private ResultSet rs;

    public FieldService() {
        cnx = DataSource.getInstance().getConnection();
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
            pst.setObject(9, field.getCrop() != null ? field.getCrop().getId() : null);

            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating field: " + e.getMessage(), e);
        } finally {
            closeResources();
        }
    }

    @Override
    public void update(Field field) {
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
            pst.setObject(9, field.getCrop() != null ? field.getCrop().getId() : null);
            pst.setInt(10, field.getId());

            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating field: " + e.getMessage(), e);
        } finally {
            closeResources();
        }
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

        try (Connection cnx = DataSource.getInstance().getConnection();
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