package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.CropManageAuthService;
import service.CropManageSoilService;
import service.SoilDataCRUD;
import entite.SoilData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SoilDataController {
    private SoilDataCRUD soilDataCRUD = new SoilDataCRUD();
    private SoilData selectedSoilData;
    private int currentCropId;
    private final CropManageAuthService apiAuthService = new CropManageAuthService();
    private final CropManageSoilService apiSoilService = new CropManageSoilService(apiAuthService);

    @FXML private VBox soilDataCardsPane;
    @FXML private TextField humiditeField;
    @FXML private TextField niveauPhField;
    @FXML private TextField niveauNutrimentField;
    @FXML private ComboBox<String> typeSolCombo;
    @FXML private TextArea resultArea;
    @FXML private Button statisticsButton;
    @FXML
    private Button recommandationButton;


    public void setCropId(int cropId) {
        this.currentCropId = cropId;
        loadSoilData();
    }

    @FXML
    public void initialize() {
        // Initialize type sol combo box if it exists
        if (typeSolCombo != null) {
            typeSolCombo.getItems().addAll("Clayey", "Sandy", "Silty", "Humiferous", "Limestone");
        }

        // Load soil types from API in background
        new Thread(this::populateSoilTypesFromApi).start();
        // Only load soil data if we're in the main view (soilDataCardsPane exists)
        if (soilDataCardsPane != null) {
            loadSoilData();
        }
    }

    @FXML
    private void showAddSoilDataForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("add_soil_data.fxml"));
            Parent root = loader.load();

            SoilDataController controller = loader.getController();
            controller.currentCropId = this.currentCropId;

            Stage stage = new Stage();
            stage.setTitle("Add New Soil Data");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Only reload if we're in the main view
            if (soilDataCardsPane != null) {
                loadSoilData();
            }
        } catch (IOException e) {
            if (resultArea != null) {
                resultArea.setText("Error loading add form: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) resultArea.getScene().getWindow();
        stage.close();
    }

    private void loadSoilData() {
        try {
            if (soilDataCardsPane == null) return; // Skip if we're in the add/edit form

            List<SoilData> soilDataList = soilDataCRUD.getSoilDataByCropId(currentCropId);
            displaySoilDataCards(soilDataList);
        } catch (SQLException e) {
            if (resultArea != null) {
                resultArea.setText("Error loading soil data: " + e.getMessage());
            }
        }
    }

    private void displaySoilDataCards(List<SoilData> soilDataList) {
        if (soilDataCardsPane == null) return; // Skip if we're in the add/edit form

        // Clear existing cards
        soilDataCardsPane.getChildren().clear();

        // Add new cards
        for (SoilData soilData : soilDataList) {
            VBox card = createSoilDataCard(soilData);
            VBox.setMargin(card, new Insets(5, 5, 5, 5));
            soilDataCardsPane.getChildren().add(card);
        }
    }

    private VBox createSoilDataCard(SoilData soilData) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(300);
        card.setPrefHeight(250);

        // Header with title and delete button
        StackPane header = new StackPane();
        header.setStyle("-fx-background-color: #4CAF50; -fx-background-radius: 10 10 0 0;");
        header.setPrefHeight(40);

        Label titleLabel = new Label("Soil Data #" + soilData.getId());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        Button deleteButton = new Button("X");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 0; -fx-min-width: 25; -fx-min-height: 25; -fx-padding: 0; -fx-border-width: 0;");
        deleteButton.setOnAction(event -> {
            try {
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Confirm Delete");
                confirmDialog.setHeaderText("Delete Soil Data");
                confirmDialog.setContentText("Are you sure you want to delete this soil data? This action cannot be undone.");

                if (confirmDialog.showAndWait().get() == ButtonType.OK) {
                    soilDataCRUD.deleteSoilData(soilData.getId());
                    loadSoilData();
                    if (resultArea != null) {
                        resultArea.setText("Soil data deleted successfully!");
                    }
                }
            } catch (SQLException e) {
                if (resultArea != null) {
                    resultArea.setText("Error deleting soil data: " + e.getMessage());
                }
            }
        });

        StackPane.setAlignment(deleteButton, Pos.TOP_RIGHT);
        StackPane.setMargin(deleteButton, new Insets(5, 5, 0, 0));

        header.getChildren().addAll(titleLabel, deleteButton);

        // Soil data details
        VBox details = new VBox(10);
        details.setAlignment(Pos.CENTER_LEFT);
        details.setPadding(new Insets(20));
        details.setStyle("-fx-background-color: white;");

        Label humiditeLabel = new Label("Humidity: " + soilData.getHumidite() + "%");
        humiditeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

        Label phLabel = new Label("pH Level: " + soilData.getNiveau_ph());
        phLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

        Label nutrimentLabel = new Label("Nutrient Level: " + soilData.getNiveau_nutriment());
        nutrimentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

        Label typeSolLabel = new Label("Soil Type: " + soilData.getType_sol());
        typeSolLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

        // Action buttons
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(10));
        buttons.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 0 0 10 10;");

        Button updateButton = new Button("Update");
        updateButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 5;");
        updateButton.setOnAction(event -> {
            try {
                selectedSoilData = soilData;
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/add_soil_data.fxml"));
                Parent root = loader.load();

                SoilDataController controller = loader.getController();
                controller.selectedSoilData = soilData;
                controller.currentCropId = this.currentCropId;
                controller.populateFields(soilData);

                Stage stage = new Stage();
                stage.setTitle("Update Soil Data");
                stage.setScene(new Scene(root));
                stage.showAndWait();

                // Only reload if we're in the main view
                if (soilDataCardsPane != null) {
                    loadSoilData();
                }
            } catch (IOException e) {
                if (resultArea != null) {
                    resultArea.setText("Error loading update form: " + e.getMessage());
                }
            }
        });

        Button detailsButton = new Button("Details");
        detailsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 5;");
        detailsButton.setOnAction(event -> {
            if (resultArea != null) {
                resultArea.setText(soilData.toString());
            }
        });

        buttons.getChildren().addAll(updateButton, detailsButton);

        details.getChildren().addAll(humiditeLabel, phLabel, nutrimentLabel, typeSolLabel);
        card.getChildren().addAll(header, details, buttons);

        return card;
    }

    @FXML
    private void createSoilData() {
        try {
            if (validateInputs()) {
                // Get current date
                String currentDate = java.time.LocalDate.now().toString();

                SoilData soilData = new SoilData(
                        0,
                        Double.parseDouble(humiditeField.getText()),
                        Double.parseDouble(niveauPhField.getText()),
                        Double.parseDouble(niveauNutrimentField.getText()),
                        typeSolCombo.getValue(),
                        currentCropId,
                        currentDate
                );

                if (selectedSoilData != null) {
                    // Update existing soil data
                    soilData.setId(selectedSoilData.getId());
                    soilDataCRUD.updateSoilData(soilData);
                    if (resultArea != null) {
                        resultArea.setText("Soil data updated successfully!");
                    }
                } else {
                    // Create new soil data
                    soilDataCRUD.createSoilData(soilData);
                    if (resultArea != null) {
                        resultArea.setText("Soil data created successfully!");
                    }
                }

                // Close the window
                Stage stage = (Stage) humiditeField.getScene().getWindow();
                stage.close();
            }
        } catch (SQLException e) {
            if (resultArea != null) {
                resultArea.setText("Error saving soil data: " + e.getMessage());
            }
        }
    }

    private void populateFields(SoilData soilData) {
        humiditeField.setText(String.valueOf(soilData.getHumidite()));
        niveauPhField.setText(String.valueOf(soilData.getNiveau_ph()));
        niveauNutrimentField.setText(String.valueOf(soilData.getNiveau_nutriment()));
        typeSolCombo.setValue(soilData.getType_sol());
    }

    private void clearFields() {
        humiditeField.clear();
        niveauPhField.clear();
        niveauNutrimentField.clear();
        typeSolCombo.getSelectionModel().clearSelection();
    }

    private boolean validateInputs() {
        try {
            if (humiditeField.getText().isEmpty() ||
                    niveauPhField.getText().isEmpty() ||
                    niveauNutrimentField.getText().isEmpty()) {
                if (resultArea != null) {
                    resultArea.setText("Please fill in all fields");
                }
                return false;
            }

            double humidite = Double.parseDouble(humiditeField.getText());
            double ph = Double.parseDouble(niveauPhField.getText());
            double nutriments = Double.parseDouble(niveauNutrimentField.getText());

            if (humidite < 0 || humidite > 100) {
                if (resultArea != null) {
                    resultArea.setText("Humidity must be between 0 and 100");
                }
                return false;
            }

            if (ph < 0 || ph > 14) {
                if (resultArea != null) {
                    resultArea.setText("pH must be between 0 and 14");
                }
                return false;
            }

            if (nutriments < 0) {
                if (resultArea != null) {
                    resultArea.setText("Nutrient level cannot be negative");
                }
                return false;
            }

            if (typeSolCombo.getValue() == null) {
                if (resultArea != null) {
                    resultArea.setText("Please select a soil type");
                }
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            if (resultArea != null) {
                resultArea.setText("Please enter valid numbers for all fields");
            }
            return false;
        }
    }

    @FXML
    private void goBackToCrops() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("crop.fxml"));
            Parent root = loader.load();

            // Get the current stage from any node in the scene
            Stage stage = (Stage) (soilDataCardsPane != null ? soilDataCardsPane.getScene().getWindow() :
                    resultArea != null ? resultArea.getScene().getWindow() :
                            typeSolCombo.getScene().getWindow());

            // Set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Crop Management");
            stage.show();
        } catch (IOException e) {
            if (resultArea != null) {
                resultArea.setText("Error going back to crops: " + e.getMessage());
            }
        }
    }

    @FXML
    private void showStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/soil_statistics.fxml"));
            Parent root = loader.load();

            // Get the controller and set the crop ID
            SoilStatisticsController controller = loader.getController();
            controller.setCropId(currentCropId);

            // Create a new stage for the statistics view
            Stage stage = new Stage();
            stage.setTitle("Soil Data Statistics");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            if (resultArea != null) {
                resultArea.setText("Error loading statistics view: " + e.getMessage());
            }
        }
    }

    private void populateSoilTypesFromApi() {
        List<Map<String, Object>> soilTypes = apiSoilService.getSoilTypes();

        if (soilTypes != null && !soilTypes.isEmpty()) {
            Platform.runLater(() -> {
                typeSolCombo.getItems().clear();
                for (Map<String, Object> soilType : soilTypes) {
                    typeSolCombo.getItems().add((String) soilType.get("Name"));
                }
            });
        }
    }


}
