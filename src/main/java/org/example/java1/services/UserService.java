package org.example.java1.services;

import com.google.gson.Gson;
import org.example.java1.entity.User;
import org.example.java1.database.DataSource;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserService {

    private static final Gson gson = new Gson();

    // Modified: Accepts imageFileName as argument
    public boolean registerUser(String email, String password, List<String> roles, String firstName, String lastName, String imageFileName) {
        if (emailExists(email)) {
            System.out.println("Email already exists!");
            return false;
        }
        List<String> assignedRoles = (roles != null && !roles.isEmpty()) ? roles : Collections.singletonList("ROLE_USER");

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String rolesJson = gson.toJson(assignedRoles);

        String sql = "INSERT INTO users (email, password, roles, first_name, last_name, created_at, image_file_name) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, rolesJson);
            stmt.setString(4, firstName);
            stmt.setString(5, lastName);
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(7, imageFileName);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User loginUser(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (BCrypt.checkpw(password, storedPassword)) {
                    List<String> roles = User.parseRolesFromJson(rs.getString("roles"));

                    return new User(
                            rs.getInt("id"),
                            rs.getString("email"),
                            roles,
                            rs.getString("password"),
                            rs.getString("last_name"),
                            rs.getString("first_name"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getString("image_file_name") // Get profile pic
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                List<String> roles = User.parseRolesFromJson(rs.getString("roles"));
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        roles,
                        rs.getString("password"),
                        rs.getString("last_name"),
                        rs.getString("first_name"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getString("image_file_name") // Get profile pic
                );
                users.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE users SET email = ?, roles = ?, first_name = ?, last_name = ?, image_file_name = ? WHERE id = ?";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getEmail());
            stmt.setString(2, gson.toJson(user.getRoles()));
            stmt.setString(3, user.getFirstName());
            stmt.setString(4, user.getLastName());
            stmt.setString(5, user.getImageFileName()); // Set profile pic
            stmt.setInt(6, user.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean emailExists(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            return rs.next(); // true if a record exists

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
