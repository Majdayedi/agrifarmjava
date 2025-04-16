package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {
    
    @FXML
    private Button farmButton;
    
    @FXML
    private Button marketplaceButton;
    
    @FXML
    private Button articlesButton;
    
    @FXML
    private Button profileButton;
    
    @FXML
    public void navigateToFarm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/farmdisplay.fxml"));
            // Don't set the controller manually as it's already specified in the FXML
            Parent farmView = loader.load();
            
            Scene currentScene = farmButton.getScene();
            Stage primaryStage = (Stage) currentScene.getWindow();
            primaryStage.setTitle("Farm Management");
            primaryStage.setScene(new Scene(farmView, 900, 600));
        } catch (IOException e) {
            showAlert("Error loading Farm view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void navigateToMarketplace() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/produit.fxml"));
            Parent marketplaceView = loader.load();
            
            Scene currentScene = marketplaceButton.getScene();
            Stage primaryStage = (Stage) currentScene.getWindow();
            primaryStage.setTitle("Marketplace");
            primaryStage.setScene(new Scene(marketplaceView, 900, 600));
        } catch (IOException e) {
            showAlert("Error loading Marketplace view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void navigateToArticles() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/article_form.fxml"));
            Parent articlesView = loader.load();
            
            Scene currentScene = articlesButton.getScene();
            Stage primaryStage = (Stage) currentScene.getWindow();
            primaryStage.setTitle("Articles");
            primaryStage.setScene(new Scene(articlesView, 900, 600));
        } catch (IOException e) {
            showAlert("Error loading Articles view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void navigateToProfile() {
        try {
            // For now, let's display a message that this feature is coming soon
            showAlert("Profile feature is coming soon!");
        } catch (Exception e) {
            showAlert("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}