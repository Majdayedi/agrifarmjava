package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connections {
    private final String URL = "jdbc:mysql://localhost:3306/projet";
    private final String USER = "root";
    private final String PASS = "";
    private Connection connection;
    private static Connections instance;

    private Connections() {
        createConnection();
    }

    private void createConnection() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connection successful");
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            throw new RuntimeException("Failed to create database connection", e);
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                createConnection();
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("Connection check failed: " + e.getMessage());
            createConnection();
            return connection;
        }
    }

    public static Connections getInstance() {
        if (instance == null) {
            instance = new Connections();
        }
        return instance;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connection closed successfully");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
