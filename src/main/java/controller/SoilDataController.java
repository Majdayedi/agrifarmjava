package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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
import java.util.stream.Collectors;

public class SoilDataController {
    private SoilDataCRUD soilDataCRUD = new SoilDataCRUD();
    private SoilData selectedSoilData;
    private int currentCropId;
    private final CropManageAuthService apiAuthService = new CropManageAuthService();
    private final CropManageSoilService apiSoilService = new CropManageSoilService(apiAuthService);

    @FXML private FlowPane soilDataCardsPane;
    @FXML private TextField humiditeField;
    @FXML private TextField niveauPhField;
    @FXML private TextField niveauNutrimentField;
    @FXML private ComboBox<String> typeSolCombo;
    @FXML private TextArea resultArea;
    @FXML private Button statisticsButton;
    @FXML
    private Button recommandationButton;

    @FXML
    private BarChart<String, Number> humidityChart;
    @FXML
    private BarChart<String, Number> phChart;
    @FXML
    private BarChart<String, Number> nutrientChart;



    public void setCropId(int cropId) {
        this.currentCropId = cropId;
        loadStatistics();
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
        VBox card = new VBox(10);
        card.setPrefWidth(300);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 1); -fx-padding: 16;");

        // Top Image + Delete Button
        HBox topBox = new HBox();
        topBox.setAlignment(Pos.TOP_RIGHT);

        ImageView icon = new ImageView(new Image(getClass().getResource("/images/soildata.png").toExternalForm()));
        icon.setFitHeight(30);
        icon.setFitWidth(30);
        icon.setPreserveRatio(true);
        icon.setSmooth(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("X");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 20; -fx-min-width: 25; -fx-min-height: 25;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this soil data?", ButtonType.OK, ButtonType.CANCEL);
            confirm.setHeaderText("Confirm Deletion");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        soilDataCRUD.deleteSoilData(soilData.getId());
                        loadSoilData();
                        if (resultArea != null) resultArea.setText("Deleted successfully!");
                    } catch (SQLException ex) {
                        if (resultArea != null) resultArea.setText("Delete error: " + ex.getMessage());
                    }
                }
            });
        });

        topBox.getChildren().addAll(icon, spacer, deleteBtn);

        // Labels
        Label title = new Label("Soil Data #" + soilData.getId());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #2c3e50;");

        Label subTitle = new Label(soilData.getType_sol());
        subTitle.setStyle("-fx-text-fill: #666666; -fx-font-size: 12;");

        Label humidity = new Label("Humidity: " + soilData.getHumidite() + "%");
        Label ph = new Label("pH Level: " + soilData.getNiveau_ph());
        Label nutrient = new Label("Nutrient: " + soilData.getNiveau_nutriment());

        humidity.setStyle("-fx-text-fill: #2c3e50;");
        ph.setStyle("-fx-text-fill: #2c3e50;");
        nutrient.setStyle("-fx-text-fill: #2c3e50;");

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button updateBtn = new Button("Update");
        updateBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 6 16;");
        updateBtn.setOnAction(e -> {
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
                loadSoilData();
            } catch (IOException ex) {
                if (resultArea != null) resultArea.setText("Error loading update form: " + ex.getMessage());
            }
        });

        Button detailsBtn = new Button("Details");
        detailsBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 6 16;");
        detailsBtn.setOnAction(e -> {
            if (resultArea != null) resultArea.setText(soilData.toString());
        });

        buttonBox.getChildren().addAll(detailsBtn, updateBtn);

        card.getChildren().addAll(topBox, title, subTitle, humidity, ph, nutrient, buttonBox);
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
                if (typeSolCombo != null) {
                    typeSolCombo.getItems().clear();
                    for (Map<String, Object> soilType : soilTypes) {
                        typeSolCombo.getItems().add((String) soilType.get("Name"));
                    }
                }
            });

        }
    }







    public void SoilStatisticsController() {
        this.soilDataCRUD = new SoilDataCRUD();
    }



    private void loadStatistics() {
        try {
            List<SoilData> soilDataList = soilDataCRUD.getSoilDataByCropId(currentCropId);
            updateHumidityChart(soilDataList);
            updatePhChart(soilDataList);
            updateNutrientChart(soilDataList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateHumidityChart(List<SoilData> soilDataList) {
        humidityChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Humidity");

        for (int i = 0; i < soilDataList.size(); i++) {
            SoilData data = soilDataList.get(i);
            series.getData().add(new XYChart.Data<>(
                    "Data " + (i + 1),
                    data.getHumidite()
            ));
        }

        humidityChart.getData().add(series);
    }

    private void updatePhChart(List<SoilData> soilDataList) {
        phChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("pH Level");

        for (int i = 0; i < soilDataList.size(); i++) {
            SoilData data = soilDataList.get(i);
            series.getData().add(new XYChart.Data<>(
                    "Data " + (i + 1),
                    data.getNiveau_ph()
            ));
        }

        phChart.getData().add(series);
    }

    private void updateNutrientChart(List<SoilData> soilDataList) {
        nutrientChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nutrient Level");

        for (int i = 0; i < soilDataList.size(); i++) {
            SoilData data = soilDataList.get(i);
            series.getData().add(new XYChart.Data<>(
                    "Data " + (i + 1),
                    data.getNiveau_nutriment()
            ));
        }

        nutrientChart.getData().add(series);
    }

    private void updateSoilTypeChart(List<SoilData> soilDataList) {

        Map<String, Long> soilTypeCounts = soilDataList.stream()
                .collect(Collectors.groupingBy(
                        SoilData::getType_sol,
                        Collectors.counting()
                ));

        soilTypeCounts.forEach((type, count) -> {

        });
    }
}
