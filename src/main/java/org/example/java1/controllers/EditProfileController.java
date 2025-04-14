package org.example.java1.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.java1.entity.User;
import org.example.java1.services.UserService;
import org.example.java1.utils.AlertHelper;
import org.example.java1.utils.SceneManager;
import org.example.java1.utils.Session;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;

public class EditProfileController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;


    @FXML
    private CheckBox userRoleCheckBox;

    @FXML
    private CheckBox adminRoleCheckBox;

    @FXML
    private HBox rolesContainer;

    private final UserService userService = new UserService();
    private User currentUser;

    @FXML
    public void initialize() {
        if (Session.getInstance().getUser() != null) {
            setUser(Session.getInstance().getUser());
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        loadUserData();
    }

    private void loadUserData() {
        if (currentUser != null) {
            firstNameField.setText(currentUser.getFirstName());
            lastNameField.setText(currentUser.getLastName());
            emailField.setText(currentUser.getEmail());

            // Set role checkboxes
            List<String> roles = currentUser.getRoles();
            userRoleCheckBox.setSelected(roles.contains("ROLE_USER"));
            adminRoleCheckBox.setSelected(roles.contains("ROLE_ADMIN"));

            // Optional: Show role management only if user is admin
            boolean isAdmin = Session.getInstance().getUser().getRoles().contains("ROLE_ADMIN");
            rolesContainer.setVisible(isAdmin);
        }
    }

    @FXML
    private void handleSaveChanges() {
        String newFirstName = firstNameField.getText().trim();
        String newLastName = lastNameField.getText().trim();
        String newEmail = emailField.getText().trim();

        if (newFirstName.isEmpty() || newLastName.isEmpty() || newEmail.isEmpty()) {
            AlertHelper.showAlert("Validation Error", "All fields except password are required.");
            return;
        }

        currentUser.setFirstName(newFirstName);
        currentUser.setLastName(newLastName);
        currentUser.setEmail(newEmail);

        // Update roles
        List<String> updatedRoles = new ArrayList<>();
        if (userRoleCheckBox.isSelected()) updatedRoles.add("ROLE_USER");
        if (adminRoleCheckBox.isSelected()) updatedRoles.add("ROLE_ADMIN");
        currentUser.setRoles(updatedRoles);

        boolean success = userService.updateUser(currentUser);

        if (success) {
            AlertHelper.showAlert("Success", "Profile updated successfully.");
            ((Stage) firstNameField.getScene().getWindow()).close();
        } else {
            AlertHelper.showAlert("Error", "Failed to update profile.");
        }
    }

    @FXML
    private void handleBackToDashboard() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        SceneManager.switchScene(stage, "/org/example/java1/AdminDashboard.fxml", (AdminDashboardController controller) -> {
            controller.setUser(currentUser); // Pass back user to dashboard
        });
    }

    @FXML
    private void cancel() {
        ((Stage) firstNameField.getScene().getWindow()).close();
    }
}
