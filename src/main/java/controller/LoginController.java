package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import entite.User;
import service.UserService;
import utils.AlertHelper;
import utils.SceneManager;
import utils.Session;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private final UserService userService = new UserService();

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        User user = userService.loginUser(email, password);

        if (user != null) {
            Session.getInstance().setUser(user); // Store globally

            // Show a success alert
            AlertHelper.showAlert("Success", "Welcome, " + user.getFirstName() + "!");

            try {
                // Load the home view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
                Parent homeView = loader.load();
                
                // Get the current stage
                Stage stage = (Stage) emailField.getScene().getWindow();
                
                // Create new scene with home view
                Scene scene = new Scene(homeView);
                
                // Set the scene to the stage
                stage.setTitle("AgriFarm - Home");
                stage.setScene(scene);
                stage.setMaximized(true); // Optional: maximize the window for better view
                stage.show();
                
            } catch (Exception e) {
                e.printStackTrace();
                AlertHelper.showAlert("Error", "Error loading home page: " + e.getMessage());
            }
        } else {
            AlertHelper.showAlert("Error", "Invalid email or password.");
        }
    }



    @FXML
    private void goToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Register");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
