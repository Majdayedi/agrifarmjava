package service;

import entite.Farm;
import entite.Task;
import utils.Connections;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FarmService implements IService<Farm> {

    private final Connection cnx;

    public FarmService() {
        this.cnx = Connections.getInstance().getConnection();
    }

    @Override
    public void create(Farm farm) {
        String requete = "INSERT INTO farm (location, name, surface, adress, budget, weather, " +
                "description, bir, photovoltaic, fence, irrigation, cabin, lon, lat) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = cnx.prepareStatement(requete, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, farm.getLocation());
            pst.setString(2, farm.getName());
            pst.setDouble(3, farm.getSurface());
            pst.setString(4, farm.getAdress());
            pst.setDouble(5, farm.getBudget());
            pst.setString(6, farm.getWeather());
            pst.setString(7, farm.getDescription());
            pst.setBoolean(8, farm.isBir());
            pst.setBoolean(9, farm.isPhotovoltaic());
            pst.setBoolean(10, farm.isFence());
            pst.setBoolean(11, farm.isIrrigation());
            pst.setBoolean(12, farm.isCabin());
            pst.setFloat(13, farm.getLon());
            pst.setFloat(14, farm.getLat());

            pst.executeUpdate();

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    farm.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating farm: " + e.getMessage(), e);
        }
    }
    public void createfarm(Farm farm, int userId) {
        String requete = "INSERT INTO farm (location, name, surface, adress, budget, weather, " +
                "description, bir, photovoltaic, fence, irrigation, cabin, lon, lat,user_id_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = cnx.prepareStatement(requete, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, farm.getLocation());
            pst.setString(2, farm.getName());
            pst.setDouble(3, farm.getSurface());
            pst.setString(4, farm.getAdress());
            pst.setDouble(5, farm.getBudget());
            pst.setString(6, farm.getWeather());
            pst.setString(7, farm.getDescription());
            pst.setBoolean(8, farm.isBir());
            pst.setBoolean(9, farm.isPhotovoltaic());
            pst.setBoolean(10, farm.isFence());
            pst.setBoolean(11, farm.isIrrigation());
            pst.setBoolean(12, farm.isCabin());
            pst.setFloat(13, farm.getLon());
            pst.setFloat(14, farm.getLat());
            pst.setFloat(15, userId);
            pst.executeUpdate();

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    farm.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating farm: " + e.getMessage(), e);
        }
    }
    @Override
    public boolean update(Farm farm) {
        String requete = "UPDATE farm SET location=?, name=?, surface=?, adress=?, budget=?, " +
                "weather=?, description=?, bir=?, photovoltaic=?, fence=?, irrigation=?, " +
                "cabin=?, lon=?, lat=? WHERE id=?";

        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, farm.getLocation());
            pst.setString(2, farm.getName());
            pst.setDouble(3, farm.getSurface());
            pst.setString(4, farm.getAdress());
            pst.setDouble(5, farm.getBudget());
            pst.setString(6, farm.getWeather());
            pst.setString(7, farm.getDescription());
            pst.setBoolean(8, farm.isBir());
            pst.setBoolean(9, farm.isPhotovoltaic());
            pst.setBoolean(10, farm.isFence());
            pst.setBoolean(11, farm.isIrrigation());
            pst.setBoolean(12, farm.isCabin());
            pst.setFloat(13, farm.getLon());
            pst.setFloat(14, farm.getLat());
            pst.setInt(15, farm.getId());

            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating farm: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void delete(Farm farm) {
        String requete = "DELETE FROM farm WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setInt(1, farm.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting farm: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Farm> readAll() {
        List<Farm> farms = new ArrayList<>();
        String query = "SELECT * FROM farm";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                farms.add(mapResultSetToFarm(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading all farms: " + e.getMessage(), e);
        }
        return farms;
    }
    public List<Farm> read(int id) {
        List<Farm> farms = new ArrayList<>();
        String query = "SELECT * FROM farm WHERE user_id_id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    farms.add(mapResultSetToFarm(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading all farms: " + e.getMessage(), e);
        }

        return farms;
    }

    @Override
    public Farm readById(int id) {
        String requete = "SELECT * FROM farm WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToFarm(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading farm by id: " + e.getMessage(), e);
        }
        return null;
    }

    public Farm getFarmByFieldId(int fieldId) {
        String query = "SELECT f.* FROM farm f " +
                "JOIN field fi ON f.id = fi.farm_id " +
                "WHERE fi.id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, fieldId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToFarm(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching farm by field ID: " + e.getMessage(), e);
        }
        return null;
    }

    private Farm mapResultSetToFarm(ResultSet rs) throws SQLException {
        Farm farm = new Farm();
        farm.setId(rs.getInt("id"));
        farm.setLocation(rs.getString("location"));
        farm.setName(rs.getString("name"));
        farm.setSurface(rs.getDouble("surface"));
        farm.setAdress(rs.getString("adress"));
        farm.setBudget(rs.getDouble("budget"));
        farm.setWeather(rs.getString("weather"));
        farm.setDescription(rs.getString("description"));
        farm.setBir(rs.getBoolean("bir"));
        farm.setPhotovoltaic(rs.getBoolean("photovoltaic"));
        farm.setFence(rs.getBoolean("fence"));
        farm.setIrrigation(rs.getBoolean("irrigation"));
        farm.setCabin(rs.getBoolean("cabin"));
        farm.setLon(rs.getFloat("lon"));
        farm.setLat(rs.getFloat("lat"));
        return farm;
    }
    public boolean updateWeather(Farm farm) {
        String sql = "UPDATE farm SET weather = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, farm.getWeather());
            pst.setInt(2, farm.getId());

            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated > 0) {
                farm.setWeather(farm.getWeather());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating task status: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

}