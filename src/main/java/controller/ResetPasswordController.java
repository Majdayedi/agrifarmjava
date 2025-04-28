package controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import service.UserService;
import utils.AlertHelper;
import utils.SceneManager;

public class ResetPasswordController {
    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    private final UserService userService = new UserService();
    private String userEmail;

    public void setEmail(String email) {
        this.userEmail = email;
    }

    @FXML
    private void handlePasswordReset() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (password.isEmpty() || confirmPassword.isEmpty()) {
            AlertHelper.showAlert("Error", "Please fill in all fields.");
            return;
        }

        if (password.length() < 8) {
            AlertHelper.showAlert("Error", "Password must be at least 8 characters long.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            AlertHelper.showAlert("Error", "Passwords do not match.");
            return;
        }

        // Update password
        boolean success = userService.updatePassword(userEmail, password);
        if (success) {
            AlertHelper.showAlert("Success", "Password has been reset successfully. You can now login with your new password.");
            goToLogin();
        } else {
            AlertHelper.showAlert("Error", "Failed to reset password. Please try again.");
        }
    }

    private void goToLogin() {
        SceneManager.switchScene((Stage) passwordField.getScene().getWindow(), "/controller/login.fxml");
    }
}