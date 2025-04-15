package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import entite.User;

public class UserDashboardController {
    // Store the user object
    @FXML
    private Label welcomeLabel;  // Label to show the welcome message

    @FXML
    private Label emailLabel;    // Label to show the user's email

    @FXML
    private Label nameLabel;     // Label to show the user's full name

    @FXML
    private ImageView profileImageView; // ImageView for the user's profile picture

    private User currentUser;

    // Method to set the user from the LoginController
    public void setUser(User user) {
        if (user == null) {
            System.out.println("User data is null!");
            return;
        }
        this.currentUser = user;

        // Update the dashboard with the user's details
        updateDashboard();
    }

    // Update the dashboard UI with the user's details
    private void updateDashboard() {
        if (currentUser != null) {
            // Set welcome message
            welcomeLabel.setText("Welcome, " + currentUser.getFirstName() + " " + currentUser.getLastName() + "!");
            emailLabel.setText("Email: " + currentUser.getEmail());
            nameLabel.setText("Name: " + currentUser.getFirstName() + " " + currentUser.getLastName());

            // Load the user's profile picture if available
            if (currentUser.getImageFileName() != null) {
                // Assuming the image is stored in a folder called 'images' inside the resources directory
                Image profileImage = new Image("file:resources/images/" + currentUser.getImageFileName());
                profileImageView.setImage(profileImage);
            }
        }
    }
    @FXML
    private void handleEditProfile() {
        if (currentUser != null) {
            // Store user in session if needed
            utils.Session.getInstance().setUser(currentUser);

            // Open Edit Profile screen
            javafx.stage.Stage stage = (javafx.stage.Stage) welcomeLabel.getScene().getWindow();
            utils.SceneManager.switchScene(stage, "/controller/edit_profile.fxml",
                    (EditProfileController controller) -> controller.setUser(currentUser));
        }
    }

    @FXML
    private void handleViewProfile() {
        if (currentUser != null) {
            javafx.stage.Stage stage = (javafx.stage.Stage) welcomeLabel.getScene().getWindow();
            utils.SceneManager.switchScene(stage, "/controller/view_profile.fxml",
                    (ViewProfileController controller) -> controller.setUserDetail(currentUser));
        }
    }

    @FXML
    private void handleLogout() {
        // Clear the session
        utils.Session.getInstance().setUser(null);

        // Redirect to login screen
        javafx.stage.Stage stage = (javafx.stage.Stage) welcomeLabel.getScene().getWindow();
        utils.SceneManager.switchScene(stage, "/controller/login.fxml", null);
    }

}
