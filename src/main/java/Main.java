import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Load FXML file
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/controller/login.fxml")));

        // Create scene
        Scene scene = new Scene(root, 520, 440);

        // Load and apply stylesheet (optional, if you have styles.css)
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());

        // Set stage
        stage.setTitle("AgriFarm - Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
