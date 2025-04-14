package org.example.java1.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

public class SceneManager {

    // Overload: basic version still works
    public static void switchScene(Stage stage, String fxmlFile) {
        switchScene(stage, fxmlFile, controller -> {}); // do nothing by default
    }

    // Overload: pass controller setup logic
    public static <T> void switchScene(Stage stage, String fxmlFile, Consumer<T> controllerSetup) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlFile));
            Parent root = loader.load();

            // Get controller and apply setup
            T controller = loader.getController();
            controllerSetup.accept(controller);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
