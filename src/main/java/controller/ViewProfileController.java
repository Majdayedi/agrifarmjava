package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import entite.User;
import utils.SceneManager;

import java.io.File;
import java.net.URL;

public class ViewProfileController {

    @FXML private Label emailLabel;
    @FXML private Label rolesLabel;
    @FXML private Label firstNameLabel;
    @FXML private Label lastNameLabel;
    @FXML private ImageView profileImageView;

    private User currentUser;

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

        // Load profile picture from external folder
        String imageFileName = user.getImageFileName();
        if (imageFileName != null && !imageFileName.isEmpty()) {
            String imagePath = "src/user_data/profile_pics/" + imageFileName; // external path
            File imageFile = new File(imagePath);

            if (imageFile.exists()) {
                profileImageView.setImage(new Image(imageFile.toURI().toString()));
            } else {
                System.out.println("Profile image not found at: " + imagePath);
                loadDefaultProfileImage();
            }
        } else {
            loadDefaultProfileImage();
        }
    }

    private void loadDefaultProfileImage() {
        try {
            // Default image from resources (e.g., src/main/resources/profile_pics/default.jpg)
            URL defaultImageUrl = getClass().getResource("/profile_pics/default.jpg");
            if (defaultImageUrl != null) {
                profileImageView.setImage(new Image(defaultImageUrl.toExternalForm()));
            } else {
                System.err.println("Default image not found in /profile_pics/default.jpg");
            }
        } catch (Exception e) {
            System.err.println("Error loading default profile image: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) emailLabel.getScene().getWindow();
        SceneManager.switchScene(stage, "/controller/AdminDashboard.fxml",
                (AdminDashboardController controller) -> controller.setUser(currentUser));
    }
}
