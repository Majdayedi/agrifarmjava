package service;

import com.google.gson.Gson;
import entite.User;
import org.mindrot.jbcrypt.BCrypt;
import utils.Connections;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;


public class UserService {

    private static final Gson gson = new Gson();

    // Modified: Accepts imageFileName as argument
    public int registerUser(String email, String password, List<String> roles, String firstName, String lastName, String imageFileName) {
        if (emailExists(email)) {
            System.out.println("Email already exists!");
            return -1;
        }
        List<String> assignedRoles = (roles != null && !roles.isEmpty()) ? roles : Collections.singletonList("ROLE_USER");

        // Generate the hash normally
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Replace $2a$ with $2y$
        if (hashedPassword.startsWith("$2a$")) {
            hashedPassword = "$2y$" + hashedPassword.substring(4);
        }

        String rolesJson = gson.toJson(assignedRoles);

        String sql = "INSERT INTO users (email, password, roles, first_name, last_name, created_at, image_file_name, is_verified) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Connections.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, email);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, rolesJson);
            stmt.setString(4, firstName);
            stmt.setString(5, lastName);
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(7, imageFileName);
            stmt.setBoolean(8, false); // Email not verified initially

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1); // Return the user ID
                    }
                }
            }
            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }


    public User loginUser(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = Connections.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");

                // ✅ Fix: Convert Symfony-style bcrypt $2y$ to Java-compatible $2a$
                if (storedPassword.startsWith("$2y$")) {
                    storedPassword = "$2a$" + storedPassword.substring(4);
                }

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
                            rs.getString("image_file_name"),
                            rs.getBoolean("is_verified")
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

        try (
                Connection conn = Connections.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
        ) {
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
                        rs.getString("image_file_name"),
                        rs.getBoolean("is_verified")
                );

                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // You can also log this if preferred
        }

        return users;
    }


    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = Connections.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE users SET email = ?, roles = ?, first_name = ?, last_name = ?, image_file_name = ?, is_verified = ? WHERE id = ?";

        try (Connection conn = Connections.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getEmail());
            stmt.setString(2, gson.toJson(user.getRoles()));
            stmt.setString(3, user.getFirstName());
            stmt.setString(4, user.getLastName());
            stmt.setString(5, user.getImageFileName()); // Set profile pic
            stmt.setBoolean(6, user.isVerified()); // Set email verification status
            stmt.setInt(7, user.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean emailExists(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";

        try (Connection conn = Connections.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            return rs.next(); // true if a record exists

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = Connections.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                List<String> roles = User.parseRolesFromJson(rs.getString("roles"));
                return new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        roles,
                        rs.getString("password"),
                        rs.getString("last_name"),
                        rs.getString("first_name"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getString("image_file_name"),
                        rs.getBoolean("is_verified")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Verify a user's email by updating their is_verified status
     * @param userId The ID of the user to verify
     * @return true if verification was successful, false otherwise
     */
    public boolean verifyUser(int userId) {
        String sql = "UPDATE users SET is_verified = 1 WHERE id = ?";

        try (Connection conn = Connections.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update a user's password
     * @param email The email of the user
     * @param newPassword The new password to set
     * @return true if the password was updated successfully, false otherwise
     */
    public boolean updatePassword(String email, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE email = ?";

        try (Connection conn = Connections.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            pstmt.setString(1, hashedPassword);
            pstmt.setString(2, email);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public int registerFacebookUser(String email, String firstName, String lastName, String profilePicUrl) {
        if (emailExists(email)) {
            System.out.println("Facebook email already exists, skipping registration.");
            return -1;
        }

        List<String> roles = Collections.singletonList("ROLE_USER");

        // Download and save the profile picture locally
        String imageFileName = null;
        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
            try (InputStream in = new URL(profilePicUrl).openStream()) {
                imageFileName = UUID.randomUUID() + ".jpg";
                Path outputPath = Paths.get("src/main/resources/profile_pics/", imageFileName);
                Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                imageFileName = null;
            }
        }

        String rolesJson = gson.toJson(roles);
        String dummyPassword = Base64.getEncoder()
                .encodeToString(String.valueOf(System.currentTimeMillis()).getBytes());

        String sql = "INSERT INTO users (email, password, roles, first_name, last_name, created_at, image_file_name, is_verified) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Connections.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, email);
            stmt.setString(2, dummyPassword); // use a dummy password instead of null
            stmt.setString(3, rolesJson);
            stmt.setString(4, firstName);
            stmt.setString(5, lastName);
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(7, imageFileName);
            stmt.setBoolean(8, true); // Facebook registration = verified

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }



}
