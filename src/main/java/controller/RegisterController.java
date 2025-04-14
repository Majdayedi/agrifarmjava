package controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.UserService;
import utils.AlertHelper;
import utils.SceneManager;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class RegisterController {
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private ImageView profileImageView;  // Added for showing the selected image

    private final UserService userService = new UserService();

    private String imageFileName = null;  // Store the file name of the selected image

    // Email regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z]+$");

    @FXML
    private void handleRegister() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();

        // Validate input fields
        if (email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            AlertHelper.showAlert("Error", "All fields are required.");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            AlertHelper.showAlert("Error", "Invalid email format.");
            return;
        }

        if (password.length() < 8) {
            AlertHelper.showAlert("Error", "Password must be at least 8 characters long.");
            return;
        }

        if (!NAME_PATTERN.matcher(firstName).matches() || !NAME_PATTERN.matcher(lastName).matches()) {
            AlertHelper.showAlert("Error", "First name and last name must contain only letters.");
            return;
        }

        // 🔒 Check if email already exists
        if (userService.emailExists(email)) {
            AlertHelper.showAlert("Error", "Email already exists!");
            return;
        }

        // Assign default role as "ROLE_USER"
        List<String> roles = Collections.singletonList("ROLE_USER");

        // Proceed with registration
        boolean success = userService.registerUser(email, password, roles, firstName, lastName, imageFileName);

        if (success) {
            AlertHelper.showAlert("Success", "Registration successful! You can now log in.");
            Stage stage = (Stage) emailField.getScene().getWindow();
            SceneManager.switchScene(stage, "/controller/login.fxml");
        } else {
            AlertHelper.showAlert("Error", "Registration failed.");
        }
    }


    @FXML
    private void goToLogin() {
        SceneManager.switchScene((Stage) emailField.getScene().getWindow(), "/controller/login.fxml");
    }
    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File selectedFile = fileChooser.showOpenDialog((Stage) emailField.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Instead of src/main/resources, use an external writable folder
                String targetDir = "src/user_data/profile_pics/"; // you can change this path
                File dir = new File(targetDir);
                if (!dir.exists()) dir.mkdirs();

                String uniqueFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                File destination = new File(dir, uniqueFileName);

                java.nio.file.Files.copy(selectedFile.toPath(), destination.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                imageFileName = uniqueFileName;

                // Load the image into the UI from the external location
                profileImageView.setImage(new javafx.scene.image.Image(destination.toURI().toString()));

            } catch (Exception e) {
                e.printStackTrace();
                AlertHelper.showAlert("Error", "Failed to upload image.");
            }
        }
    }

}
