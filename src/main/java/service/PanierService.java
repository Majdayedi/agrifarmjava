package service;

import entite.Commande;
import entite.Panier;
import entite.Produit;
import entite.User;
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
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

public class PanierService implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(PanierService.class.getName());

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
    private final CommandeService commandeService = new CommandeService();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
    private final String IMAGE_DIRECTORY = "src/main/resources/images/";

    private static final List<String> TYPES_COMMANDE = Arrays.asList(
            "Achat direct", "Précommande", "Réservation", "Livraison"
    );

    private static final List<String> MODES_PAIEMENT = Arrays.asList(
            "Carte bancaire", "Espèces", "Chèque", "Virement bancaire", "Paiement à la livraison"
    );

    private Connection connection;
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Récupérer l'instance du panier
        panier = Panier.getInstance();

        // Récupérer l'utilisateur courant depuis la session
        currentUser = utils.Session.getInstance().getUser();

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
        imageView.setFitWidth(60);
        imageView.setFitHeight(60);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);

        // Load image asynchronously
        Platform.runLater(() -> {
            try {
                String imagePath = produit.getImage_file_name();
                if (imagePath != null && !imagePath.isEmpty()) {
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString(), 60, 60, true, true);
                        imageView.setImage(image);
                    } else {
                        // Essayer comme un chemin relatif depuis les ressources
                        String relativePath = IMAGE_DIRECTORY + new File(imagePath).getName();
                        File relativeFile = new File(relativePath);

                        if (relativeFile.exists()) {
                            Image image = new Image(relativeFile.toURI().toString(), 60, 60, true, true);
                            imageView.setImage(image);
                        } else {
                            // Charger l'image par défaut
                            File defaultImage = new File(IMAGE_DIRECTORY + "default_product.png");
                            if (defaultImage.exists()) {
                                Image image = new Image(defaultImage.toURI().toString(), 60, 60, true, true);
                                imageView.setImage(image);
                            }
                        }
                    }
                } else {
                    // Charger l'image par défaut
                    File defaultImage = new File(IMAGE_DIRECTORY + "default_product.png");
                    if (defaultImage.exists()) {
                        Image image = new Image(defaultImage.toURI().toString(), 60, 60, true, true);
                        imageView.setImage(image);
                    }
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error loading image for product: " + produit.getId(), ex);
            }
        });

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
        if (currentUser == null) {
            showError("Erreur", "Vous devez être connecté pour passer une commande.");
            return;
        }

        if (adresseField.getText() == null || adresseField.getText().trim().isEmpty()) {
            showError("Erreur", "Veuillez saisir une adresse de livraison.");
            return;
        }

        String typePaiement = paiementCombo.getValue();
        if (typePaiement == null) {
            showError("Erreur", "Veuillez sélectionner un mode de paiement.");
            return;
        }

        // Vérifier la disponibilité des produits
        ProduitService produitService = new ProduitService();
        Map<Produit, Integer> produitsQuantites = panier.getProduitsQuantites();
        
        for (Map.Entry<Produit, Integer> entry : produitsQuantites.entrySet()) {
            Produit produit = entry.getKey();
            int quantiteDemandee = entry.getValue();
            
            // Récupérer la quantité actuelle du produit
            Produit produitActuel = produitService.read(produit.getId());
            if (produitActuel == null || produitActuel.getQuantite() < quantiteDemandee) {
                showError("Stock insuffisant", 
                    "Le produit '" + produit.getNom() + "' n'est plus disponible en quantité suffisante.\n" +
                    "Quantité disponible : " + (produitActuel != null ? produitActuel.getQuantite() : 0) + "\n" +
                    "Quantité demandée : " + quantiteDemandee);
                return;
            }
        }

        // Calculate total price
        double totalPrice = calculateTotalPrice();

        boolean paiementReussi = false;

        // Process payment based on payment type
        if (typePaiement.equals("Carte bancaire") || typePaiement.equals("Virement bancaire")) {
            try {
                StripePaymentService stripeService = new StripePaymentService();
                paiementReussi = stripeService.processPayment(totalPrice, "EUR");
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur de paiement", "Une erreur est survenue lors du traitement du paiement: " + e.getMessage());
                return;
            }
        } else {
            paiementReussi = true;
        }

        if (paiementReussi) {
            try {
                // Create the order
                Commande commande = new Commande();
                commande.setUserId(currentUser.getId());
                commande.setDate_creation_commande(new java.sql.Date(System.currentTimeMillis()));
                commande.setStatus(typePaiement.equals("Paiement à la livraison") ? "En attente de paiement" : "Confirmée");
                commande.setAdress(adresseField.getText());
                commande.setPrix(totalPrice);
                commande.setQuantite(panier.getNombreTotalArticles());
                commande.setType_commande(typeCommandeCombo.getValue());
                commande.setPaiment(typePaiement);

                // Ajouter les produits à la commande
                for (Map.Entry<Produit, Integer> entry : produitsQuantites.entrySet()) {
                    Produit produit = entry.getKey();
                    int quantite = entry.getValue();
                    commande.addProduit(produit, quantite);
                    
                    // Mettre à jour la quantité du produit
                    Produit produitActuel = produitService.read(produit.getId());
                    produitActuel.setQuantite(produitActuel.getQuantite() - quantite);
                    produitService.update(produitActuel);
                }

                // Save the order
                boolean success = commandeService.create(commande);

                if (success) {
                    // Clear the cart
                    panier.viderPanier();
                    chargerPanier();
                    mettreAJourRecapitulatif();
                    
                    String messageSuccess = "Votre commande a été passée avec succès!";
                    if (!typePaiement.equals("Carte bancaire") && !typePaiement.equals("Virement bancaire")) {
                        messageSuccess += "\nVeuillez préparer votre " + 
                            (typePaiement.equals("Espèces") ? "paiement en espèces" : 
                             typePaiement.equals("Chèque") ? "chèque" : 
                             "paiement") + " pour la livraison.";
                    }

                    // Envoyer l'email de confirmation
                    EmailService emailService = new EmailService();
                    boolean emailSent = emailService.sendConfirmationEmail(
                        currentUser.getEmail(),
                        "Client", // Nom générique
                        totalPrice
                    );

                    if (!emailSent) {
                        LOGGER.warning("L'email de confirmation n'a pas pu être envoyé.");
                    }

                    showSuccess("Succès", messageSuccess);

                    // Rediriger vers la page du marketplace
                    handleContinuerAchatsButtonAction(event);
                } else {
                    showError("Erreur", "Une erreur est survenue lors de la création de la commande.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur", "Une erreur est survenue lors de la création de la commande: " + e.getMessage());
            }
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

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void ajouterProduit(Produit produit, int quantite) {
        panier.ajouterProduit(produit, quantite);
    }

    public void retirerProduit(Produit produit) {
        panier.supprimerProduit(produit);
    }

    public Map<Produit, Integer> getPanier() {
        return panier.getProduitsQuantites();
    }

    public double getTotal() {
        return panier.calculerTotal();
    }

    public void viderPanier() {
        panier.viderPanier();
    }

    public boolean passerCommande(String adresse, String typePaiement) {
        if (currentUser == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Authentification requise");
            alert.setHeaderText(null);
            alert.setContentText("Vous devez être connecté pour passer une commande.");
            alert.showAndWait();
            return false;
        }

        if (adresse == null || adresse.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Adresse manquante");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez saisir une adresse de livraison.");
            alert.showAndWait();
            return false;
        }

        try {
            Commande commande = new Commande();
            commande.setUserId(currentUser.getId());
            commande.setAdress(adresse);
            commande.setPaiment(typePaiement);
            commande.setStatus("En attente");
            commande.setPrix(getTotal());
            commande.setQuantite(panier.getNombreTotalArticles());

            // Ajouter les produits à la commande
            for (Map.Entry<Produit, Integer> entry : panier.getProduitsQuantites().entrySet()) {
                commande.addProduit(entry.getKey(), entry.getValue());
            }

            // Sauvegarder la commande
            commandeService.create(commande);

            // Vider le panier après la commande
            viderPanier();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Commande passée");
            alert.setHeaderText(null);
            alert.setContentText("Votre commande a été enregistrée avec succès !");
            alert.showAndWait();

            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du passage de la commande", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue lors du passage de la commande.");
            alert.showAndWait();
            return false;
        }
    }

    public List<Commande> getCommandesUtilisateur() {
        if (currentUser == null) {
            return new ArrayList<>();
        }
        return commandeService.getCommandesByUser(currentUser.getId());
    }

    public void genererPDFCommande(Commande commande, Stage stage) {
        if (currentUser == null || commande.getUserId() != currentUser.getId()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Accès refusé");
            alert.setHeaderText(null);
            alert.setContentText("Vous n'avez pas accès à cette commande.");
            alert.showAndWait();
            return;
        }
        commandeService.generatePDF(commande, stage);
    }

    private double calculateTotalPrice() {
        return getTotal();
    }

    private void showError(String title, String content) {
        showAlert(Alert.AlertType.ERROR, title, null, content);
    }

    private void showSuccess(String title, String content) {
        showAlert(Alert.AlertType.INFORMATION, title, null, content);
    }
}