package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.EmailService;
import service.UserService;
import utils.AlertHelper;
import utils.SceneManager;

public class VerificationController {
    @FXML
    private TextField tokenField;

    private final EmailService emailService = new EmailService();
    private final UserService userService = new UserService();

    @FXML
    private void handleVerification() {
        String token = tokenField.getText().trim();

        if (token.isEmpty()) {
            AlertHelper.showAlert("Error", "Please enter the verification code.");
            return;
        }

        // Verify the token using the in-memory approach
        Integer userId = emailService.verifyToken(token);

        if (userId != null) {
            // Token is valid, update user's verification status
            boolean success = userService.verifyUser(userId);

            if (success) {
                AlertHelper.showAlert("Success", "Email verified successfully! You can now log in.");
                Stage stage = (Stage) tokenField.getScene().getWindow();
                SceneManager.switchScene(stage, "/controller/login.fxml");
            } else {
                AlertHelper.showAlert("Error", "Failed to update verification status. Please contact support.");
            }
        } else {
            AlertHelper.showAlert("Error", "Invalid or expired verification code. Please try again or request a new one.");
        }
    }

    @FXML
    private void goToLogin() {
        SceneManager.switchScene((Stage) tokenField.getScene().getWindow(), "/controller/login.fxml");
    }
}