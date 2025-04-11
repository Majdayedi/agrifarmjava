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

    public Connections() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connection successful");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }
    public static Connections getInstance() {
        if (instance== null)
            instance = new Connections();
        return instance;
    }
}
