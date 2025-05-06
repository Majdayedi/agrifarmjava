package controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import entite.User;
import service.UserService;
import utils.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;


import java.io.File;
import java.util.List;

import static utils.AlertHelper.showAlert;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberMeCheckBox;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        // Load saved credentials if they exist
        if (CredentialManager.isRememberMeEnabled()) {
            emailField.setText(CredentialManager.getSavedEmail());
            passwordField.setText(CredentialManager.getSavedPassword());
            rememberMeCheckBox.setSelected(true);
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        User user = userService.loginUser(email, password);

        if (user != null) {
            if (!user.isVerified()) {
                showAlert("Account Not Verified", "Please verify your email before logging in.");

                // Optional: Redirect to verification scene
                Stage stage = (Stage) emailField.getScene().getWindow();
                SceneManager.switchScene(stage, "/controller/verification.fxml", (VerificationController controller) -> {
                    // You can optionally pass the user's email or ID
                });
                return;
            }

            // Save credentials if Remember Me is checked
            CredentialManager.saveCredentials(email, password, rememberMeCheckBox.isSelected());

            Session.getInstance().setUser(user); // Store globally
            showAlert("Success", "Welcome, " + user.getFirstName() + "!");
            Stage stage = (Stage) emailField.getScene().getWindow();

            if (user.getRoles().contains("ROLE_ADMIN")) {
                SceneManager.switchScene(stage, "/controller/AdminDashboard.fxml", (AdminDashboardController controller) -> {
                    controller.setUser(user);
                });
            } else {
                SceneManager.switchScene(stage, "/controller/UserDashboard.fxml", (UserDashboardController controller) -> {
                    controller.setUser(user);
                });
            }
        } else {
            showAlert("Error", "Invalid email or password.");
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
    @FXML
    private void handleFaceLogin() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Load OpenCV

        try {
            // Capture current face (not saved to disk)
            Mat currentFace = WebcamUtil.captureAndReturnFace();
            if (currentFace == null || currentFace.empty()) {
                showAlert("Face Login Failed", "No face detected.");
                return;
            }

            List<User> allUsers = userService.getAllUsers();
            double similarityThreshold = 0.85;

            for (User user : allUsers) {
                double totalScore = 0.0;
                int validSamples = 0;

                for (int i = 1; i <= 5; i++) {
                    File faceFile = new File("src/main/resources/faces/" + user.getEmail() + "_" + i + ".jpg");
                    if (!faceFile.exists()) continue;

                    Mat storedMat = Imgcodecs.imread(faceFile.getAbsolutePath());
                    if (storedMat.empty()) continue;

                    double score = FaceRecognitionUtil.getFaceSimilarityScore(currentFace, storedMat);
                    totalScore += score;
                    validSamples++;
                }

                if (validSamples > 0) {
                    double averageScore = totalScore / validSamples;
                    System.out.println("Average similarity score for " + user.getEmail() + ": " + averageScore);

                    if (averageScore >= similarityThreshold) {
                        if (!user.isVerified()) {
                            showAlert("Login Failed", "Please verify your email first.");
                            return;
                        }

                        Session.getInstance().setUser(user);
                        showAlert("Login Successful", "Welcome, " + user.getFirstName() + "!");
                        Stage stage = (Stage) emailField.getScene().getWindow();

                        if (user.getRoles().contains("ROLE_ADMIN")) {
                            SceneManager.switchScene(stage, "/controller/AdminDashboard.fxml", controller -> {
                                ((AdminDashboardController) controller).setUser(user);
                            });
                        } else {
                            SceneManager.switchScene(stage, "/controller/UserDashboard.fxml", controller -> {
                                ((UserDashboardController) controller).setUser(user);
                            });
                        }
                        return;
                    }
                }
            }

            showAlert("Login Failed", "No matching face found.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred: " + e.getMessage());
        }
    }





    @FXML
    private void goToForgotPassword() {
        SceneManager.switchScene((Stage) emailField.getScene().getWindow(), "/controller/forgot-password.fxml");
    }
}
