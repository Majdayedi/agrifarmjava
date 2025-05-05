package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class MainFx extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the FXML file (make sure the path is correct)
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/controller/login.fxml")));

            // Create the scene with preferred size
            Scene scene = new Scene(root, 520, 440);

            // Optional: apply stylesheet
            String cssPath = "/css/StylesUser.css";
            if (getClass().getResource(cssPath) != null) {
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(cssPath)).toExternalForm());
            }

            // Set up the stage
            primaryStage.setTitle("AgriFarm - Login");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load FXML or CSS: " + e.getMessage());
        }
    }
}