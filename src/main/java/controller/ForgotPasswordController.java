package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.UserService;
import service.EmailService;
import utils.AlertHelper;
import utils.SceneManager;

public class ForgotPasswordController {
    @FXML
    private TextField emailField;

    private final UserService userService = new UserService();
    private final EmailService emailService = new EmailService();

    @FXML
    private void handleResetRequest() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            AlertHelper.showAlert("Error", "Please enter your email address.");
            return;
        }

        // Check if email exists
        if (!userService.emailExists(email)) {
            AlertHelper.showAlert("Error", "No account found with this email address.");
            return;
        }

        // Generate reset token
        String token = emailService.generatePasswordResetToken(email);

        // Send reset email
        try {
            emailService.sendPasswordResetEmail(email, token);
            AlertHelper.showAlert("Success", "Password reset instructions have been sent to your email.");

            // Redirect to token verification page
            Stage stage = (Stage) emailField.getScene().getWindow();
            SceneManager.switchScene(stage, "/controller/verify-reset-token.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlert("Error", "Failed to send reset instructions. Please try again later.");
        }
    }

    @FXML
    private void goToLogin() {
        SceneManager.switchScene((Stage) emailField.getScene().getWindow(), "/controller/login.fxml");
    }
}