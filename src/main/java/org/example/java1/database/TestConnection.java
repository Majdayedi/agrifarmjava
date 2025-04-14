package org.example.java1.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestConnection {
    public static void main(String[] args) {
        // Get the database connection
        Connection connection = org.example.java1.database.DataSource.getInstance().getConnection();

        // SQL Query to select all users with roles
        String query = "SELECT id, first_name, last_name, roles FROM users"; // Update table name if different

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Print the results
            while (rs.next()) {
                int id = rs.getInt("id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String roles = rs.getString("roles"); // Retrieve roles

                System.out.println("ID: " + id + ", First Name: " + firstName + ", Last Name: " + lastName + ", Roles: " + roles);
            }
        } catch (SQLException e) {
            System.err.println("Query failed: " + e.getMessage());
        }
    }
}
