package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import entite.User;
import org.apache.commons.lang3.StringUtils;
import utils.SceneManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ViewProfileController {

    @FXML private Label emailLabel;
    @FXML private Label rolesLabel;
    @FXML private Label firstNameLabel;
    @FXML private Label lastNameLabel;
    @FXML private ImageView profileImageView;

    private User currentUser;

    // Add these constants at the class level
    private static final String SHARED_PROFILE_DIR = "C:\\shared-profile-pics\\";
    private static final String DEFAULT_IMAGE_RESOURCE = SHARED_PROFILE_DIR + "default.jpg"; // Update to your actual resource path

    public void setUserDetail(User user) {
        if (user == null) {
            System.out.println("User data is null!");
            return;
        }

        this.currentUser = user;

        // Set text fields
        emailLabel.setText("Email: " + user.getEmail());
        rolesLabel.setText("Roles: " + String.join(", ", user.getRoles()));
        firstNameLabel.setText("First Name: " + user.getFirstName());
        lastNameLabel.setText("Last Name: " + user.getLastName());

        // Load profile picture
        String imageFileName = user.getImageFileName();
        if (StringUtils.isNotBlank(imageFileName)) {
            // 1. Try shared directory first
            File imageFile = new File(SHARED_PROFILE_DIR + imageFileName);
            System.out.println("Checking image in: " + imageFile.getAbsolutePath()); // Log the full path

            if (imageFile.exists()) {
                String imageUrl = imageFile.toURI().toString();
                System.out.println("Loaded image from shared folder.");
                profileImageView.setImage(new Image(imageUrl));
                return;
            }

            // 2. Fallback to legacy location (temporary during migration)
            File legacyFile = new File("src/user_data/profile_pics/" + imageFileName);
            if (legacyFile.exists()) {
                try {
                    // Migrate to shared directory
                    Files.copy(
                            legacyFile.toPath(),
                            Paths.get(SHARED_PROFILE_DIR + imageFileName),
                            StandardCopyOption.REPLACE_EXISTING
                    );
                    profileImageView.setImage(new Image(legacyFile.toURI().toString()));
                    System.out.println("Migrated image from legacy folder.");
                    return;
                } catch (IOException e) {
                    System.err.println("Failed to migrate profile image: " + e.getMessage());
                }
            }

            System.out.println("Profile image not found for: " + imageFileName);
        }

        loadDefaultProfileImage();
    }

    private void loadDefaultProfileImage() {
        try {
            // 1. Try shared directory first
            File defaultFile = new File(SHARED_PROFILE_DIR + "default.jpg");
            if (defaultFile.exists()) {
                profileImageView.setImage(new Image(defaultFile.toURI().toString()));
                System.out.println("Loaded default image from shared folder.");
                return;
            }

            // 2. Fallback to embedded resource
            InputStream defaultStream = getClass().getResourceAsStream(DEFAULT_IMAGE_RESOURCE);
            if (defaultStream != null) {
                profileImageView.setImage(new Image(defaultStream));
                System.out.println("Loaded default image from resources.");
            } else {
                System.err.println("Default image not found in resources: " + DEFAULT_IMAGE_RESOURCE);
                profileImageView.setImage(null); // Clear image view
            }
        } catch (Exception e) {
            System.err.println("Error loading default profile image: " + e.getMessage());
            profileImageView.setImage(null);
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) emailLabel.getScene().getWindow();
        SceneManager.switchScene(stage, "/controller/AdminDashboard.fxml",
                (AdminDashboardController controller) -> controller.setUser(currentUser));
    }
}
