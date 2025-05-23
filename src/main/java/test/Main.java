package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import controller.AdminDashboardController;
import controller.UserDashboardController;
import utils.Session;
import utils.SceneManager;
import org.opencv.core.Core;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Load the OpenCV library
          // This will automatically load the appropriate OpenCV DLL.

        // Print OpenCV version to confirm it loaded successfully
        System.out.println("OpenCV Version: " + Core.VERSION);

        // Check if user is already logged in
        if (Session.getInstance().isLoggedIn()) {
            // User is logged in, redirect to appropriate dashboard
            if (Session.getInstance().isAdmin()) {
                SceneManager.switchScene(stage, "/controller/AdminDashboard.fxml",
                        (AdminDashboardController controller) -> controller.setUser(Session.getInstance().getUser()));
            } else {
                SceneManager.switchScene(stage, "/controller/UserDashboard.fxml",
                        (UserDashboardController controller) -> controller.setUser(Session.getInstance().getUser()));
            }
        } else {
            // No active session, show login screen
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/controller/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("Login");
            stage.setScene(scene);
            stage.show();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
