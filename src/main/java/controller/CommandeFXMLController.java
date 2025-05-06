package controller;

import entite.Commande;
import entite.Produit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.CommandeService;
import service.ProduitService;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandeFXMLController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(CommandeFXMLController.class.getName());

    @FXML private TextField quantiteField;
    @FXML private TextField prixField;
    @FXML private ComboBox<String> typeCommandeCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextField adressField;
    @FXML private ComboBox<String> paimentCombo;
    

    @FXML private Button updateButton;
    @FXML private Button deleteButton;

    
    @FXML private TableView<Commande> commandeTable;
    @FXML private TableColumn<Commande, Integer> idColumn;
    @FXML private TableColumn<Commande, Integer> quantiteColumn;
    @FXML private TableColumn<Commande, Double> prixColumn;
    @FXML private TableColumn<Commande, String> typeCommandeColumn;
    @FXML private TableColumn<Commande, String> statusColumn;
    @FXML private TableColumn<Commande, Date> dateCreationColumn;
    
    @FXML private Button marketplaceButton;
    @FXML private Button agricoleButton;
    @FXML private Button adminButton;
    @FXML private Button logoutButton;
    
    @FXML private ListView<Produit> produitsListView;
    @FXML private Button addProduitButton;
    @FXML private Button removeProduitButton;
    
    private final CommandeService commandeService = new CommandeService();
    private final ProduitService produitService = new ProduitService();
    private Commande selectedCommande;
    private final ObservableList<Commande> commandeData = FXCollections.observableArrayList();
    private final ObservableList<Produit> allProduits = FXCollections.observableArrayList();
    private final ObservableList<Produit> selectedProduits = FXCollections.observableArrayList();

    private static final List<String> TYPES_COMMANDE = Arrays.asList(
        "Achat direct", "Précommande", "Réservation", "Livraison"
    );
    
    private static final List<String> STATUS_COMMANDE = Arrays.asList(
        "En attente", "Confirmée", "En cours", "Livrée", "Annulée"
    );
    
    private static final List<String> MODES_PAIEMENT = Arrays.asList(
        "Carte bancaire", "Espèces", "Chèque", "Virement bancaire", "Paiement à la livraison"
    );
    
    // Variable pour stocker le produit à commander (passé depuis le marketplace)
    private Produit produitACommander;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser les ComboBox
        typeCommandeCombo.getItems().addAll(TYPES_COMMANDE);
        statusCombo.getItems().addAll(STATUS_COMMANDE);
        paimentCombo.getItems().addAll(MODES_PAIEMENT);
        
        // Initialiser les colonnes de la TableView
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        typeCommandeColumn.setCellValueFactory(new PropertyValueFactory<>("type_commande"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateCreationColumn.setCellValueFactory(new PropertyValueFactory<>("date_creation_commande"));
        
        // Ajouter un écouteur de sélection pour la TableView
        commandeTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    handleCommandeSelection(newSelection);
                }
            }
        );
        // Charger toutes les commandes
        loadAllCommandes();
        
        // Charger tous les produits pour la liste de sélection
        loadAllProduits();
        
        // Configurer les boutons pour gérer les produits
        setupProduitButtons();
    }
    
    // Méthode pour définir le produit à commander

    
    private void setupProduitButtons() {
        addProduitButton.setOnAction(event -> {
            // Ouvrir une boîte de dialogue pour sélectionner des produits
            Dialog<List<Produit>> dialog = createProduitSelectionDialog();
            dialog.showAndWait().ifPresent(produits -> {
                if (produits != null && !produits.isEmpty()) {
                    for (Produit p : produits) {
                        if (!selectedProduits.contains(p)) {
                            selectedProduits.add(p);
                        }
                    }
                    // Mise à jour du prix total
                    updateTotalPrice();
                }
            });
        });
        
        removeProduitButton.setOnAction(event -> {
            Produit selectedProduit = produitsListView.getSelectionModel().getSelectedItem();
            if (selectedProduit != null) {
                selectedProduits.remove(selectedProduit);
                // Mise à jour du prix total
                updateTotalPrice();
            }
        });
    }
    
    private void updateTotalPrice() {
        double totalPrice = 0;
        for (Produit p : selectedProduits) {
            totalPrice += p.getPrix();
        }
        prixField.setText(String.format("%.2f", totalPrice));
    }
    
    private Dialog<List<Produit>> createProduitSelectionDialog() {
        Dialog<List<Produit>> dialog = new Dialog<>();
        dialog.setTitle("Sélectionner des produits");
        dialog.setHeaderText("Choisissez les produits pour cette commande");
        
        ButtonType confirmButtonType = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        
        ListView<Produit> produitsView = new ListView<>();
        produitsView.setItems(allProduits);
        produitsView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // Pré-sélectionner les produits déjà choisis
        for (Produit p : selectedProduits) {
            if (allProduits.contains(p)) {
                produitsView.getSelectionModel().select(p);
            }
        }
        
        VBox content = new VBox(10);
        content.getChildren().add(new Label("Produits disponibles:"));
        content.getChildren().add(produitsView);
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return new ArrayList<>(produitsView.getSelectionModel().getSelectedItems());
            }
            return null;
        });
        
        return dialog;
    }
    
    private void loadAllCommandes() {
        commandeData.clear();
        List<Commande> commandes = commandeService.readAll();
        commandeData.addAll(commandes);
        commandeTable.setItems(commandeData);
    }
    
    private void loadAllProduits() {
        allProduits.clear();
        List<Produit> produits = produitService.readAll();
        allProduits.addAll(produits);
    }
    
    private void handleCommandeSelection(Commande commande) {
        selectedCommande = commande;
        
        // Remplir les champs avec les données de la commande sélectionnée
        quantiteField.setText(String.valueOf(commande.getQuantite()));
        prixField.setText(String.valueOf(commande.getPrix()));
        typeCommandeCombo.setValue(commande.getType_commande());
        statusCombo.setValue(commande.getStatus());
        adressField.setText(commande.getAdress());
        paimentCombo.setValue(commande.getPaiment());
        
        // Afficher les produits de la commande
        selectedProduits.clear();
        selectedProduits.addAll(commande.getProduits());
        produitsListView.setItems(selectedProduits);
        
        // Activer les boutons de mise à jour et suppression
        updateButton.setDisable(false);
        deleteButton.setDisable(false);
    }
    
    @FXML
    void handleAddButtonAction(ActionEvent event) {
        try {
            if (validateInputs()) {
                Commande commande = new Commande();
                commande.setQuantite(Integer.parseInt(quantiteField.getText()));
                commande.setPrix(Double.parseDouble(prixField.getText())); 
                commande.setType_commande(typeCommandeCombo.getValue());
                commande.setStatus(statusCombo.getValue());
                commande.setAdress(adressField.getText());
                commande.setPaiment(paimentCombo.getValue());
                
                // Ajouter les produits sélectionnés à la commande
                for (Produit p : selectedProduits) {
                    commande.addProduit(p);
                }
                
                boolean success = commandeService.create(commande);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande ajoutée avec succès", 
                             "La commande a été enregistrée dans la base de données.");
                    clearFields();
                    loadAllCommandes();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout de la commande", 
                             "Une erreur s'est produite lors de l'ajout de la commande.");
                }
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer des valeurs numériques valides", 
                     "Les champs quantité et prix doivent contenir des valeurs numériques.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur s'est produite", e.getMessage());
        }
    }
    
    @FXML
    void handleUpdateButtonAction(ActionEvent event) {
        if (selectedCommande == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucune commande sélectionnée", 
                     "Veuillez sélectionner une commande à mettre à jour.");
            return;
        }
        
        try {
            if (validateInputs()) {
                selectedCommande.setQuantite(Integer.parseInt(quantiteField.getText()));
                selectedCommande.setPrix(Double.parseDouble(prixField.getText()));
                selectedCommande.setType_commande(typeCommandeCombo.getValue());
                selectedCommande.setStatus(statusCombo.getValue());
                selectedCommande.setAdress(adressField.getText());
                selectedCommande.setPaiment(paimentCombo.getValue());
                
                // Mettre à jour les produits
                selectedCommande.getProduits().clear();
                for (Produit p : selectedProduits) {
                    selectedCommande.addProduit(p);
                }
                
                boolean success = commandeService.update(selectedCommande);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande mise à jour avec succès",
                             "La commande a été mise à jour dans la base de données.");
                    clearFields();
                    loadAllCommandes();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la mise à jour de la commande",
                             "Une erreur s'est produite lors de la mise à jour de la commande.");
                }
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer des valeurs numériques valides",
                     "Les champs quantité et prix doivent contenir des valeurs numériques.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur s'est produite", e.getMessage());
        }
    }
    
    @FXML
    void handleDeleteButtonAction(ActionEvent event) {
        if (selectedCommande == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucune commande sélectionnée",
                     "Veuillez sélectionner une commande à supprimer.");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de Suppression");
        confirmation.setHeaderText("Supprimer la commande");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette commande ? Cette action est irréversible.");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = commandeService.delete(selectedCommande.getId());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande supprimée avec succès",
                             "La commande a été supprimée de la base de données.");
                    clearFields();
                    loadAllCommandes();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression de la commande",
                             "Une erreur s'est produite lors de la suppression de la commande.");
                }
            }
        });
    }
    
    @FXML
    void handleClearButtonAction(ActionEvent event) {
        clearFields();
    }
    
    private void clearFields() {
        quantiteField.clear();
        prixField.clear();
        typeCommandeCombo.getSelectionModel().clearSelection();
        statusCombo.getSelectionModel().clearSelection();
        adressField.clear();
        paimentCombo.getSelectionModel().clearSelection();
        
        selectedProduits.clear();
        produitsListView.setItems(selectedProduits);
        
        selectedCommande = null;
        commandeTable.getSelectionModel().clearSelection();
        
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }
    
    private boolean validateInputs() {
        if (quantiteField.getText() == null || quantiteField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champ manquant", "La quantité est requise",
                     "Veuillez entrer une valeur pour la quantité.");
            return false;
        }
        
        if (prixField.getText() == null || prixField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champ manquant", "Le prix est requis",
                     "Veuillez entrer une valeur pour le prix.");
            return false;
        }
        
        if (typeCommandeCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Champ manquant", "Le type de commande est requis",
                     "Veuillez sélectionner un type de commande.");
            return false;
        }
        
        if (statusCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Champ manquant", "Le statut est requis",
                     "Veuillez sélectionner un statut pour la commande.");
            return false;
        }
        
        if (adressField.getText() == null || adressField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champ manquant", "L'adresse est requise",
                     "Veuillez entrer une adresse pour la livraison.");
            return false;
        }
        
        if (paimentCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Champ manquant", "Le mode de paiement est requis",
                     "Veuillez sélectionner un mode de paiement.");
            return false;
        }
        
        if (selectedProduits.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Produits manquants", "Aucun produit sélectionné",
                     "Veuillez sélectionner au moins un produit pour cette commande.");
            return false;
        }
        
        return true;
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
                     "Impossible de charger la vue Marketplace", 
                     "Une erreur s'est produite: " + e.getMessage());
        }
    }
    
    @FXML
    void handleAgricoleButtonAction(ActionEvent event) {
        try {
            // Load the Agricole view
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
                     "Impossible de charger la vue Admin", 
                     "Une erreur s'est produite: " + e.getMessage());
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
                    LOGGER.log(Level.SEVERE, "Error during logout", e);
                }
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