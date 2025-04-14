package org.example.java1.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.java1.entity.User;
import org.example.java1.services.UserService;
import org.example.java1.utils.AlertHelper;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.example.java1.utils.SceneManager;
import org.example.java1.utils.Session;

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

            Stage stage = (Stage) emailField.getScene().getWindow();

            // Check if the user is an admin or a normal user
            if (user.getRoles().contains("ROLE_ADMIN")) {
                // Redirect to Admin Dashboard
                SceneManager.switchScene(stage, "/org/example/java1/adminDashboard.fxml", (AdminDashboardController controller) -> {
                    controller.setUser(user); // Pass user to the controller
                });
            } else {
                // Redirect to User Dashboard
                SceneManager.switchScene(stage, "/org/example/java1/userDashboard.fxml", (UserDashboardController controller) -> {
                    controller.setUser(user); // Pass user to the controller
                });
            }
        } else {
            AlertHelper.showAlert("Error", "Invalid email or password.");
        }
    }



    @FXML
    private void goToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/java1/register.fxml"));
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
