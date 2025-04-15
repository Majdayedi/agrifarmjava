package service;

import entite.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskService implements IService<Task> {

    private final Connection cnx;
    private PreparedStatement pst;
    private ResultSet rs;

    public TaskService() {
        cnx = DataSource.getInstance().getConnection();
    }

    @Override
    public void create(Task task) {
        String requete = "INSERT INTO task (field_id, name, description, status, date, " +
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
            pst.setInt(9, task.getEstimatedDuration());
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
    public void update(Task task) {
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
            pst.setInt(9, task.getEstimatedDuration());
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

        try (Connection cnx = DataSource.getInstance().getConnection();
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
        String query = "SELECT * FROM task WHERE field_id = ?";

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

    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setName(rs.getString("name"));
        task.setDescription(rs.getString("description"));
        task.setStatus(rs.getString("status"));
        task.setDate(rs.getDate("date"));
        task.setRessource(rs.getString("ressource"));
        task.setResponsable(rs.getString("responsable"));
        task.setPriority(rs.getString("priority"));
        task.setEstimatedDuration(rs.getInt("estimated_duration"));
        task.setDeadline(rs.getDate("deadline"));
        task.setWorkers(rs.getInt("workers"));
        task.setLastUpdated(rs.getTimestamp("last_updated"));
        task.setPaymentWorker(rs.getDouble("payment_worker"));
        task.setTotal(rs.getDouble("total"));
        
        // You'll need to set the field relationship here
        // This would require additional queries or joins
        
        return task;
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