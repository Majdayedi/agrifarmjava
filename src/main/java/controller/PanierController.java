package controller;

import entite.Commande;
import entite.Panier;
import entite.Produit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PanierController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(PanierController.class.getName());

    @FXML private VBox panierItemsContainer;
    
    @FXML private Label nombreArticlesLabel;
    @FXML private Label totalPrixLabel;
    
    @FXML private ComboBox<String> typeCommandeCombo;
    @FXML private TextField adresseField;
    @FXML private ComboBox<String> paiementCombo;
    
    @FXML private Button viderPanierButton;
    @FXML private Button continuerAchatsButton;
    @FXML private Button passerCommandeButton;
    
    @FXML private Button agricoleButton;
    @FXML private Button adminButton;
    @FXML private Button marketplaceButton;
    @FXML private Button logoutButton;
    
    private Panier panier;
    private final CommandeController commandeController = new CommandeController();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
    private final String IMAGE_DIRECTORY = "src/main/resources/images/";

    private static final List<String> TYPES_COMMANDE = Arrays.asList(
        "Achat direct", "Précommande", "Réservation", "Livraison"
    );
    
    private static final List<String> MODES_PAIEMENT = Arrays.asList(
        "Carte bancaire", "Espèces", "Chèque", "Virement bancaire", "Paiement à la livraison"
    );
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Récupérer l'instance du panier
        panier = Panier.getInstance();
        
        // Initialiser les combos
        typeCommandeCombo.getItems().addAll(TYPES_COMMANDE);
        typeCommandeCombo.setValue(TYPES_COMMANDE.get(0));
        
        paiementCombo.getItems().addAll(MODES_PAIEMENT);
        paiementCombo.setValue(MODES_PAIEMENT.get(0));
        
        // Charger les données du panier
        chargerPanier();
        
        // Mettre à jour les labels de récapitulatif
        mettreAJourRecapitulatif();
    }
    
    private void chargerPanier() {
        // Vider le conteneur
        panierItemsContainer.getChildren().clear();
        
        // Ajouter chaque produit du panier
        for (Map.Entry<Produit, Integer> entry : panier.getProduitsQuantites().entrySet()) {
            Produit produit = entry.getKey();
            int quantite = entry.getValue();
            
            // Créer une carte pour chaque produit
            panierItemsContainer.getChildren().add(createPanierItemCard(produit, quantite));
        }
        
        // Si le panier est vide, afficher un message
        if (panierItemsContainer.getChildren().isEmpty()) {
            Label emptyLabel = new Label("Votre panier est vide");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-padding: 20px;");
            emptyLabel.setAlignment(Pos.CENTER);
            emptyLabel.setMaxWidth(Double.MAX_VALUE);
            panierItemsContainer.getChildren().add(emptyLabel);
        }
    }
    
    private Node createPanierItemCard(Produit produit, int quantite) {
        // Conteneur principal (HBox) pour la carte de produit
        HBox card = new HBox();
        card.getStyleClass().add("panier-item");
        card.setSpacing(10);
        card.setAlignment(Pos.CENTER_LEFT);
        
        // Image du produit
        ImageView imageView = new ImageView();
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);
        
        try {
            String imagePath = produit.getImage_file_name();
            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    imageView.setImage(image);
                } else {
                    // Essayer comme un chemin relatif depuis les ressources
                    String relativePath = IMAGE_DIRECTORY + new File(imagePath).getName();
                    File relativeFile = new File(relativePath);
                    
                    if (relativeFile.exists()) {
                        Image image = new Image(relativeFile.toURI().toString());
                        imageView.setImage(image);
                    } else {
                        // Charger l'image par défaut
                        File defaultImage = new File(IMAGE_DIRECTORY + "default_product.png");
                        if (defaultImage.exists()) {
                            Image image = new Image(defaultImage.toURI().toString());
                            imageView.setImage(image);
                        }
                    }
                }
            } else {
                // Charger l'image par défaut
                File defaultImage = new File(IMAGE_DIRECTORY + "default_product.png");
                if (defaultImage.exists()) {
                    Image image = new Image(defaultImage.toURI().toString());
                    imageView.setImage(image);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error loading image for product: " + produit.getId(), ex);
        }
        
        // Centre l'image dans un StackPane
        StackPane imageContainer = new StackPane();
        imageContainer.getChildren().add(imageView);
        imageContainer.setPrefWidth(80);
        imageContainer.setPrefHeight(80);
        imageContainer.getStyleClass().add("image-container");
        
        // Détails du produit
        VBox detailsBox = new VBox(5);
        detailsBox.getStyleClass().add("panier-item-details");
        detailsBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(detailsBox, Priority.ALWAYS);
        
        Label nomLabel = new Label(produit.getNom());
        nomLabel.getStyleClass().add("panier-item-title");
        
        Label prixLabel = new Label(currencyFormat.format(produit.getPrix()) + " / unité");
        prixLabel.getStyleClass().add("panier-item-price");
        
        Label quantiteLabel = new Label("Quantité: " + quantite);
        quantiteLabel.getStyleClass().add("panier-item-quantity");
        
        detailsBox.getChildren().addAll(nomLabel, prixLabel, quantiteLabel);
        
        // Total pour ce produit
        VBox totalBox = new VBox();
        totalBox.setAlignment(Pos.CENTER);
        totalBox.setPrefWidth(120);
        
        double totalProduit = produit.getPrix() * quantite;
        Label totalLabel = new Label(currencyFormat.format(totalProduit));
        totalLabel.getStyleClass().add("panier-item-total");
        
        totalBox.getChildren().add(totalLabel);
        
        // Actions
        VBox actionsBox = new VBox(5);
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.setPrefWidth(100);
        
        Button supprimerButton = new Button("Supprimer");
        supprimerButton.getStyleClass().add("delete-button");
        supprimerButton.setOnAction(e -> {
            supprimerProduit(produit);
        });
        
        actionsBox.getChildren().add(supprimerButton);
        
        // Ajouter tous les éléments à la carte
        card.getChildren().addAll(imageContainer, detailsBox, totalBox, actionsBox);
        
        return card;
    }
    
    private void mettreAJourRecapitulatif() {
        nombreArticlesLabel.setText(String.valueOf(panier.getNombreTotalArticles()));
        totalPrixLabel.setText(currencyFormat.format(panier.calculerTotal()));
        
        // Désactiver le bouton de commander si le panier est vide
        passerCommandeButton.setDisable(panier.estVide());
        viderPanierButton.setDisable(panier.estVide());
    }
    
    private void supprimerProduit(Produit produit) {
        panier.supprimerProduit(produit);
        chargerPanier();
        mettreAJourRecapitulatif();
    }
    
    @FXML
    void handleViderPanierButtonAction(ActionEvent event) {
        // Demander confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Vider le panier");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir vider votre panier ?");
        confirmation.setContentText("Cette action supprimera tous les produits de votre panier.");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                panier.viderPanier();
                chargerPanier();
                mettreAJourRecapitulatif();
            }
        });
    }
    
    @FXML
    void handleContinuerAchatsButtonAction(ActionEvent event) {
        try {
            // Charger la vue du marketplace
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/marketplace.fxml"));
            Parent marketplaceView = loader.load();
            
            // Get current stage
            Stage stage = (Stage) continuerAchatsButton.getScene().getWindow();
            
            // Create new scene with Marketplace view
            Scene scene = new Scene(marketplaceView);
            
            // Set the scene to the stage
            stage.setScene(scene);
            stage.setTitle("AgriFarm - Marketplace");
            stage.show();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading Marketplace view", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Impossible de charger la vue Marketplace", 
                    "Une erreur s'est produite: " + e.getMessage());
        }
    }
    
    @FXML
    void handlePasserCommandeButtonAction(ActionEvent event) {
        // Vérifier si l'adresse est fournie
        if (adresseField.getText() == null || adresseField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Adresse manquante", 
                    "Veuillez fournir une adresse de livraison", 
                    "L'adresse est requise pour finaliser la commande.");
            return;
        }
        
        // Créer la commande à partir du panier
        Commande commande = panier.creerCommande(
                adresseField.getText(),
                paiementCombo.getValue(),
                typeCommandeCombo.getValue()
        );
        
        if (commande == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Impossible de créer la commande", 
                    "Le panier est vide ou une erreur s'est produite.");
            return;
        }
        
        // Enregistrer la commande dans la base de données
        boolean success = commandeController.create(commande);
        
        if (success) {
            // Vider le panier après une commande réussie
            panier.viderPanier();
            
            // Afficher une confirmation
            showAlert(Alert.AlertType.INFORMATION, "Commande passée", 
                    "Votre commande a été passée avec succès", 
                    "Merci pour votre commande ! Vous pouvez suivre son état dans votre historique de commandes.");
            
            // Retourner au marketplace
            handleContinuerAchatsButtonAction(null);
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Impossible de finaliser la commande", 
                    "Une erreur s'est produite lors de l'enregistrement de la commande.");
        }
    }
    
    @FXML
    void handleAgricoleButtonAction(ActionEvent event) {
        try {
            // Charger la vue Agricole
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/produit.fxml"));
            Parent agricoleView = loader.load();
            
            // Get current stage
            Stage stage = (Stage) agricoleButton.getScene().getWindow();
            
            // Create new scene with Agricole view
            Scene scene = new Scene(agricoleView);
            
            // Set the scene to the stage
            stage.setScene(scene);
            stage.setTitle("AgriFarm - Agricole (CRUD)");
            stage.show();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading Agricole view", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Impossible de charger la vue Agricole", 
                    "Une erreur s'est produite: " + e.getMessage());
        }
    }
    
    @FXML
    void handleAdminButtonAction(ActionEvent event) {
        try {
            // Charger la vue Admin
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/admin.fxml"));
            Parent adminView = loader.load();
            
            // Get current stage
            Stage stage = (Stage) adminButton.getScene().getWindow();
            
            // Create new scene with Admin view
            Scene scene = new Scene(adminView);
            
            // Set the scene to the stage
            stage.setScene(scene);
            stage.setTitle("AgriFarm - Administration");
            stage.show();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading Admin view", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Impossible de charger la vue Admin", 
                    "Une erreur s'est produite: " + e.getMessage());
        }
    }
    
    @FXML
    void handleMarketplaceButtonAction(ActionEvent event) {
        handleContinuerAchatsButtonAction(event);
    }
    
    @FXML
    void handleLogoutButtonAction(ActionEvent event) {
        // Demander confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Déconnexion");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");
        confirmation.setContentText("Votre panier sera conservé pour votre prochaine connexion.");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Fermer l'application
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                stage.close();
            }
        });
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 