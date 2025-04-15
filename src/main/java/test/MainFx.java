package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

import java.io.IOException;

public class MainFx extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the FXML file (make sure the path is correct)
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controller/login.fxml"));
            Parent root = fxmlLoader.load();

            // Create the scene
            Scene scene = new Scene(root);

            // Set up the stage
            primaryStage.setTitle("Projet");
            primaryStage.setWidth(1200);  // Default width in pixels
            primaryStage.setHeight(800);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load FXML file: " + e.getMessage());
        }
    }
}