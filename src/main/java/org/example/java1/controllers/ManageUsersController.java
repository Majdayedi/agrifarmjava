package org.example.java1.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.java1.entity.User;
import org.example.java1.utils.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.java1.services.UserService;

import java.io.IOException;
import java.util.List;

public class ManageUsersController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> firstNameColumn;
    @FXML private TableColumn<User, String> lastNameColumn;

    private final UserService userService = new UserService();
    private User admin;


    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        loadUsers();
    }

    private void loadUsers() {
        List<User> users = userService.getAllUsers();
        ObservableList<User> userList = FXCollections.observableArrayList(users);
        userTable.setItems(userList);
    }
    @FXML
    private void handleViewUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            String userInfo = String.format(
                    "ID: %d\nEmail: %s\nFirst Name: %s\nLast Name: %s\nRoles: %s\nProfile Pic: %s",
                    selectedUser.getId(),
                    selectedUser.getEmail(),
                    selectedUser.getFirstName(),
                    selectedUser.getLastName(),
                    selectedUser.getRoles().toString(),
                    selectedUser.getImageFileName()
            );
            AlertHelper.showAlert("User Info", userInfo);
        } else {
            AlertHelper.showAlert("Warning", "No user selected.");
        }
    }
    @FXML
    private void handleUpdateUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/java1/edit_profile.fxml"));
                Parent root = loader.load();

                EditProfileController controller = loader.getController();
                controller.setUser(selectedUser); // Set selected user in the edit controller

                Stage stage = new Stage();
                stage.setTitle("Edit User");
                stage.setScene(new Scene(root));
                stage.showAndWait();

                loadUsers(); // Refresh the table after the window is closed
            } catch (IOException e) {
                e.printStackTrace();
                AlertHelper.showAlert("Error", "Could not open edit window.");
            }
        } else {
            AlertHelper.showAlert("Warning", "No user selected.");
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            boolean deleted = userService.deleteUser(selectedUser.getId());
            if (deleted) {
                AlertHelper.showAlert("Success", "User deleted.");
                loadUsers();
            } else {
                AlertHelper.showAlert("Error", "Failed to delete user.");
            }
        } else {
            AlertHelper.showAlert("Warning", "No user selected.");
        }
    }

    public void setAdmin(User user) {
        this.admin = user;
    }

}