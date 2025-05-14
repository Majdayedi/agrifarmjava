package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import entite.User;
import utils.SceneManager;
import utils.Session;
import utils.CredentialManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class UserDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private ImageView profileImageView;

    private static final String PROFILE_PICS_DIR = "C:\\shared-profile-pics\\";
    private static final String DEFAULT_IMAGE_PATH = PROFILE_PICS_DIR + "default.jpg";

    private User currentUser;

    public void setUser(User user) {
        if (user == null) {
            System.out.println("User data is null!");
            return;
        }

        this.currentUser = user;
        updateDashboard();
    }

    private void updateDashboard() {
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFirstName() + " " + currentUser.getLastName() + "!");
            emailLabel.setText("Email: " + currentUser.getEmail());
            nameLabel.setText("Name: " + currentUser.getFirstName() + " " + currentUser.getLastName());

            loadUserProfilePicture(currentUser.getImageFileName());
        }
    }

    private void loadUserProfilePicture(String imageName) {
        try {
            if (imageName != null && !imageName.isEmpty()) {
                File sharedFile = new File(PROFILE_PICS_DIR + imageName);
                System.out.println("Checking image in: " + sharedFile.getAbsolutePath());

                if (sharedFile.exists()) {
                    String uri = sharedFile.toURI().toString() + "?t=" + System.currentTimeMillis();
                    profileImageView.setImage(new Image(uri));
                    System.out.println("Loaded image from shared folder.");
                } else {
                    File legacyFile = new File("src/user_data/profile_pics/" + imageName);
                    if (legacyFile.exists()) {
                        // Copy to shared directory for future use
                        Files.copy(
                                legacyFile.toPath(),
                                Path.of(PROFILE_PICS_DIR + imageName),
                                StandardCopyOption.REPLACE_EXISTING
                        );
                        profileImageView.setImage(new Image(legacyFile.toURI().toString()));
                        System.out.println("Loaded image from legacy folder.");
                    } else {
                        System.out.println("Profile image not found. Loading default image.");
                        setDefaultProfileImage();
                    }
                }
            } else {
                System.out.println("No image name provided. Loading default image.");
                setDefaultProfileImage();
            }
        } catch (Exception e) {
            System.err.println("Error loading profile picture: " + e.getMessage());
            setDefaultProfileImage();
        }
    }

    private void setDefaultProfileImage() {
        try {
            File defaultFile = new File(DEFAULT_IMAGE_PATH);
            if (defaultFile.exists()) {
                profileImageView.setImage(new Image(defaultFile.toURI().toString()));
                System.out.println("Loaded default profile image.");
            } else {
                System.err.println("Default profile image not found at " + DEFAULT_IMAGE_PATH);
                profileImageView.setImage(null); // Optional: show placeholder
            }
        } catch (Exception e) {
            System.err.println("Error loading default profile image: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditProfile() {
        if (currentUser != null) {
            Session.getInstance().setUser(currentUser);

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            SceneManager.switchScene(stage, "/controller/edit_profile.fxml",
                    (EditProfileController controller) -> controller.setUser(currentUser));
        }
    }

    @FXML
    private void handleViewProfile() {
        if (currentUser != null) {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            SceneManager.switchScene(stage, "/controller/view_profile.fxml",
                    (ViewProfileController controller) -> controller.setUserDetail(currentUser));
        }
    }

    @FXML
    private void handleLogout() {
        Session.getInstance().setUser(null);
        CredentialManager.clearCredentials();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
