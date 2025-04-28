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
import java.util.List;

public class UserDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private ImageView profileImageView;

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
        if (imageName != null && !imageName.isEmpty()) {
            File file = new File("src/user_data/profile_pics/" + imageName);
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
        try {
            Image image = new Image(getClass().getResource("/profile_pics/default.jpg").toExternalForm());
            profileImageView.setImage(image);
        } catch (Exception e) {
            System.out.println("Default profile image not found in resources!");
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
