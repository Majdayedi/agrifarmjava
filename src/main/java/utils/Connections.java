package utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Connections {
    private static final Logger logger = Logger.getLogger(Connections.class.getName());
    private static Connections instance;
    private static Connection connection;

    private Connections() {
        try {
            connection = DatabaseConnection.getConnection();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de l'initialisation de la connexion", e);
        }
    }

    public static Connections getInstance() {
        if (instance == null) {
            instance = new Connections();
        }
        return instance;
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DatabaseConnection.getConnection();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de la vérification de la connexion", e);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}