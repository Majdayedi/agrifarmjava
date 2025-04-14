import controller.FarmController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public void start(Stage primaryStage) {
        try {
            // 1. Create loader instance
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ressources/farm.fxml"));

            // 2. Set controller manually
            loader.setController(new FarmController());  // Uses your existing controller

            // 3. Load the FXML
            Parent root = loader.load();

            // 4. Rest remains the same
            Scene scene = new Scene(root, 900.0, 600.0);
            primaryStage.setTitle("Farm Management System");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}