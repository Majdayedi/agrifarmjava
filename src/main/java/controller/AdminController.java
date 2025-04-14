package controller;

import entite.Produit;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import utils.Connections;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(AdminController.class.getName());

    @FXML
    private FlowPane productCardsPane;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private ComboBox<String> filterCategoryCombo;
    
    @FXML
    private ComboBox<String> filterStatusCombo;
    
    @FXML
    private Label statisticsLabel;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button agricoleButton;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private Button marketplaceButton;
    
    private ProduitController produitController;
    private ObservableList<Produit> productsList = FXCollections.observableArrayList();
    private int totalProducts = 0;
    private int approvedProducts = 0;
    private int pendingProducts = 0;
    private final String IMAGE_DIRECTORY = "src/main/resources/images/";
    
    public AdminController() {
        produitController = new ProduitController();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize filter combos
        filterCategoryCombo.getItems().addAll("Toutes", "Légumes", "Fruits", "Céréales", "Autres");
        filterCategoryCombo.setValue("Toutes");
        
        filterStatusCombo.getItems().addAll("Tous", "Approuvés", "En attente");
        filterStatusCombo.setValue("Tous");
        
        // Add listeners for filters
        filterCategoryCombo.setOnAction(e -> loadProducts());
        filterStatusCombo.setOnAction(e -> loadProducts());
        
        // Load products initially
        loadProducts();
    }
    
    @FXML
    private void handleAgricoleButtonAction(ActionEvent event) {
        try {
            // Load the Agricole CRUD view
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
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation", 
                     "Impossible de charger la vue Agricole", 
                     "Une erreur s'est produite: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogoutButtonAction(ActionEvent event) {
        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de déconnexion");
        confirmDialog.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");
        confirmDialog.setContentText("Toutes les modifications non enregistrées seront perdues.");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Close the application for now
                    Stage stage = (Stage) logoutButton.getScene().getWindow();
                    stage.close();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error during logout", e);
                }
            }
        });
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
        loadProducts();
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
        filterStatusCombo.setValue("Tous");
        loadProducts();
    }
    
    @FXML
    private void handleMarketplaceButtonAction(ActionEvent event) {
        try {
            // Load the Marketplace view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/marketplace.fxml"));
            Parent marketplaceView = loader.load();
            
            // Get current stage
            Stage stage = (Stage) marketplaceButton.getScene().getWindow();
            
            // Create new scene with Marketplace view
            Scene scene = new Scene(marketplaceView);
            
            // Set the scene to the stage
            stage.setScene(scene);
            stage.setTitle("AgriFarm - Marketplace");
            stage.show();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading Marketplace view", e);
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation", 
                     "Impossible de charger la vue Marketplace", 
                     "Une erreur s'est produite: " + e.getMessage());
        }
    }
    
    private void loadProducts() {
        productCardsPane.getChildren().clear();
        productsList.clear();
        
        // Reset counters
        totalProducts = 0;
        approvedProducts = 0;
        pendingProducts = 0;
        
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            connection = Connections.getInstance().getConnection();
            
            // Build query based on filters
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT * FROM produit WHERE 1=1");
            
            // Add search filter if present
            if (searchField.getText() != null && !searchField.getText().trim().isEmpty()) {
                queryBuilder.append(" AND nom LIKE ?");
            }
            
            // Add category filter if selected
            if (filterCategoryCombo.getValue() != null && !filterCategoryCombo.getValue().equals("Toutes")) {
                queryBuilder.append(" AND categories = ?");
            }
            
            // Add status filter if selected
            if (filterStatusCombo.getValue() != null) {
                if (filterStatusCombo.getValue().equals("Approuvés")) {
                    queryBuilder.append(" AND approved = 1");
                } else if (filterStatusCombo.getValue().equals("En attente")) {
                    queryBuilder.append(" AND approved = 0");
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
                product.setApproved(rs.getBoolean("approved"));
                
                // Update counters
                totalProducts++;
                if (product.isApproved()) {
                    approvedProducts++;
                } else {
                    pendingProducts++;
                }
                
                productsList.add(product);
                createProductCard(product);
            }
            
            // Update statistics label
            updateStatistics();
            
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error loading products for admin view", ex);
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
        card.getStyleClass().add("product-card");
        card.setPrefWidth(220);
        card.setPrefHeight(320);
        card.setPadding(new Insets(10));
        card.setSpacing(8);
        
        // Status indicator
        HBox statusBox = new HBox();
        statusBox.setAlignment(Pos.CENTER_RIGHT);
        Label statusLabel = new Label(product.isApproved() ? "Approuvé" : "En attente");
        statusLabel.getStyleClass().add(product.isApproved() ? "status-approved" : "status-pending");
        statusLabel.setPadding(new Insets(3, 8, 3, 8));
        statusBox.getChildren().add(statusLabel);
        
        // Product image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(150);
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
                    } else {
                        // Load default image
                        File defaultImage = new File(IMAGE_DIRECTORY + "default_product.png");
                        if (defaultImage.exists()) {
                            Image image = new Image(defaultImage.toURI().toString());
                            imageView.setImage(image);
                        }
                    }
                }
            } else {
                // Load default image
                File defaultImage = new File(IMAGE_DIRECTORY + "default_product.png");
                if (defaultImage.exists()) {
                    Image image = new Image(defaultImage.toURI().toString());
                    imageView.setImage(image);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error loading image for product: " + product.getId(), ex);
            // If all else fails, add a placeholder text instead of image
            imageView.setImage(null);
        }
        
        // Center the imageView
        StackPane imageContainer = new StackPane();
        imageContainer.getChildren().add(imageView);
        imageContainer.setPrefHeight(150);
        
        // Product details
        Label nameLabel = new Label(product.getNom());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        nameLabel.setWrapText(true);
        
        Label priceLabel = new Label(String.format("%.2f DT", product.getPrix()));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        priceLabel.setTextFill(Color.GREEN);
        
        Label categoryLabel = new Label("Catégorie: " + product.getCategories());
        categoryLabel.setFont(Font.font("System", 12));
        
        Label quantityLabel = new Label("Quantité: " + product.getQuantite());
        quantityLabel.setFont(Font.font("System", 12));
        
        // Action buttons
        HBox actionsBox = new HBox();
        actionsBox.setSpacing(10);
        actionsBox.setAlignment(Pos.CENTER);
        
        Button approveButton = new Button("Approuver");
        approveButton.setDisable(product.isApproved());
        approveButton.getStyleClass().add("approve-button");
        approveButton.setOnAction(e -> approveProduct(product));
        
        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(e -> deleteProduct(product));
        
        actionsBox.getChildren().addAll(approveButton, deleteButton);
        
        // Add all components to the card
        card.getChildren().addAll(statusBox, imageContainer, nameLabel, categoryLabel, priceLabel, quantityLabel, actionsBox);
        
        // Add card to the pane
        productCardsPane.getChildren().add(card);
    }
    
    private void approveProduct(Produit product) {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            System.out.println("Début de l'approbation du produit ID: " + product.getId());
            
            // Obtenir une connexion fraîche directement
            connection = Connections.getInstance().getConnection();
            if (connection == null) {
                System.err.println("Connexion null, impossible d'approuver le produit");
                showAlert(Alert.AlertType.ERROR, "Erreur d'Approbation", 
                         "Échec de l'approbation du produit", 
                         "Impossible de se connecter à la base de données");
                return;
            }
            
            // Mise à jour directe dans la base de données
            String sql = "UPDATE produit SET approved = 1 WHERE id = ?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, product.getId());
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                product.setApproved(true);  // Update the local object
                // Reload products to reflect the change
                loadProducts();
                showAlert(Alert.AlertType.INFORMATION, "Produit Approuvé", 
                         "Produit approuvé avec succès", 
                         "Le produit a été approuvé et est maintenant visible pour les utilisateurs.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur d'Approbation", 
                         "Échec de l'approbation du produit", 
                         "Aucune ligne n'a été mise à jour dans la base de données.");
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQL Error approving product: " + ex.getMessage(), ex);
            showAlert(Alert.AlertType.ERROR, "Erreur d'Approbation", 
                     "Échec de l'approbation du produit", 
                     "Erreur SQL: " + ex.getMessage());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error approving product: " + ex.getMessage(), ex);
            showAlert(Alert.AlertType.ERROR, "Erreur d'Approbation", 
                     "Échec de l'approbation du produit", 
                     "Une erreur s'est produite: " + ex.getMessage());
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing resources: " + e.getMessage(), e);
            }
        }
    }
    
    private void deleteProduct(Produit product) {
        // Ask for confirmation before deleting
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de Suppression");
        confirmation.setHeaderText("Supprimer le produit");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ce produit ? Cette action est irréversible.");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Connection connection = null;
                PreparedStatement statement = null;
                
                try {
                    System.out.println("Début de la suppression du produit ID: " + product.getId());
                    
                    // Obtenir une connexion fraîche directement
                    connection = Connections.getInstance().getConnection();
                    if (connection == null) {
                        System.err.println("Connexion null, impossible de supprimer le produit");
                        showAlert(Alert.AlertType.ERROR, "Erreur de Suppression", 
                                 "Échec de la suppression du produit", 
                                 "Impossible de se connecter à la base de données");
                        return;
                    }
                    
                    // Suppression directe dans la base de données
                    String sql = "DELETE FROM produit WHERE id = ?";
                    statement = connection.prepareStatement(sql);
                    statement.setInt(1, product.getId());
                    
                    int rowsAffected = statement.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        // Reload products to reflect the change
                        loadProducts();
                        showAlert(Alert.AlertType.INFORMATION, "Produit Supprimé", 
                                 "Produit supprimé avec succès", 
                                 "Le produit a été supprimé de la base de données.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur de Suppression", 
                                 "Échec de la suppression du produit", 
                                 "Aucune ligne n'a été supprimée dans la base de données.");
                    }
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "SQL Error deleting product: " + ex.getMessage(), ex);
                    showAlert(Alert.AlertType.ERROR, "Erreur de Suppression", 
                             "Échec de la suppression du produit", 
                             "Erreur SQL: " + ex.getMessage());
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error deleting product: " + ex.getMessage(), ex);
                    showAlert(Alert.AlertType.ERROR, "Erreur de Suppression", 
                             "Échec de la suppression du produit", 
                             "Une erreur s'est produite: " + ex.getMessage());
                } finally {
                    try {
                        if (statement != null) statement.close();
                        if (connection != null) connection.close();
                    } catch (SQLException e) {
                        LOGGER.log(Level.SEVERE, "Error closing resources: " + e.getMessage(), e);
                    }
                }
            }
        });
    }
    
    private void updateStatistics() {
        statisticsLabel.setText(String.format("Total: %d produits | Approuvés: %d | En attente: %d", 
                               totalProducts, approvedProducts, pendingProducts));
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 