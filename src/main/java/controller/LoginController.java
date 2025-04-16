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
            // Store the user in session
            Session.getInstance().setUser(user);

            // Show a success alert
            AlertHelper.showAlert("Success", "Welcome, " + user.getFirstName() + "!");

            Stage stage = (Stage) emailField.getScene().getWindow();

            if (user.getRoles().contains("ROLE_ADMIN")) {
                // Redirect to Admin Dashboard
                SceneManager.switchScene(stage, "/controller/adminDashboard.fxml", controller -> {
                    if (controller instanceof AdminDashboardController) {
                        ((AdminDashboardController) controller).setUser(user);
                    }
                });
            } else {
                // Redirect to User Dashboard
                SceneManager.switchScene(stage, "/home.fxml", controller -> {
                    if (controller instanceof UserDashboardController) {
                        ((UserDashboardController) controller).setUser(user);
                    }
                });
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
