package utils;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Utility class for managing JavaFX scene transitions with robust full-screen management
 */
public class SceneManager {
    // Static field to track full-screen mode status globally
    private static boolean fullScreenEnabled = true;
    
    // Map to track full-screen listeners for each stage
    private static final Map<Stage, ChangeListener<Boolean>> fullScreenListeners = new HashMap<>();
    /**
     * Switches to a new scene without controller configuration
     * 
     * @param stage The current stage
     * @param fxmlFile Path to the FXML file
     */
    public static void switchScene(Stage stage, String fxmlFile) {
        switchScene(stage, fxmlFile, controller -> {}); // do nothing by default
    }

    /**
     * Switches to a new scene and applies a controller configuration
     * 
     * @param stage The current stage
     * @param fxmlFile Path to the FXML file
     * @param controllerSetup Consumer to configure the controller
     * @param <T> Controller type
     */
    public static <T> void switchScene(Stage stage, String fxmlFile, Consumer<T> controllerSetup) {
        try {
            // Ensure the stage has our full-screen listener attached
            ensureFullScreenListener(stage);
            
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlFile));
            Parent root = loader.load();

            // Get controller and apply setup
            T controller = loader.getController();
            if (controllerSetup != null) {
                controllerSetup.accept(controller);
            }
            Scene scene = new Scene(root);
            stage.setScene(scene);
            
            // Apply full-screen settings based on global preference
            applyFullScreenSettings(stage);
            
            // Show the stage
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showAlert("Error", "Failed to load the requested page: " + e.getMessage());
        }
    }

    /**
     * Convenience method to switch to the verification page
     * 
     * @param stage The current stage
     */
    public static void switchToVerification(Stage stage) {
        switchScene(stage, "/controller/verification.fxml");
    }
    /**
     * Directly set a stage to full-screen mode
     * 
     * @param stage The stage to set to full-screen
     */
    public static void setStageToFullScreen(Stage stage) {
        // Configure the stage first
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.setFullScreenExitHint("Press ESC to exit full-screen mode");
        stage.setFullScreenExitKeyCombination(KeyCombination.valueOf("ESC"));
        
        // Ensure we have a listener
        ensureFullScreenListener(stage);
        
        // Enable full-screen globally
        fullScreenEnabled = true;
        
        // Apply full-screen with proper timing
        Platform.runLater(() -> {
            Platform.runLater(() -> {
                stage.setFullScreen(true);
            });
        });
    }
    
    /**
     * Enable full-screen mode globally for the application
     * Enable full-screen mode globally for the application
     */
    public static void enableFullScreen() {
        fullScreenEnabled = true;
    }
    
    /**
     * Disable full-screen mode globally for the application
     */
    public static void disableFullScreen() {
        fullScreenEnabled = false;
    }
    /**
     * Check if full-screen mode is enabled globally
     * 
     * @return true if full-screen is enabled
     */
    public static boolean isFullScreenEnabled() {
        return fullScreenEnabled;
    }
    
    /**
     * Applies full-screen settings to the stage based on global preference
     * 
     * @param stage The stage to configure
     */
    private static void applyFullScreenSettings(Stage stage) {
        // Set base window properties
        stage.setResizable(true);
        stage.setMaximized(true);
        
        // Configure full-screen exit behavior
        stage.setFullScreenExitHint("Press ESC to exit full-screen mode");
        stage.setFullScreenExitKeyCombination(KeyCombination.valueOf("ESC"));
        
        // Apply full-screen if enabled globally
        if (fullScreenEnabled) {
            // Use multiple runLater calls to ensure proper sequencing
            Platform.runLater(() -> {
                // First make sure we're maximized
                stage.setMaximized(true);
                
                // Then schedule full-screen in another runLater for better timing
                Platform.runLater(() -> {
                    // Finally set full-screen
                    stage.setFullScreen(true);
                });
            });
        }
    }
    
    /**
     * Ensures the stage has a full-screen listener attached
     * 
     * @param stage The stage to attach the listener to
     */
    private static void ensureFullScreenListener(Stage stage) {
        // Remove existing listener if present
        if (fullScreenListeners.containsKey(stage)) {
            stage.fullScreenProperty().removeListener(fullScreenListeners.get(stage));
            fullScreenListeners.remove(stage);
        }
        
        // Create new listener that updates global preference when user exits full-screen
        ChangeListener<Boolean> listener = (observable, oldValue, newValue) -> {
            if (!newValue) {
                // User exited full-screen mode
                fullScreenEnabled = false;
            } else {
                // User entered full-screen mode
                fullScreenEnabled = true;
            }
        };
        
        // Attach listener and store reference
        stage.fullScreenProperty().addListener(listener);
        fullScreenListeners.put(stage, listener);
    }
}
