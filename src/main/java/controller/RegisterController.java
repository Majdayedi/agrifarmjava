package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import entite.User;
import service.FacebookTokenFetcher;
import service.FacebookUserService;
import service.UserService;
import utils.*;
import service.EmailService;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

public class RegisterController {
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ImageView profileImageView;
    @FXML
    private Label selectedImageLabel;
    @FXML
    private ImageView cameraView;

    private File selectedImageFile;
    private UserService userService = new UserService();
    private volatile boolean capturing = false;

    @FXML
    private void handleRegister() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill in all fields");
            return;
        }

        try {
            String imageFileName = null;
            if (selectedImageFile != null) {
                Path imagesDir = Path.of("src/user_data/profile_pics/");
                if (!Files.exists(imagesDir)) {
                    Files.createDirectory(imagesDir);
                }

                imageFileName = email + "_" + System.currentTimeMillis() + ".jpg";
                Path targetPath = imagesDir.resolve(imageFileName);
                Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            int userId = userService.registerUser(
                    email,
                    password,
                    Collections.singletonList("ROLE_USER"),
                    firstName,
                    lastName,
                    imageFileName
            );

            if (userId > 0) {
                startFaceCapture();

                EmailService emailService = new EmailService();
                String verificationToken = emailService.generateVerificationToken(userId);
                emailService.sendVerificationEmail(email, verificationToken);

                showAlert("Success", "Registration successful! Please check your email for verification.");
                goToLogin();
            } else {
                showAlert("Error", "Registration failed. Email might already be in use.");
            }
        } catch (Exception e) {
            showAlert("Error", "An error occurred during registration: " + e.getMessage());
        }
    }

    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        selectedImageFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
        if (selectedImageFile != null) {
            profileImageView.setImage(new Image(selectedImageFile.toURI().toString()));
            selectedImageLabel.setText(selectedImageFile.getName());
        }
    }

    @FXML
    private void goToLogin() {
        Stage stage = (Stage) profileImageView.getScene().getWindow();
        SceneManager.switchScene(stage, "/controller/login.fxml");
    }

    @FXML
    private void handleGoogleSignUp() {
        try {
            GoogleAuthConfig googleAuth = new GoogleAuthConfig();
            String authUrl = googleAuth.getAuthorizationUrl();

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(authUrl));
            } else {
                showAlert("Browser Error", "Unable to open the browser. Copy and paste this URL:\n" + authUrl);
                return;
            }

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Google Sign-Up");
            dialog.setHeaderText("Enter the authorization code from your browser");
            dialog.setContentText("Authorization Code:");

            Optional<String> codeResult = dialog.showAndWait();
            if (codeResult.isEmpty()) {
                showAlert("Cancelled", "Google sign-up was cancelled.");
                return;
            }

            String code = codeResult.get().trim();
            var userInfo = googleAuth.getUserInfo(code);
            String email = userInfo.getEmail();

            String encodedPassword = Base64.getEncoder()
                    .encodeToString(String.valueOf(System.currentTimeMillis()).getBytes());

            int userId = userService.registerUser(
                    email,
                    encodedPassword,
                    Collections.singletonList("ROLE_USER"),
                    userInfo.getGivenName(),
                    userInfo.getFamilyName(),
                    null
            );

            if (userId > 0) {
                User user = userService.getUserById(userId);
                if (user != null) {
                    user.setVerified(true);
                    userService.updateUser(user);
                }

                showAlert("Success", "You have been successfully registered via Google!");
                goToLogin();
            } else {
                showAlert("Registration Error", "Registration failed. This email may already be registered.");
            }

        } catch (Exception e) {
            showAlert("Google Sign-In Error", "An error occurred during Google sign-up:\n" + e.getMessage());
        }
    }
    @FXML
    private void handleFacebookSignUp() {
        try {
            FacebookAuth.launchLogin(); // Open browser for Facebook OAuth

            CallbackServer.start(code -> {
                try {
                    String accessToken = FacebookTokenFetcher.getAccessToken(code);
                    FacebookUserService.FacebookUser fbUser = FacebookUserService.fetchUserInfo(accessToken);

                    int userId = userService.registerFacebookUser(
                            fbUser.getEmail(),
                            fbUser.getFirstName(),
                            fbUser.getLastName(),
                            fbUser.getPictureUrl()
                    );

                    Platform.runLater(() -> {
                        if (userId > 0) {
                            // Optionally: auto-login user here
                            // Session.setCurrentUser(userService.getUserById(userId));

                            showAlert("Success", "You have been successfully registered via Facebook!");
                            goToLogin();
                        } else {
                            showAlert("Info", "This Facebook account is already registered. Please use 'Login with Facebook'.");
                            goToLogin();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() ->
                            showAlert("Facebook Sign-In Error", "An error occurred during Facebook sign-up:\n" + e.getMessage()));
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to open Facebook login page.");
        }
    }



    private void startFaceCapture() {
        String email = emailField.getText();
        if (email != null && !email.isEmpty()) {
           WebcamUtil.captureAndSaveMultipleFacesWithPreview(email, 3, cameraView);
        } else {
            showAlert("Error", "Email is required before capturing face.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
