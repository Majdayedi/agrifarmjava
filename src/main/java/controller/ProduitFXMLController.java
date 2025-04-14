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
import utils.DataSource;

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

    @FXML private TextField nomField;
    @FXML private TextField quantiteField;
    @FXML private TextField prixField;
    @FXML private ComboBox<String> categoriesCombo;
    @FXML private TextArea descriptionArea;
    @FXML private TextField imageFileNameField;
    @FXML private CheckBox approvedCheck;
    
    @FXML private Button browseButton;
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;
    
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private ComboBox<String> filterCategoryCombo;
    
    @FXML private FlowPane productCardsPane;
    
    @FXML private Button adminButton;
    @FXML private Button logoutButton;
    @FXML private Button marketplaceButton;
    
    private final ProduitController produitController = new ProduitController();
    private Produit selectedProduit;
    private final ObservableList<Produit> produitData = FXCollections.observableArrayList();
    private final String IMAGE_DIRECTORY = "src/main/resources/images/";
    private final String DEFAULT_IMAGE = "default_product.png";
    
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
    
    private final List<String> CATEGORIES = Arrays.asList(
        "Fruits", "Légumes", "Céréales", "Produits laitiers", "Viandes", "Outils agricoles", "Semences", "Engrais", "Autre"
    );
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ProduitFXMLController initializing...");
        
        // Create images directory if it doesn't exist
        File imagesDir = new File(IMAGE_DIRECTORY);
        if (!imagesDir.exists()) {
            boolean created = imagesDir.mkdirs();
            System.out.println("Image directory created: " + created);
        }
        
        // Check database connection before loading products
        Connection conn = DataSource.getInstance().getConnection();
        if (conn != null) {
            loadAllProducts();
        } else {
            System.err.println("Warning: Database connection is null, products cannot be loaded");
            showAlert(Alert.AlertType.WARNING, "Connection Error", 
                     "Could not connect to the database. Please check your database connection settings.");
        }
        
        populateCategories();
        setupListeners();
        setupInputValidation();
        
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }
    
    private void populateCategories() {
        categoriesCombo.getItems().addAll(CATEGORIES);
        filterCategoryCombo.getItems().add("Toutes");
        filterCategoryCombo.getItems().addAll(CATEGORIES);
        filterCategoryCombo.getSelectionModel().selectFirst();
    }
    
    private void setupListeners() {
        filterCategoryCombo.setOnAction(event -> filterByCategory());
    }
    
    private void loadAllProducts() {
        try {
        produitData.clear();
        List<Produit> products = produitController.readAll();
            if (products != null) {
        produitData.addAll(products);
                displayProductCards();
            }
        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void displayProductCards() {
        productCardsPane.getChildren().clear();
        
        for (Produit produit : produitData) {
            VBox card = createProductCard(produit);
            productCardsPane.getChildren().add(card);
        }
    }
    
    private VBox createProductCard(Produit produit) {
        // Card container
        VBox card = new VBox();
        card.setPrefWidth(180);
        card.setPrefHeight(320);
        card.setSpacing(8);
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().add("product-card");
        
        // Create image placeholder with a solid background color
        StackPane imageContainer = new StackPane();
        imageContainer.setMinSize(150, 150);
        imageContainer.setMaxSize(150, 150);
        imageContainer.getStyleClass().add("image-container");
        
        // Try to load the product image
        boolean imageLoaded = false;
        
        if (produit.getImage_file_name() != null && !produit.getImage_file_name().isEmpty()) {
            try {
                // First try the absolute path
                File imageFile = new File(produit.getImage_file_name());
                
                // If not an absolute path, try the relative path in our image directory
                if (!imageFile.exists() || !imageFile.isAbsolute()) {
                    imageFile = new File(IMAGE_DIRECTORY + produit.getImage_file_name());
                }
                
                System.out.println("Trying to load image from: " + imageFile.getAbsolutePath() + " (exists: " + imageFile.exists() + ")");
                
                if (imageFile.exists() && imageFile.isFile() && imageFile.length() > 0) {
                    FileInputStream fis = new FileInputStream(imageFile);
                    Image image = new Image(fis);
                    
                    if (!image.isError()) {
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(140);
                        imageView.setFitHeight(140);
                        imageView.setPreserveRatio(true);
                        imageView.setSmooth(true);
                        imageView.setCache(true);
                        
                        imageContainer.getChildren().add(imageView);
                        imageLoaded = true;
                        
                        System.out.println("Image loaded successfully for: " + produit.getNom());
                    } else {
                        System.err.println("Image has errors: " + produit.getImage_file_name());
                    }
                    fis.close();
                } else {
                    System.err.println("Image file not valid: " + imageFile.getAbsolutePath());
                }
            } catch (Exception e) {
                System.err.println("Failed to load image: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // If no image was loaded, create a text-based placeholder
        if (!imageLoaded) {
            // Get the first 2 letters of the product name
            String initials = "";
            if (produit.getNom() != null && !produit.getNom().isEmpty()) {
                initials = produit.getNom().substring(0, Math.min(2, produit.getNom().length())).toUpperCase();
            }
            
            Label placeholderText = new Label(initials);
            placeholderText.setFont(Font.font("System", FontWeight.BOLD, 36));
            placeholderText.setTextFill(Color.web("#888888"));
            
            imageContainer.getChildren().add(placeholderText);
            System.out.println("Using placeholder for: " + produit.getNom());
        }
        
        card.getChildren().add(imageContainer);
        
        // Product name
        Label nameLabel = new Label(produit.getNom());
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setMaxWidth(160);
        
        // Price
        Label priceLabel = new Label(currencyFormat.format(produit.getPrix()));
        priceLabel.getStyleClass().add("product-price");
        
        // Category with icon
        HBox categoryBox = new HBox(5);
        categoryBox.setAlignment(Pos.CENTER);
        Label categoryLabel = new Label(produit.getCategories());
        categoryBox.getChildren().add(categoryLabel);
        categoryBox.getStyleClass().add("product-category");
        
        // Quantity
        Label quantityLabel = new Label("Stock: " + produit.getQuantite());
        
        // Status indicator (approved or not)
        HBox statusBox = new HBox();
        statusBox.setAlignment(Pos.CENTER);
        Circle statusCircle = new Circle(5);
        statusCircle.setFill(produit.isApproved() ? Color.GREEN : Color.RED);
        Label statusLabel = new Label(produit.isApproved() ? " Approuvé" : " En attente");
        statusLabel.getStyleClass().add(produit.isApproved() ? "approved-status" : "pending-status");
        statusBox.getChildren().addAll(statusCircle, statusLabel);
        
        // Add all elements to the card
        card.getChildren().addAll(nameLabel, priceLabel, categoryBox, quantityLabel, statusBox);
        
        // Set up click handler to select this product
        card.setOnMouseClicked(event -> {
            selectedProduit = produit;
            populateFields(selectedProduit);
            updateButton.setDisable(false);
            deleteButton.setDisable(false);
            
            // Highlight the selected card
            for (javafx.scene.Node node : productCardsPane.getChildren()) {
                if (node instanceof VBox) {
                    VBox cardNode = (VBox) node;
                    cardNode.getStyleClass().remove("product-card-selected");
                    cardNode.getStyleClass().add("product-card");
                }
            }
            card.getStyleClass().remove("product-card");
            card.getStyleClass().add("product-card-selected");
        });
        
        return card;
    }
    
    private static class Circle extends javafx.scene.shape.Circle {
        public Circle(double radius) {
            super(radius);
        }
    }
    
    private void populateFields(Produit produit) {
        nomField.setText(produit.getNom());
        quantiteField.setText(String.valueOf(produit.getQuantite()));
        prixField.setText(String.valueOf(produit.getPrix()));
        categoriesCombo.setValue(produit.getCategories());
        descriptionArea.setText(produit.getDescription());
        imageFileNameField.setText(produit.getImage_file_name());
        approvedCheck.setSelected(produit.isApproved());
        
        // Show preview of the existing image
        if (produit.getImage_file_name() != null && !produit.getImage_file_name().isEmpty()) {
            try {
                // First try the absolute path
                File imageFile = new File(produit.getImage_file_name());
                
                // If not an absolute path, try the relative path in our image directory
                if (!imageFile.exists() || !imageFile.isAbsolute()) {
                    imageFile = new File(IMAGE_DIRECTORY + produit.getImage_file_name());
                }
                
                if (imageFile.exists() && imageFile.isFile()) {
                    showImagePreview(imageFile.getAbsolutePath());
                }
            } catch (Exception e) {
                System.err.println("Error loading image preview: " + e.getMessage());
            }
        }
    }
    
    @FXML
    void handleAddButtonAction(ActionEvent event) {
        try {
            if (validateInputs()) {
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
                
                boolean success = produitController.create(produit);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit ajouté avec succès");
                    clearFields();
                    loadAllProducts();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout du produit");
                }
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer des valeurs numériques valides pour la quantité et le prix");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur s'est produite: " + e.getMessage());
        }
    }
    
    @FXML
    void handleUpdateButtonAction(ActionEvent event) {
        try {
            if (selectedProduit != null && validateInputs()) {
                selectedProduit.setNom(nomField.getText());
                selectedProduit.setQuantite(Integer.parseInt(quantiteField.getText()));
                selectedProduit.setPrix(Double.parseDouble(prixField.getText()));
                selectedProduit.setCategories(categoriesCombo.getValue());
                selectedProduit.setDescription(descriptionArea.getText());
                
                String newImageName = imageFileNameField.getText();
                if (!newImageName.equals(selectedProduit.getImage_file_name())) {
                    copyImageIfNeeded(newImageName);
                    selectedProduit.setImage_file_name(newImageName);
                }
                
                selectedProduit.setApproved(approvedCheck.isSelected());
                
                boolean success = produitController.update(selectedProduit);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit mis à jour avec succès");
                    clearFields();
                    loadAllProducts();
                    selectedProduit = null;
                    updateButton.setDisable(true);
                    deleteButton.setDisable(true);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la mise à jour du produit");
                }
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer des valeurs numériques valides pour la quantité et le prix");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur s'est produite: " + e.getMessage());
        }
    }
    
    @FXML
    void handleDeleteButtonAction(ActionEvent event) {
        if (selectedProduit != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmation");
            confirmAlert.setHeaderText("Supprimer le produit?");
            confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer ce produit?");
            
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean success = produitController.delete(selectedProduit.getId());
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit supprimé avec succès");
                        clearFields();
                        loadAllProducts();
                        selectedProduit = null;
                        updateButton.setDisable(true);
                        deleteButton.setDisable(true);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression du produit");
                    }
                }
            });
        }
    }
    
    @FXML
    void handleClearButtonAction(ActionEvent event) {
        clearFields();
        selectedProduit = null;
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }
    
    @FXML
    void handleBrowseButtonAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        // Set initial directory to user's desktop if possible
        try {
            String userHome = System.getProperty("user.home");
            File desktopDir = new File(userHome + "/Desktop");
            if (desktopDir.exists()) {
                fileChooser.setInitialDirectory(desktopDir);
            }
        } catch (Exception e) {
            // If we can't access desktop, that's fine
        }
        
        File selectedFile = fileChooser.showOpenDialog(browseButton.getScene().getWindow());
        if (selectedFile != null) {
            imageFileNameField.setText(selectedFile.getAbsolutePath());
            System.out.println("Selected image: " + selectedFile.getAbsolutePath());
            
            // Preview the selected image
            showImagePreview(selectedFile.getAbsolutePath());
        }
    }
    
    @FXML
    void handleSearchButtonAction(ActionEvent event) {
        String keyword = searchField.getText().trim();
        
        // Vérifier si le champ de recherche contient des caractères spéciaux SQL
        if (keyword != null && !keyword.isEmpty()) {
            if (containsSQLInjection(keyword)) {
                showAlert(Alert.AlertType.WARNING, "Attention", 
                         "Recherche non valide. Votre recherche contient des caractères non autorisés.");
                return;
            }
            
            // Limiter la longueur de la recherche
            if (keyword.length() > 50) {
                showAlert(Alert.AlertType.WARNING, "Attention", 
                         "Veuillez limiter votre recherche à 50 caractères maximum.");
                searchField.setText(keyword.substring(0, 50));
                return;
            }
        }
        
        if (keyword.isEmpty()) {
            loadAllProducts();
        } else {
            produitData.clear();
            List<Produit> foundProducts = produitController.searchByName(keyword);
            produitData.addAll(foundProducts);
            displayProductCards();
        }
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
    
    /**
     * Validation des entrées formulaire
     */
    private void setupInputValidation() {
        // Limitation du nombre de caractères dans les champs
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 100) {
                nomField.setText(oldValue); // Restaure l'ancienne valeur
            }
        });
        
        // N'accepte que les chiffres pour la quantité
        quantiteField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                quantiteField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        // N'accepte que les chiffres et un point pour le prix
        prixField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                prixField.setText(oldValue);
            }
        });
        
        // Limitation du nombre de caractères dans la description
        descriptionArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 1000) {
                descriptionArea.setText(oldValue); // Restaure l'ancienne valeur
            }
        });
    }
    
    private void filterByCategory() {
        String selectedCategory = filterCategoryCombo.getValue();
        if (selectedCategory == null || selectedCategory.equals("Toutes")) {
            loadAllProducts();
        } else {
            produitData.clear();
            List<Produit> filteredProducts = produitController.filterByCategory(selectedCategory);
            produitData.addAll(filteredProducts);
            displayProductCards();
        }
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
    
    private boolean validateInputs() {
        StringBuilder errorMessages = new StringBuilder();
        boolean isValid = true;
        
        // Validation du nom
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            errorMessages.append("- Le nom du produit ne peut pas être vide\n");
            nomField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (nomField.getText().trim().length() < 3) {
            errorMessages.append("- Le nom du produit doit contenir au moins 3 caractères\n");
            nomField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            nomField.setStyle("");
        }
        
        // Validation de la quantité
        if (quantiteField.getText() == null || quantiteField.getText().trim().isEmpty()) {
            errorMessages.append("- La quantité ne peut pas être vide\n");
            quantiteField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (!isPositiveInteger(quantiteField.getText())) {
            errorMessages.append("- La quantité doit être un nombre entier positif\n");
            quantiteField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            int quantity = Integer.parseInt(quantiteField.getText());
            if (quantity <= 0) {
                errorMessages.append("- La quantité doit être supérieure à zéro\n");
                quantiteField.setStyle("-fx-border-color: red;");
                isValid = false;
            } else {
                quantiteField.setStyle("");
            }
        }
        
        // Validation du prix
        if (prixField.getText() == null || prixField.getText().trim().isEmpty()) {
            errorMessages.append("- Le prix ne peut pas être vide\n");
            prixField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (!isPositiveDouble(prixField.getText())) {
            errorMessages.append("- Le prix doit être un nombre positif (utilisez un point pour les décimales)\n");
            prixField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            double price = Double.parseDouble(prixField.getText());
            if (price <= 0) {
                errorMessages.append("- Le prix doit être supérieur à zéro\n");
                prixField.setStyle("-fx-border-color: red;");
                isValid = false;
            } else {
                prixField.setStyle("");
            }
        }
        
        // Validation de la catégorie
        if (categoriesCombo.getValue() == null || categoriesCombo.getValue().trim().isEmpty()) {
            errorMessages.append("- Veuillez sélectionner une catégorie\n");
            categoriesCombo.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            categoriesCombo.setStyle("");
        }
        
        // Validation de la description (optionnelle mais limitée en taille)
        if (descriptionArea.getText() != null && descriptionArea.getText().length() > 1000) {
            errorMessages.append("- La description est trop longue (maximum 1000 caractères)\n");
            descriptionArea.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            descriptionArea.setStyle("");
        }
        
        // Affichage des erreurs si nécessaire
        if (!isValid) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de validation");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes :");
            alert.setContentText(errorMessages.toString());
            alert.showAndWait();
        }
        
        return isValid;
    }
    
    private boolean isPositiveInteger(String value) {
        try {
            int number = Integer.parseInt(value);
            return number >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean isPositiveDouble(String value) {
        try {
            double number = Double.parseDouble(value);
            return number >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private void copyImageIfNeeded(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return;
        }
        
        File sourceFile = new File(imagePath);
            if (sourceFile.exists() && sourceFile.isFile()) {
            try {
                // Make sure target directory exists
                File imagesDir = new File(IMAGE_DIRECTORY);
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs();
                }
                
                String fileName = sourceFile.getName();
                Path targetPath = Paths.get(IMAGE_DIRECTORY + fileName);
                
                System.out.println("Copying image from " + sourceFile.getAbsolutePath() + " to " + targetPath);
                
                try {
                    // Copy the file
                    Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                    
                    // Update the field to just contain the filename, not the full path
                    imageFileNameField.setText(fileName);
                    
                    System.out.println("Image copied successfully to: " + targetPath);
                } catch (IOException e) {
                    System.err.println("Warning: Could not copy image file. Using absolute path instead: " + e.getMessage());
                    // If we can't copy the file, use the absolute path instead
                    imageFileNameField.setText(sourceFile.getAbsolutePath());
                }
                } catch (Exception e) {
                System.err.println("Failed to process image: " + e.getMessage());
                e.printStackTrace();
                
                // Use absolute path as fallback
                imageFileNameField.setText(sourceFile.getAbsolutePath());
            }
        } else {
            System.err.println("Source file doesn't exist or is not a file: " + imagePath);
        }
    }
    
    private void showImagePreview(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists() && imageFile.isFile()) {
                FileInputStream fis = new FileInputStream(imageFile);
                Image image = new Image(fis, 300, 300, true, true);
                
                if (!image.isError()) {
                    // Create a preview dialog
                    Stage previewStage = new Stage();
                    previewStage.initModality(Modality.NONE);
                    previewStage.setTitle("Aperçu de l'image");
                    
                    // Create an ImageView for the preview
                    ImageView previewImage = new ImageView(image);
                    previewImage.setFitWidth(300);
                    previewImage.setFitHeight(300);
                    previewImage.setPreserveRatio(true);
                    
                    // Add the ImageView to a BorderPane
                    BorderPane root = new BorderPane();
                    root.setCenter(previewImage);
                    root.setPadding(new Insets(10));
                    
                    // Create a Scene and add it to the Stage
                    Scene scene = new Scene(root);
                    previewStage.setScene(scene);
                    
                    // Show the preview
                    previewStage.show();
                }
                
                fis.close();
            }
        } catch (Exception e) {
            System.err.println("Error showing image preview: " + e.getMessage());
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    void handleAdminButtonAction(ActionEvent event) {
        try {
            // Load the Admin view
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
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation", 
                     "Impossible de charger la vue Admin");
        }
    }
    
    @FXML
    void handleLogoutButtonAction(ActionEvent event) {
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
                    System.err.println("Error during logout: " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    void handleMarketplaceButtonAction(ActionEvent event) {
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
                     "Impossible de charger la vue Marketplace");
        }
    }
} 