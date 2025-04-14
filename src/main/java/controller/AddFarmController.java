package controller;

import entite.Farm;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import service.FarmService;

import java.io.IOException;

public class AddFarmController {
    @FXML private TextField nameField;
    @FXML private TextField locationField;
    @FXML private TextField addressField;
    @FXML private TextField surfaceField;
    @FXML private TextField budgetField;
    @FXML private TextField latField;
    @FXML private TextField lonField;
    @FXML private TextArea descriptionField;

    @FXML private CheckBox bircheck;
    @FXML private CheckBox irrigationCheck;
    @FXML private CheckBox photoCheck;
    @FXML private CheckBox fence;
    @FXML private CheckBox cabincheck;

    private final FarmService farmService = new FarmService();

    @FXML
    public void initialize() {
        // No initialization needed
    }

    @FXML
    private void handleupate() {
      
            try {
                // Mettre à jour les données du Farm existant
                if (currentFarm != null) {
                    currentFarm.setName(nameField.getText());
                    currentFarm.setLocation(locationField.getText());
                    currentFarm.setAdress(addressField.getText());
                    currentFarm.setSurface(Double.parseDouble(surfaceField.getText()));
                    currentFarm.setBudget(Double.parseDouble(budgetField.getText()));
                    currentFarm.setLat(Float.parseFloat(latField.getText()));
                    currentFarm.setLon(Float.parseFloat(lonField.getText()));
                    currentFarm.setDescription(descriptionField.getText());
                    currentFarm.setBir(bircheck.isSelected());
                    currentFarm.setIrrigation(irrigationCheck.isSelected());
                    currentFarm.setPhotovoltaic(photoCheck.isSelected());
                    currentFarm.setFence(fence.isSelected());
                    currentFarm.setCabin(cabincheck.isSelected());

                    // Sauvegarder les modifications dans la base de données
                    farmService.update(currentFarm);

                    showAlert(AlertType.INFORMATION, "Succès", "Farm modifié avec succès !");
                    returnToGrid();
                }
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Erreur", "Impossible de modifier le Farm : " + e.getMessage());
            }
        }

    @FXML
    private void handleCancel() {
        returnToGrid();
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void returnToGrid() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/farmdisplay.fxml"));
            BorderPane farmGrid = loader.load();
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.getScene().setRoot(farmGrid);
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Error", "Could not load farm grid: " + e.getMessage());
        }
    }

    private Farm currentFarm;

    public void setFarm(Farm farm) {
        this.currentFarm = farm;

        // Pré-remplir les champs
        nameField.setText(farm.getName());
        locationField.setText(farm.getLocation());
        addressField.setText(farm.getAdress());
        surfaceField.setText(String.valueOf(farm.getSurface()));
        budgetField.setText(String.valueOf(farm.getBudget()));
        latField.setText(String.valueOf(farm.getLat()));
        lonField.setText(String.valueOf(farm.getLon()));
        descriptionField.setText(farm.getDescription());

        // Pré-remplir les cases à cocher
        bircheck.setSelected(farm.isBir());
        irrigationCheck.setSelected(farm.isIrrigation());
        photoCheck.setSelected(farm.isPhotovoltaic());
        fence.setSelected(farm.isFence());
        cabincheck.setSelected(farm.isCabin());
    }
    @FXML
    private void handleSave() {
        try {
            // Validate required fields
            if (nameField.getText().isEmpty() || addressField.getText().isEmpty() ||
                    surfaceField.getText().isEmpty() || budgetField.getText().isEmpty()) {
                showAlert(AlertType.ERROR, "Validation Error", "Please fill all required fields");
                return;
            }

            // Create new farm
            Farm farm = new Farm();
            farm.setName(nameField.getText());
            farm.setLocation(locationField.getText());
            farm.setAdress(addressField.getText());
            farm.setSurface(Float.parseFloat(surfaceField.getText()));
            farm.setBudget(Float.parseFloat(budgetField.getText()));
            farm.setDescription(descriptionField.getText());;

            // Set coordinates
            if (!latField.getText().isEmpty()) {
                farm.setLat(Float.parseFloat(latField.getText()));
            }
            if (!lonField.getText().isEmpty()) {
                farm.setLon(Float.parseFloat(lonField.getText()));
            }

            // Set boolean flags
            farm.setBir(bircheck.isSelected());
            farm.setIrrigation(irrigationCheck.isSelected());
            farm.setCabin(cabincheck.isSelected());
            farm.setFence(fence.isSelected());
            farm.setPhotovoltaic(photoCheck.isSelected());

            // Save to database
            farmService.create(farm);

            // Show success message
            showAlert(AlertType.INFORMATION, "Success", "Farm added successfully!");

            // Return to farm grid
            returnToGrid();

        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Input Error", "Please enter valid numbers for numeric fields");
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Error", "Failed to add farm: " + e.getMessage());
        }
    }



}