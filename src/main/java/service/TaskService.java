package service;

import entite.Task;
import utils.Connections;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskService implements IService<Task> {

    private final Connection cnx;
    private PreparedStatement pst;
    private ResultSet rs;

    public TaskService() {
        cnx = Connections.getInstance().getConnection();
    }

    @Override
    public void create(Task task) {
        String requete = "INSERT INTO task ( field_id,name, description, status, date, " +
                "ressource, responsable, priority, estimated_duration, deadline, workers, " +
                "last_updated, payment_worker, total) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, task.getField().getId());

            pst.setString(2, task.getName());
            pst.setString(3, task.getDescription());
            pst.setString(4, task.getStatus());
            pst.setDate(5, new java.sql.Date(task.getDate().getTime()));
            pst.setString(6, task.getRessource());
            pst.setString(7, task.getResponsable());
            pst.setString(8, task.getPriority());
            pst.setString(9, task.getEstimatedDuration());
            pst.setDate(10, new java.sql.Date(task.getDeadline().getTime()));
            pst.setInt(11, task.getWorkers());
            pst.setTimestamp(12, new java.sql.Timestamp(task.getLastUpdated().getTime()));
            pst.setDouble(13, task.getPaymentWorker());
            pst.setDouble(14, task.getTotal());

            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating task: " + e.getMessage(), e);
        } finally {
            closeResources();
        }
    }

    @Override
    public boolean update(Task task) {
        String requete = "UPDATE task SET field_id=?, name=?, description=?, status=?, date=?, " +
                "ressource=?, responsable=?, priority=?, estimated_duration=?, deadline=?, workers=?, " +
                "last_updated=?, payment_worker=?, total=? WHERE id=?";

        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, task.getField().getId());
            pst.setString(2, task.getName());
            pst.setString(3, task.getDescription());
            pst.setString(4, task.getStatus());
            pst.setDate(5, new java.sql.Date(task.getDate().getTime()));
            pst.setString(6, task.getRessource());
            pst.setString(7, task.getResponsable());
            pst.setString(8, task.getPriority());
            pst.setString(9, task.getEstimatedDuration());
            pst.setDate(10, new java.sql.Date(task.getDeadline().getTime()));
            pst.setInt(11, task.getWorkers());
            pst.setTimestamp(12, new java.sql.Timestamp(task.getLastUpdated().getTime()));
            pst.setDouble(13, task.getPaymentWorker());
            pst.setDouble(14, task.getTotal());
            pst.setInt(15, task.getId());

            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating task: " + e.getMessage(), e);
        } finally {
            closeResources();
        }
        return false;
    }

    @Override
    public void delete(Task task) {
        String requete = "DELETE FROM task WHERE id=?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, task.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting task: " + e.getMessage(), e);
        } finally {
            closeResources();
        }
    }

    @Override
    public List<Task> readAll() {
        List<Task> tasks = new ArrayList<>();
        String query = "SELECT * FROM task";

        try (Connection cnx = Connections.getInstance().getConnection();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading all tasks: " + e.getMessage(), e);
        }
        return tasks;
    }

    @Override
    public Task readById(int id) {
        String requete = "SELECT * FROM task WHERE id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            rs = pst.executeQuery();
            if(rs.next()) {
                return mapResultSetToTask(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading task by id: " + e.getMessage(), e);
        } finally {
            closeResources();
        }
        return null;
    }

    public List<Task> getTasksByField(int fieldId) {
        List<Task> tasks = new ArrayList<>();
        String query = "SELECT t.*, f.id AS field_id, f.name AS field_name " +
                "FROM task t " +
                "JOIN field f ON t.field_id = f.id " +
                "WHERE t.field_id = ?";

        try {
            pst = cnx.prepareStatement(query);
            pst.setInt(1, fieldId);
            rs = pst.executeQuery();

            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading tasks by field: " + e.getMessage(), e);
        } finally {
            closeResources();
        }

        return tasks;
    }
    public List<Task> getTasksByStatus(String status) {
        List<Task> tasks = new ArrayList<>();
        String query = "SELECT * FROM task WHERE status = ?";

        try {
            pst = cnx.prepareStatement(query);
            pst.setString(1, status);
            rs = pst.executeQuery();

            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading tasks by status: " + e.getMessage(), e);
        } finally {
            closeResources();
        }

        return tasks;
    }

    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setName(rs.getString("name"));
        task.setDescription(rs.getString("description"));
        task.setStatus(rs.getString("status"));

        // Handle date fields safely
        task.setDate(parseDateSafely(rs.getString("date")));
        task.setDeadline(parseDateSafely(rs.getString("deadline")));

        task.setRessource(rs.getString("ressource"));
        task.setResponsable(rs.getString("responsable"));
        task.setPriority(rs.getString("priority"));
        task.setEstimatedDuration(rs.getString("estimated_duration"));
        task.setWorkers(rs.getInt("workers"));
        task.setLastUpdated(rs.getTimestamp("last_updated"));
        task.setPaymentWorker(rs.getDouble("payment_worker"));
        task.setTotal(rs.getDouble("total"));

        return task;
    }

    // Helper method to safely parse date strings
    private Date parseDateSafely(String dateString) {
        if (dateString == null || dateString.trim().isEmpty() || dateString.equals("0000-00-00")) {
            return null;
        }
        try {
            return Date.valueOf(dateString);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid date format: " + dateString);
            return null;
        }
    }
    // Helper method to safely convert dates to strings
    private String convertDateToString(ResultSet rs, String columnName) throws SQLException {
        try {
            java.sql.Date date = rs.getDate(columnName);
            return (date != null) ? date.toString() : null;
        } catch (SQLException e) {
            if (e.getMessage().contains("Zero date value prohibited")) {
                return null; // or return "0000-00-00" if you want to preserve the zero date
            }
            throw e;
        }
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

    public boolean updateStatus(Task task, String status) {
        String sql = "UPDATE task SET status = ? WHERE id = ?";
        try {
            pst = cnx.prepareStatement(sql);
            pst.setString(1, status);
            pst.setInt(2, task.getId());
            
            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated > 0) {
                task.setStatus(status);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating task status: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources();
        }
        return false;
    }

    public Task getTaskById(int taskId) {
        String query = "SELECT * FROM task WHERE id = ?";
        try {
            pst = cnx.prepareStatement(query);
            pst.setInt(1, taskId);
            rs = pst.executeQuery();

            if (rs.next()) {
                return mapResultSetToTask(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading task by ID: " + e.getMessage(), e);
        } finally {
            closeResources();
        }
        return null;
    }
}