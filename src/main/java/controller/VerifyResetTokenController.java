package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.EmailService;
import utils.AlertHelper;
import utils.SceneManager;

public class VerifyResetTokenController {
    @FXML
    private TextField tokenField;

    private final EmailService emailService = new EmailService();

    @FXML
    private void handleTokenVerification() {
        String token = tokenField.getText().trim();

        if (token.isEmpty()) {
            AlertHelper.showAlert("Error", "Please enter the reset token.");
            return;
        }

        // Verify token and get user email
        String email = emailService.verifyPasswordResetToken(token);
        if (email == null) {
            AlertHelper.showAlert("Error", "Invalid or expired reset token. Please request a new one.");
            goToLogin();
            return;
        }

        // Token is valid, redirect to reset password page
        Stage stage = (Stage) tokenField.getScene().getWindow();
        SceneManager.switchScene(stage, "/controller/reset-password.fxml", (ResetPasswordController controller) -> {
            controller.setEmail(email);
        });
    }

    @FXML
    private void goToLogin() {
        SceneManager.switchScene((Stage) tokenField.getScene().getWindow(), "/controller/login.fxml");
    }
}