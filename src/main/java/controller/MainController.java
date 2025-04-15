package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private StackPane userContentPane;
    @FXML private StackPane adminContentPane;

    private Parent userView;
    private Parent adminView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Load the user view (current CRUD interface)
            FXMLLoader userLoader = new FXMLLoader(getClass().getResource("/test/produit.fxml"));
            userView = userLoader.load();
            userContentPane.getChildren().add(userView);
            
            // Load the admin view
            FXMLLoader adminLoader = new FXMLLoader(getClass().getResource("/test/admin.fxml"));
            adminView = adminLoader.load();
            adminContentPane.getChildren().add(adminView);
            
            System.out.println("Views loaded successfully");
        } catch (IOException e) {
            System.err.println("Error loading views: " + e.getMessage());
            e.printStackTrace();
            
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load views");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    @FXML
    void handleLogoutAction(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Déconnexion");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir vous déconnecter?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Close the application
            Platform.exit();
        }
    }
} 