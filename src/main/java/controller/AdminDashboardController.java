package controller;

import entite.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import utils.SceneManager;
import utils.Session;
import utils.CredentialManager;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class AdminDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Button editProfileButton;
    @FXML private Button manageUsersButton;
    @FXML private Button logoutButton;
    @FXML private Button viewProfileButton;
    @FXML private Button produitButton;
    @FXML private ImageView profileImageView;

    private static final String SHARED_PROFILE_DIR = "C:\\shared-profile-pics\\";
    private static final String DEFAULT_IMAGE_PATH = SHARED_PROFILE_DIR + "default.jpg";

    private User currentUser;

    public void setUser(User user) {
        if (user == null) {
            System.out.println("User data is null!");
            return;
        }

        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getEmail());

        loadUserProfilePicture(user.getImageFileName());

        List<String> roles = user.getRoles();
        manageUsersButton.setVisible(roles != null && roles.contains("ROLE_ADMIN"));
    }

    private void loadUserProfilePicture(String imageName) {
        try {
            if (StringUtils.isNotBlank(imageName)) {
                File imageFile = new File(SHARED_PROFILE_DIR + imageName);
                System.out.println("Checking image in: " + imageFile.getAbsolutePath());

                if (imageFile.exists()) {
                    String imageUrl = imageFile.toURI().toString() + "?t=" + System.currentTimeMillis();
                    profileImageView.setImage(new Image(imageUrl));
                    System.out.println("Loaded image from shared folder.");
                    return;
                }

                File legacyFile = new File("src/user_data/profile_pics/" + imageName);
                if (legacyFile.exists()) {
                    Path target = Paths.get(SHARED_PROFILE_DIR, imageName);
                    Files.copy(legacyFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
                    profileImageView.setImage(new Image(target.toUri().toString()));
                    System.out.println("Migrated and loaded legacy image.");
                    return;
                }
            }

            setDefaultProfileImage();

        } catch (Exception e) {
            System.err.println("Error loading profile image: " + e.getMessage());
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
                System.err.println("Default profile image not found at: " + DEFAULT_IMAGE_PATH);
                profileImageView.setImage(null); // Optional: show placeholder
            }
        } catch (Exception e) {
            System.err.println("Error loading default profile image: " + e.getMessage());
            profileImageView.setImage(null);
        }
    }

    @FXML
    private void handleEditProfile() {
        if (currentUser == null) return;

        Stage stage = (Stage) editProfileButton.getScene().getWindow();
        SceneManager.switchScene(stage, "/controller/edit_profile.fxml", controller -> {
            if (controller instanceof EditProfileController) {
                ((EditProfileController) controller).setUser(currentUser);
            }
        });
    }

    @FXML
    private void handleManageUsers() {
        if (currentUser == null || !currentUser.getRoles().contains("ROLE_ADMIN")) return;

        Stage stage = (Stage) manageUsersButton.getScene().getWindow();
        SceneManager.switchScene(stage, "/controller/manage_users.fxml", controller -> {
            if (controller instanceof ManageUsersController) {
                ((ManageUsersController) controller).setAdmin(currentUser);
            }
        });
    }

    @FXML
    private void handleViewProfile() {
        if (currentUser == null) return;

        Stage stage = (Stage) viewProfileButton.getScene().getWindow();
        SceneManager.switchScene(stage, "/controller/view_profile.fxml", controller -> {
            if (controller instanceof ViewProfileController) {
                ((ViewProfileController) controller).setUserDetail(currentUser);
            }
        });
    }

    @FXML
    private void handleLogout() {
        Session.getInstance().setUser(null);
        CredentialManager.clearCredentials();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProduitButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/admin.fxml")); // Adjust if needed
            Parent root = loader.load();

            Stage stage = (Stage) produitButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Produits");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
