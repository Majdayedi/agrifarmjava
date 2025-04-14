import controller.FarmController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public void start(Stage primaryStage) {
        try {
            // Load the home.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            
            // Load the FXML
            Parent root = loader.load();
            
            // Create scene and set stage
            Scene scene = new Scene(root, 900.0, 600.0);
            primaryStage.setTitle("AgriFarm System");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}