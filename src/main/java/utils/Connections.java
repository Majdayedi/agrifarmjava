package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connections {

    private final String url = "jdbc:mysql://localhost:3306/test";
    private final String username = "root";
    private final String password = "";
    private Connection connection;
    private static Connections instance;

    private Connections() {
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connections getInstance() {
        if (instance == null)
            instance = new Connections();
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, username, password);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reconnecting to the database", e);
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}