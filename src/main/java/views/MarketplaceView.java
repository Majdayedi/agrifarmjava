package views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import models.User;

public class MarketplaceView extends BorderPane {
    private final User currentUser;

    public MarketplaceView(User currentUser) {
        this.currentUser = currentUser;
        setupTopBar();
        setupContent();
    }

    private void setupTopBar() {
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #f8f9fa;");

        Button panierButton = new Button("Panier");
        panierButton.setOnAction(e -> handlePanierButtonAction());
        
        Button commandesButton = new Button("Mes Commandes");
        commandesButton.setOnAction(e -> handleCommandesButtonAction());
        
        Button statsButton = new Button("Mes Statistiques");
        statsButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        statsButton.setOnAction(e -> handleStatsButtonAction());

        topBar.getChildren().addAll(statsButton, commandesButton, panierButton);
        setTop(topBar);
    }

    private void handleStatsButtonAction() {
        if (currentUser != null) {
            StatistiquesView statsView = new StatistiquesView(currentUser.getId());
            statsView.show();
        } else {
            showError("Erreur", "Vous devez être connecté pour voir vos statistiques.");
        }
    }

    private void setupContent() {
        // Implémentation du contenu du marketplace
    }

    private void handlePanierButtonAction() {
        // Implémentation de l'action du panier
    }

    private void handleCommandesButtonAction() {
        // Implémentation de l'action des commandes
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 