package org.example.java1.controllers;

import org.example.java1.entity.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.java1.utils.SceneManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AdminDashboardController {

    @FXML private Label welcomeLabel;

    @FXML private Button editProfileButton;
    @FXML private Button manageUsersButton;
    @FXML private Button logoutButton;
    @FXML private Button viewProfileButton;

    @FXML private ImageView profileImageView;

    private User currentUser;

    public void setUser(User user) {
        if (user == null) {
            System.out.println("User data is null!");
            return;
        }

        this.currentUser = user;

        // Display welcome message
        welcomeLabel.setText("Welcome, " + user.getEmail());

        // Display profile picture
        loadUserProfilePicture(user.getImageFileName());

        List<String> roles = user.getRoles();
        if (roles != null && roles.contains("ROLE_ADMIN")) {
            manageUsersButton.setVisible(true);
        } else {
            manageUsersButton.setVisible(false);
        }
    }

    private void loadUserProfilePicture(String imageName) {
        if (imageName != null && !imageName.isEmpty()) {
            File file = new File("user_data/profile_pics/" + imageName);
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                profileImageView.setImage(image);
            } else {
                System.out.println("Profile image not found. Loading default.");
                setDefaultProfileImage();
            }
        } else {
            setDefaultProfileImage();
        }
    }

    private void setDefaultProfileImage() {
        Image image = new Image(getClass().getResource("/profile_pics/default.jpg").toExternalForm());
        profileImageView.setImage(image);
    }

    @FXML
    private void handleEditProfile() {
        if (currentUser == null) return;

        Stage stage = (Stage) editProfileButton.getScene().getWindow();
        SceneManager.switchScene(stage, "/org/example/java1/edit_profile.fxml", (EditProfileController controller) -> {
            controller.setUser(currentUser);
        });
    }

    @FXML
    private void handleManageUsers() {
        if (currentUser == null || !currentUser.getRoles().contains("ROLE_ADMIN")) return;

        Stage stage = (Stage) manageUsersButton.getScene().getWindow();
        SceneManager.switchScene(stage, "/org/example/java1/manage_users.fxml", (ManageUsersController controller) -> {
            controller.setAdmin(currentUser);
        });
    }

    @FXML
    private void handleViewProfile() {
        if (currentUser == null) return;

        Stage stage = (Stage) viewProfileButton.getScene().getWindow();
        SceneManager.switchScene(stage, "/org/example/java1/view_profile.fxml", (ViewProfileController controller) -> {
            controller.setUserDetail(currentUser);
        });
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/java1/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
