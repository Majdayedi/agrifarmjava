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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.ProduitService;
import utils.Connections;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProduitFXMLController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ProduitFXMLController.class.getName());

    @FXML private TextField nomField, quantiteField, prixField, imageFileNameField, searchField;
    @FXML private ComboBox<String> categoriesCombo, filterCategoryCombo;
    @FXML private TextArea descriptionArea;
    @FXML private CheckBox approvedCheck;
    @FXML private Button browseButton, addButton, updateButton, deleteButton, clearButton, 
                        searchButton, adminButton, logoutButton, marketplaceButton, homeButton;
    @FXML private FlowPane productCardsPane;
    
    private final ProduitService produitService = new ProduitService();
    private Produit selectedProduit;
    private final ObservableList<Produit> produitData = FXCollections.observableArrayList();
    private final String IMAGE_DIRECTORY = "src/main/resources/images/";
    private final String DEFAULT_IMAGE = "default_product.png";
    
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
    
    private final List<String> CATEGORIES = Arrays.asList(
        "Fruits", "Légumes", "Céréales", "Produits laitiers", "Viandes", "Outils agricoles", 
        "Semences", "Engrais", "Autre"
    );
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Create images directory if needed
        new File(IMAGE_DIRECTORY).mkdirs();
        
        // Initialize UI components
        categoriesCombo.getItems().addAll(CATEGORIES);
        filterCategoryCombo.getItems().addAll("Toutes");
        filterCategoryCombo.getItems().addAll(CATEGORIES);
        filterCategoryCombo.setValue("Toutes");
        filterCategoryCombo.setOnAction(e -> filterByCategory());
        
        // Setup validation
        setupValidation();
        
        // Initial state
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        
        // Load data if connection available
        Connection conn = Connections.getInstance().getConnection();
        if (conn != null) {
            loadAllProducts();
        } else {
            showAlert(Alert.AlertType.WARNING, "Database Error", 
                    "Could not connect to the database. Please check your connection settings.");
        }
    }
    
    @FXML private void handleAddButtonAction(ActionEvent event) {
        if (!validateInputs()) return;
                
        try {
                Produit produit = new Produit(
                        nomField.getText(),
                        Integer.parseInt(quantiteField.getText()),
                        Double.parseDouble(prixField.getText()),
                        categoriesCombo.getValue(),
                        descriptionArea.getText(),
                        imageFileNameField.getText()
                );
                produit.setApproved(approvedCheck.isSelected());
                
                copyImageIfNeeded(imageFileNameField.getText());
                
            if (produitService.create(produit)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully");
                    clearFields();
                    loadAllProducts();
                } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add product");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }
    
    @FXML private void handleUpdateButtonAction(ActionEvent event) {
        if (selectedProduit == null || !validateInputs()) return;
        
        try {
            // Update product fields
                selectedProduit.setNom(nomField.getText());
                selectedProduit.setQuantite(Integer.parseInt(quantiteField.getText()));
                selectedProduit.setPrix(Double.parseDouble(prixField.getText()));
                selectedProduit.setCategories(categoriesCombo.getValue());
                selectedProduit.setDescription(descriptionArea.getText());
            selectedProduit.setApproved(approvedCheck.isSelected());
            
            // Handle image if changed
            String newImage = imageFileNameField.getText();
            if (!newImage.equals(selectedProduit.getImage_file_name())) {
                copyImageIfNeeded(newImage);
                selectedProduit.setImage_file_name(newImage);
                }
                
            if (produitService.update(selectedProduit)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully");
                    clearFields();
                    loadAllProducts();
                    selectedProduit = null;
                    updateButton.setDisable(true);
                    deleteButton.setDisable(true);
                } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update product");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }
    
    @FXML private void handleDeleteButtonAction(ActionEvent event) {
        if (selectedProduit == null) return;
        
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Confirmation");
        confirmAlert.setHeaderText("Delete Product?");
        confirmAlert.setContentText("Are you sure you want to delete this product?");
            
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                if (produitService.delete(selectedProduit.getId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully");
                        clearFields();
                        loadAllProducts();
                        selectedProduit = null;
                        updateButton.setDisable(true);
                        deleteButton.setDisable(true);
                    } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete product");
                    }
                }
            });
    }
    
    @FXML private void handleClearButtonAction(ActionEvent event) {
        clearFields();
        selectedProduit = null;
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }
    
    @FXML private void handleBrowseButtonAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File selectedFile = fileChooser.showOpenDialog(browseButton.getScene().getWindow());
        if (selectedFile != null) {
            imageFileNameField.setText(selectedFile.getAbsolutePath());
        }
    }
    
    @FXML private void handleSearchButtonAction(ActionEvent event) {
        String keyword = searchField.getText().trim();
        
        if (keyword.isEmpty()) {
            loadAllProducts();
                return;
            }
            
        if (containsSQLInjection(keyword)) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Search contains invalid characters");
                return;
            }
        
        produitData.clear();
        produitData.addAll(produitService.searchByName(keyword));
        displayProductCards();
    }
    
    @FXML private void handleAdminButtonAction(ActionEvent event) { navigateTo("/controller/admin.fxml", "Admin"); }
    @FXML private void handleMarketplaceButtonAction(ActionEvent event) { navigateTo("/controller/marketplace.fxml", "Marketplace"); }
    @FXML private void navigateToHome() { navigateTo("/home.fxml", "Home"); }
    
    @FXML private void handleLogoutButtonAction(ActionEvent event) {
        new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to log out?")
            .showAndWait()
            .filter(r -> r == ButtonType.OK)
            .ifPresent(r -> ((Stage) logoutButton.getScene().getWindow()).close());
    }
    
    private void navigateTo(String fxmlPath, String title) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) homeButton.getScene().getWindow();
            stage.setScene(new Scene(view));
            stage.setTitle("AgriFarm - " + title);
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Navigation error", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load the requested view");
        }
    }
    
    private void loadAllProducts() {
            produitData.clear();
        produitData.addAll(produitService.readAll());
            displayProductCards();
        }
    
    private void displayProductCards() {
        productCardsPane.getChildren().clear();
        for (Produit produit : produitData) {
            productCardsPane.getChildren().add(createProductCard(produit));
        }
    }
    
    private VBox createProductCard(Produit produit) {
        // Create card container
        VBox card = new VBox(8);
        card.setPrefSize(180, 320);
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().add("product-card");
        
        // Load image or create placeholder
        ImageView imageView = new ImageView();
        imageView.setFitWidth(140);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);
        
        try {
            String imagePath = produit.getImage_file_name();
            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (!imageFile.exists()) imageFile = new File(IMAGE_DIRECTORY + imagePath);
                
                if (imageFile.exists()) {
                    imageView.setImage(new Image(new FileInputStream(imageFile)));
            }
            }
        } catch (Exception e) {
            // Image loading failed, continue with no image
        }
        
        // Create card labels
        Label nameLabel = new Label(produit.getNom());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        nameLabel.setWrapText(true);
        
        Label priceLabel = new Label(String.format("%.2f DT", produit.getPrix()));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        priceLabel.setTextFill(Color.GREEN);
        
        Label categoryLabel = new Label("Category: " + produit.getCategories());
        Label quantityLabel = new Label("Quantity: " + produit.getQuantite());
        
        // Status indicator
        Label statusLabel = new Label(produit.isApproved() ? "✓ Approved" : "⨯ Pending");
        statusLabel.setTextFill(produit.isApproved() ? Color.GREEN : Color.RED);
        
        // Add components to card
        StackPane imageContainer = new StackPane(imageView);
        imageContainer.setMinHeight(150);
        card.getChildren().addAll(
            imageContainer, nameLabel, priceLabel, categoryLabel, quantityLabel, statusLabel
        );
        
        // Set card click handler
        card.setOnMouseClicked(e -> {
            selectedProduit = produit;
            populateFields(produit);
            updateButton.setDisable(false);
            deleteButton.setDisable(false);
            
            // Update styles
            productCardsPane.getChildren().forEach(node -> {
                if (node instanceof VBox) node.getStyleClass().setAll("product-card");
            });
            card.getStyleClass().setAll("product-card-selected");
        });
        
        return card;
    }
    
    private void populateFields(Produit produit) {
        nomField.setText(produit.getNom());
        quantiteField.setText(String.valueOf(produit.getQuantite()));
        prixField.setText(String.valueOf(produit.getPrix()));
        categoriesCombo.setValue(produit.getCategories());
        descriptionArea.setText(produit.getDescription());
        imageFileNameField.setText(produit.getImage_file_name());
        approvedCheck.setSelected(produit.isApproved());
    }
    
    private void clearFields() {
        nomField.clear();
        quantiteField.clear();
        prixField.clear();
        categoriesCombo.getSelectionModel().clearSelection();
        descriptionArea.clear();
        imageFileNameField.clear();
        approvedCheck.setSelected(false);
    }
    
    private void filterByCategory() {
        String category = filterCategoryCombo.getValue();
        if (category == null || category.equals("Toutes")) {
            loadAllProducts();
        } else {
            produitData.clear();
            produitData.addAll(produitService.filterByCategory(category));
            displayProductCards();
        }
    }
    
    private void copyImageIfNeeded(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return;
        
        File sourceFile = new File(imagePath);
        if (!sourceFile.exists() || !sourceFile.isFile()) return;
        
        try {
            new File(IMAGE_DIRECTORY).mkdirs();
            String fileName = sourceFile.getName();
            Files.copy(sourceFile.toPath(), 
                      Paths.get(IMAGE_DIRECTORY + fileName), 
                      StandardCopyOption.REPLACE_EXISTING);
            imageFileNameField.setText(fileName);
        } catch (IOException e) {
            // If copy fails, use absolute path
            imageFileNameField.setText(sourceFile.getAbsolutePath());
        }
    }
    
    private void setupValidation() {
        // Limit input lengths
        nomField.textProperty().addListener((o, old, val) -> {
            if (val.length() > 100) nomField.setText(old);
        });
        
        // Only allow digits in quantity field
        quantiteField.textProperty().addListener((o, old, val) -> {
            if (!val.matches("\\d*")) quantiteField.setText(val.replaceAll("[^\\d]", ""));
        });
        
        // Only allow digits and decimal points in price field
        prixField.textProperty().addListener((o, old, val) -> {
            if (!val.matches("\\d*(\\.\\d*)?")) prixField.setText(old);
        });
        
        // Limit description length
        descriptionArea.textProperty().addListener((o, old, val) -> {
            if (val.length() > 1000) descriptionArea.setText(old);
        });
    }
    
    private boolean validateInputs() {
        boolean valid = true;
        StringBuilder errors = new StringBuilder();
        
        // Name validation
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            errors.append("- Name cannot be empty\n");
            nomField.setStyle("-fx-border-color: red;");
            valid = false;
        } else {
            nomField.setStyle("");
        }
        
        // Quantity validation
        try {
            int qty = Integer.parseInt(quantiteField.getText());
            if (qty <= 0) {
                errors.append("- Quantity must be greater than zero\n");
                quantiteField.setStyle("-fx-border-color: red;");
                valid = false;
            } else {
                quantiteField.setStyle("");
            }
        } catch (NumberFormatException e) {
            errors.append("- Quantity must be a valid number\n");
            quantiteField.setStyle("-fx-border-color: red;");
            valid = false;
        }
        
        // Price validation
        try {
            double price = Double.parseDouble(prixField.getText());
            if (price <= 0) {
                errors.append("- Price must be greater than zero\n");
                prixField.setStyle("-fx-border-color: red;");
                valid = false;
            } else {
                prixField.setStyle("");
            }
        } catch (NumberFormatException e) {
            errors.append("- Price must be a valid number\n");
            prixField.setStyle("-fx-border-color: red;");
            valid = false;
        }
        
        // Category validation
        if (categoriesCombo.getValue() == null) {
            errors.append("- Please select a category\n");
            categoriesCombo.setStyle("-fx-border-color: red;");
            valid = false;
        } else {
            categoriesCombo.setStyle("");
        }
        
        if (!valid) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", errors.toString());
        }
        
        return valid;
    }
    
    private boolean containsSQLInjection(String input) {
        String[] keywords = {"SELECT", "INSERT", "UPDATE", "DELETE", "DROP", "--", ";"};
        return Arrays.stream(keywords).anyMatch(kw -> input.toUpperCase().contains(kw)) || 
               input.contains("'") || input.contains("\"");
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 