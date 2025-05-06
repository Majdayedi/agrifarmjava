package test;

import controller.AdminDashboardController;
import controller.UserDashboardController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Core;
import utils.SceneManager;
import utils.Session;

import java.io.IOException;
import java.util.Objects;

public class MainFx extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Load the OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);  // This will automatically load the appropriate OpenCV DLL.

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


}