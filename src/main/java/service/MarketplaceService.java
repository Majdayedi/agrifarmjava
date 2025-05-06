package service;

import entite.Commande;
import entite.Panier;
import entite.Produit;
import entite.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import utils.Connections;
import utils.Session;
import views.StatistiquesView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

public class MarketplaceService implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(MarketplaceService.class.getName());

    @FXML
    private FlowPane productCardsPane;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> filterCategoryCombo;

    @FXML
    private ComboBox<String> filterPriceCombo;

    @FXML
    private Label statisticsLabel;

    @FXML
    private Button produitButton;

    @FXML
    private Button panierButton;

    @FXML
    private Label panierCountLabel;

    @FXML
    private Button homeButton;

    @FXML
    private Button ordersButton;

    @FXML
    private Button statsButton;

    private ProduitService produitService;
    private ObservableList<Produit> productsList = FXCollections.observableArrayList();
    private int totalProducts = 0;
    private final String IMAGE_DIRECTORY = "src/main/resources/images/";
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);

    private final List<String> CATEGORIES = Arrays.asList(
            "Toutes", "Fruits", "Légumes", "Céréales", "Produits laitiers", "Viandes", "Outils agricoles", "Semences", "Engrais", "Autre"
    );

    private final List<String> PRICE_RANGES = Arrays.asList(
            "Tous les prix", "Moins de 10 DT", "10 DT - 50 DT", "50 DT - 100 DT", "Plus de 100 DT"
    );

    private User currentUser;
    private final CommandeService commandeService = new CommandeService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        produitService = new ProduitService();

        // Get the current user from session
        this.currentUser = Session.getInstance().getUser();

        // Vérifier que nous avons une connexion valide à la base de données
        if (produitService.getConnection() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de se connecter à la base de données",
                    "Veuillez vérifier votre configuration de base de données et réessayer.");
            return;
        }

        // Mise à jour du compteur de panier
        updatePanierCount();

        // Initialize filter combos
        filterCategoryCombo.getItems().addAll(CATEGORIES);
        filterCategoryCombo.setValue("Toutes");

        filterPriceCombo.getItems().addAll(PRICE_RANGES);
        filterPriceCombo.setValue("Tous les prix");

        // Add listeners for filters
        filterCategoryCombo.setOnAction(e -> loadApprovedProducts());
        filterPriceCombo.setOnAction(e -> loadApprovedProducts());

        // Load products initially
        loadApprovedProducts();

        if (produitButton != null) {
            produitButton.setOnAction(event -> handleAgricoleButtonAction(event));
        }
    }

    @FXML
    public void handleAgricoleButtonAction(ActionEvent event) {
        try {
            // Charger la vue Agricole
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/produit.fxml"));
            Parent agricoleView = loader.load();

            // Obtenir la fenêtre actuelle
            Stage stage = (Stage) produitButton.getScene().getWindow();

            // Créer une nouvelle scène avec la vue Agricole
            Scene scene = new Scene(agricoleView);

            // Définir la scène sur la fenêtre
            stage.setScene(scene);
            stage.setTitle("AgriFarm - Produits");
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement de la vue Produits", e);
            showError("Erreur de Navigation", "Impossible de charger la vue Produits: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearchButtonAction() {
        // Vérifier si le champ de recherche contient des caractères spéciaux SQL
        String searchText = searchField.getText();
        if (searchText != null && !searchText.isEmpty()) {
            if (containsSQLInjection(searchText)) {
                showAlert(Alert.AlertType.WARNING, "Attention",
                        "Recherche non valide",
                        "Votre recherche contient des caractères non autorisés.");
                return;
            }
        }
        loadApprovedProducts();
    }

    /**
     * Vérifie si une chaîne contient des caractères potentiellement utilisés pour l'injection SQL
     * @param input la chaîne à vérifier
     * @return true si la chaîne contient des caractères suspects
     */
    private boolean containsSQLInjection(String input) {
        String[] sqlKeywords = {"SELECT", "INSERT", "UPDATE", "DELETE", "DROP", "UNION", "ALTER",
                "--", ";", "/*", "*/", "@@", "@", "CHAR(", "EXEC(", "EXECUTE(", "DECLARE"};

        for (String keyword : sqlKeywords) {
            if (input.toUpperCase().contains(keyword)) {
                return true;
            }
        }

        return input.contains("'") || input.contains("\"") || input.contains("\\");
    }

    @FXML
    private void handleRefreshButtonAction() {
        searchField.clear();
        filterCategoryCombo.setValue("Toutes");
        filterPriceCombo.setValue("Tous les prix");
        loadApprovedProducts();
    }

    private void loadApprovedProducts() {
        productCardsPane.getChildren().clear();
        productsList.clear();

        // Reset counter
        totalProducts = 0;

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = Connections.getInstance().getConnection();

            // Build query based on filters - Only approved products
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT * FROM produit WHERE approved = 1");

            // Add search filter if present
            if (searchField.getText() != null && !searchField.getText().trim().isEmpty()) {
                queryBuilder.append(" AND nom LIKE ?");
            }

            // Add category filter if selected
            if (filterCategoryCombo.getValue() != null && !filterCategoryCombo.getValue().equals("Toutes")) {
                queryBuilder.append(" AND categories = ?");
            }

            // Add price filter if selected
            if (filterPriceCombo.getValue() != null && !filterPriceCombo.getValue().equals("Tous les prix")) {
                switch (filterPriceCombo.getValue()) {
                    case "Moins de 10 DT":
                        queryBuilder.append(" AND prix < 10");
                        break;
                    case "10 DT - 50 DT":
                        queryBuilder.append(" AND prix >= 10 AND prix <= 50");
                        break;
                    case "50 DT - 100 DT":
                        queryBuilder.append(" AND prix > 50 AND prix <= 100");
                        break;
                    case "Plus de 100 DT":
                        queryBuilder.append(" AND prix > 100");
                        break;
                }
            }

            // Order by ID for consistency
            queryBuilder.append(" ORDER BY id");

            ps = connection.prepareStatement(queryBuilder.toString());

            // Set parameters
            int paramIndex = 1;

            if (searchField.getText() != null && !searchField.getText().trim().isEmpty()) {
                ps.setString(paramIndex++, "%" + searchField.getText().trim() + "%");
            }

            if (filterCategoryCombo.getValue() != null && !filterCategoryCombo.getValue().equals("Toutes")) {
                ps.setString(paramIndex, filterCategoryCombo.getValue());
            }

            rs = ps.executeQuery();

            while (rs.next()) {
                Produit product = new Produit();
                product.setId(rs.getInt("id"));
                product.setNom(rs.getString("nom"));
                product.setPrix(rs.getDouble("prix"));
                product.setQuantite(rs.getInt("quantite"));
                product.setCategories(rs.getString("categories"));
                product.setImage_file_name(rs.getString("image_file_name"));
                product.setDescription(rs.getString("description"));
                product.setApproved(true); // We know it's approved based on the query

                // Update counter
                totalProducts++;

                productsList.add(product);
                createProductCard(product);
            }

            // Update statistics label
            updateStatistics();

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error loading products for marketplace", ex);
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load products",
                    "An error occurred while loading products: " + ex.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (connection != null) connection.close();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error closing database resources", ex);
            }
        }
    }

    private void createProductCard(Produit product) {
        VBox card = new VBox();
        card.getStyleClass().add("marketplace-card");
        card.setPrefWidth(220);
        card.setPrefHeight(350);
        card.setPadding(new Insets(10));
        card.setSpacing(8);
        card.setAlignment(Pos.TOP_CENTER);

        // Product image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(150);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);

        // Load image asynchronously
        Platform.runLater(() -> {
            try {
                String imagePath = product.getImage_file_name();
                if (imagePath != null && !imagePath.isEmpty()) {
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString(), 150, 100, true, true);
                        imageView.setImage(image);
                    } else {
                        // Try as a relative path from resources
                        String relativePath = IMAGE_DIRECTORY + new File(imagePath).getName();
                        File relativeFile = new File(relativePath);

                        if (relativeFile.exists()) {
                            Image image = new Image(relativeFile.toURI().toString(), 150, 100, true, true);
                            imageView.setImage(image);
                        } else {
                            // Load default image
                            File defaultImage = new File(IMAGE_DIRECTORY + "default_product.png");
                            if (defaultImage.exists()) {
                                Image image = new Image(defaultImage.toURI().toString(), 150, 100, true, true);
                                imageView.setImage(image);
                            }
                        }
                    }
                } else {
                    // Load default image
                    File defaultImage = new File(IMAGE_DIRECTORY + "default_product.png");
                    if (defaultImage.exists()) {
                        Image image = new Image(defaultImage.toURI().toString(), 150, 100, true, true);
                        imageView.setImage(image);
                    }
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error loading image for product: " + product.getId(), ex);
                imageView.setImage(null);
            }
        });

        // Center the imageView
        StackPane imageContainer = new StackPane();
        imageContainer.getChildren().add(imageView);
        imageContainer.setPrefHeight(100);
        imageContainer.getStyleClass().add("image-container");

        // Product details
        Label nameLabel = new Label(product.getNom());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setMaxWidth(200);

        Label priceLabel = new Label(currencyFormat.format(product.getPrix()));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        priceLabel.setTextFill(Color.GREEN);

        Label categoryLabel = new Label("Catégorie: " + product.getCategories());
        categoryLabel.setFont(Font.font("System", 12));

        Label quantityLabel = new Label("Disponible: " + product.getQuantite() + " unités");
        quantityLabel.setFont(Font.font("System", 12));

        // Description with scroll capability if long
        String description = product.getDescription();
        if (description == null || description.isEmpty()) {
            description = "Aucune description disponible.";
        }

        // Truncate description if too long
        if (description.length() > 100) {
            description = description.substring(0, 97) + "...";
        }

        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(200);
        descriptionLabel.setMaxHeight(60);

        // Action button
        Button buyButton = new Button("Voir détails");
        buyButton.getStyleClass().add("buy-button");
        buyButton.setOnAction(e -> showProductDetails(product));

        // Add all components to the card
        card.getChildren().addAll(imageContainer, nameLabel, priceLabel, categoryLabel, quantityLabel, descriptionLabel, buyButton);

        // Add card to the pane
        productCardsPane.getChildren().add(card);
    }

    private void showProductDetails(Produit product) {
        // Create a dialog to show full product details
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Détails du Produit");
        dialog.setHeaderText(product.getNom());

        // Set the button types
        ButtonType buyButtonType = new ButtonType("Ajouter au panier", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buyButtonType, ButtonType.CLOSE);

        // Create the detail layout
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        // Product image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(300);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        try {
            String imagePath = product.getImage_file_name();
            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    imageView.setImage(image);
                } else {
                    // Try as a relative path from resources
                    String relativePath = IMAGE_DIRECTORY + new File(imagePath).getName();
                    File relativeFile = new File(relativePath);

                    if (relativeFile.exists()) {
                        Image image = new Image(relativeFile.toURI().toString());
                        imageView.setImage(image);
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error loading image for product detail: " + product.getId(), ex);
        }

        // Center the image
        StackPane imageContainer = new StackPane();
        imageContainer.getChildren().add(imageView);

        // Product details
        Label nameLabel = new Label(product.getNom());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 22));

        Label priceLabel = new Label(currencyFormat.format(product.getPrix()));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        priceLabel.setTextFill(Color.GREEN);

        Label categoryLabel = new Label("Catégorie: " + product.getCategories());
        categoryLabel.setFont(Font.font("System", 14));

        Label quantityLabel = new Label("Quantité disponible: " + product.getQuantite() + " unités");
        quantityLabel.setFont(Font.font("System", 14));

        Label descriptionTitle = new Label("Description:");
        descriptionTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label descriptionLabel = new Label(product.getDescription() != null && !product.getDescription().isEmpty()
                ? product.getDescription() : "Aucune description disponible.");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(500);

        // Add all components to the layout
        content.getChildren().addAll(
                imageContainer, nameLabel, priceLabel, categoryLabel, quantityLabel,
                new Separator(), descriptionTitle, descriptionLabel
        );

        // Set the content
        dialog.getDialogPane().setContent(content);

        // Show the dialog and handle the response
        dialog.showAndWait().ifPresent(response -> {
            if (response == buyButtonType) {
                addToCart(product);
            }
        });
    }

    private void addToCart(Produit product) {
        try {
            // Ajouter le produit au panier
            Panier panier = Panier.getInstance();

            // Demander la quantité
            TextInputDialog quantiteDialog = new TextInputDialog("1");
            quantiteDialog.setTitle("Quantité");
            quantiteDialog.setHeaderText("Entrez la quantité souhaitée");
            quantiteDialog.setContentText("Quantité:");

            // Vérifier que la quantité est un nombre valide et qu'elle ne dépasse pas la quantité disponible
            quantiteDialog.showAndWait().ifPresent(quantiteStr -> {
                try {
                    int quantite = Integer.parseInt(quantiteStr);
                    if (quantite <= 0) {
                        showAlert(Alert.AlertType.WARNING, "Quantité invalide",
                                "La quantité doit être supérieure à 0",
                                "Veuillez entrer une quantité positive.");
                        return;
                    }

                    if (quantite > product.getQuantite()) {
                        showAlert(Alert.AlertType.WARNING, "Quantité insuffisante",
                                "Il n'y a que " + product.getQuantite() + " unités disponibles",
                                "Veuillez réduire la quantité demandée.");
                        return;
                    }

                    // Ajouter au panier
                    panier.ajouterProduit(product, quantite);

                    // Mettre à jour le compteur du panier
                    updatePanierCount();

                    // Demander si l'utilisateur veut voir le panier ou continuer ses achats
                    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmation.setTitle("Produit ajouté au panier");
                    confirmation.setHeaderText("Le produit a été ajouté à votre panier");
                    confirmation.setContentText("Que voulez-vous faire maintenant ?");

                    ButtonType voirPanierButton = new ButtonType("Voir le panier");
                    ButtonType continuerButton = new ButtonType("Continuer mes achats");

                    confirmation.getButtonTypes().setAll(voirPanierButton, continuerButton);

                    confirmation.showAndWait().ifPresent(buttonType -> {
                        if (buttonType == voirPanierButton) {
                            handlePanierButtonAction(null);
                        }
                    });

                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Veuillez entrer un nombre valide",
                            "La quantité doit être un nombre entier.");
                }
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding product to cart", e);
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ajouter le produit au panier",
                    "Une erreur s'est produite: " + e.getMessage());
        }
    }

    /**
     * Ouvre la vue du panier
     */
    @FXML
    private void handlePanierButtonAction(ActionEvent event) {
        try {
            // Load the Panier view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/panier.fxml"));
            Parent panierView = loader.load();

            // Get current stage
            Stage stage = (Stage) panierButton.getScene().getWindow();

            // Create new scene with Panier view
            Scene scene = new Scene(panierView);

            // Set the scene to the stage
            stage.setScene(scene);
            stage.setTitle("AgriFarm - Panier");
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading Panier view", e);
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation",
                    "Impossible de charger la vue Panier",
                    "Une erreur s'est produite: " + e.getMessage());
        }
    }

    /**
     * Met à jour le compteur d'articles dans le panier
     */
    private void updatePanierCount() {
        Panier panier = Panier.getInstance();
        int count = panier.getNombreTotalArticles();

        if (count > 0) {
            panierCountLabel.setText(String.valueOf(count));
            panierCountLabel.setVisible(true);
        } else {
            panierCountLabel.setVisible(false);
        }
    }

    private void updateStatistics() {
        statisticsLabel.setText(String.format("Total: %d produits disponibles", totalProducts));
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void navigateToHome(ActionEvent event) {
        try {
            // Load the Home/Produit view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/produit.fxml"));
            Parent homeView = loader.load();

            // Get current stage
            Stage stage = (Stage) homeButton.getScene().getWindow();

            // Create new scene with Home view
            Scene scene = new Scene(homeView);

            // Set the scene to the stage
            stage.setScene(scene);
            stage.setTitle("AgriFarm - Accueil");
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading Home view", e);
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation",
                    "Impossible de charger la vue d'accueil",
                    "Une erreur s'est produite: " + e.getMessage());
        }
    }

    @FXML
    private void handleOrdersButtonAction(ActionEvent event) {
        try {
            // Check if user is logged in
            if (currentUser == null) {
                showAlert(Alert.AlertType.WARNING, "Non connecté",
                        "Vous devez être connecté pour voir vos commandes",
                        "Veuillez vous connecter pour accéder à vos commandes.");
                return;
            }

            // Create a new dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Mes Commandes");
            dialog.setHeaderText("Liste de vos commandes");

            // Create the content
            VBox content = new VBox(10);
            content.setPadding(new Insets(20));

            // Get user's orders
            List<Commande> commandes = commandeService.getCommandesByUser(currentUser.getId());

            if (commandes.isEmpty()) {
                content.getChildren().add(new Label("Vous n'avez pas encore de commandes."));
            } else {
                for (Commande commande : commandes) {
                    // Create a card for each order
                    VBox orderCard = createOrderCard(commande);
                    content.getChildren().add(orderCard);
                }
            }

            // Add scroll pane for better viewing
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(400);

            // Set the content
            dialog.getDialogPane().setContent(scrollPane);

            // Add close button
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            // Show the dialog
            dialog.showAndWait();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error displaying orders", e);
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'afficher les commandes",
                    "Une erreur s'est produite: " + e.getMessage());
        }
    }

    private VBox createOrderCard(Commande commande) {
        VBox card = new VBox(5);
        card.getStyleClass().add("order-card");
        card.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: #ddd; " +
                     "-fx-border-radius: 5; -fx-background-radius: 5;");

        // Order header
        Label dateLabel = new Label("Date: " + commande.getDate_creation_commande());
        dateLabel.setStyle("-fx-font-weight: bold;");

        Label statusLabel = new Label("Status: " + commande.getStatus());
        statusLabel.setStyle("-fx-text-fill: " + getStatusColor(commande.getStatus()));

        Label totalLabel = new Label(String.format("Total: %.2f DT", commande.getPrix()));
        totalLabel.setStyle("-fx-font-weight: bold;");

        // Products list
        VBox productsBox = new VBox(5);
        for (Produit produit : commande.getProduits()) {
            int quantite = commande.getQuantitesParProduit().get(produit.getId());
            Label produitLabel = new Label(String.format("%s (x%d) - %.2f DT/unité",
                    produit.getNom(), quantite, produit.getPrix()));
            productsBox.getChildren().add(produitLabel);
        }

        // Download PDF button
        Button downloadButton = new Button("Télécharger PDF");
        downloadButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        downloadButton.setOnAction(e -> generatePDF(commande));

        card.getChildren().addAll(dateLabel, statusLabel, totalLabel,
                new Separator(), productsBox, downloadButton);

        return card;
    }

    private String getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "en attente":
                return "#f39c12";
            case "confirmée":
                return "#27ae60";
            case "livrée":
                return "#2ecc71";
            case "annulée":
                return "#e74c3c";
            default:
                return "#000000";
        }
    }

    private void generatePDF(Commande commande) {
        try {
            commandeService.generatePDF(commande, (Stage) ordersButton.getScene().getWindow());
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "PDF généré avec succès",
                    "Le PDF de votre commande a été généré et enregistré.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating PDF", e);
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de générer le PDF",
                    "Une erreur s'est produite: " + e.getMessage());
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleStatsButtonAction(ActionEvent event) {
        try {
            // Vérifier si l'utilisateur est connecté
            if (currentUser == null) {
                showAlert(Alert.AlertType.WARNING, "Non connecté",
                        "Vous devez être connecté pour voir vos statistiques",
                        "Veuillez vous connecter pour accéder à vos statistiques d'achat.");
                return;
            }

            // Créer et afficher la vue des statistiques
            StatistiquesView statsView = new StatistiquesView(currentUser.getId());
            statsView.show();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'affichage des statistiques", e);
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'afficher les statistiques",
                    "Une erreur s'est produite: " + e.getMessage());
        }
    }
}