package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import entite.SoilData;
import service.CropManageAuthService;
import service.CropManageSoilService;
import service.SoilDataCRUD;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SoilDataController {
    private final SoilDataCRUD soilDataCRUD = new SoilDataCRUD();
    private SoilData selectedSoilData;
    private int currentCropId;
    private boolean isFirstSoilData = true;

    private final CropManageAuthService apiAuthService = new CropManageAuthService();
    private final CropManageSoilService apiSoilService = new CropManageSoilService(apiAuthService);

    @FXML
    private FlowPane soilDataCardsPane;
    @FXML
    private TextField humiditeField;
    @FXML
    private TextField niveauPhField;
    @FXML
    private TextField niveauNutrimentField;
    @FXML
    private ComboBox<String> typeSolCombo;
    @FXML
    private TextArea resultArea;
    @FXML
    private BarChart<String, Number> humidityChart;
    @FXML
    private BarChart<String, Number> phChart;
    @FXML
    private BarChart<String, Number> nutrientChart;

    /**
     * Called by parent to set which crop we’re working with.
     */
    public void setCropId(int cropId) {
        this.currentCropId = cropId;
        loadAllData();
    }

    /**
     * Called by add/update forms to tell us if it’s the very first record.
     */
    public void setIsFirstSoilData(boolean isFirst) {
        this.isFirstSoilData = isFirst;
    }

    @FXML
    public void initialize() {
        // populate built-in soil types
        if (typeSolCombo != null) {
            typeSolCombo.getItems().addAll("Clayey", "Sandy", "Silty", "Humiferous", "Limestone");
        }
        // also fetch from API for dynamic list
        new Thread(this::populateSoilTypesFromApi).start();
    }

    /**
     * Loads both the cards *and* the stats.
     */
    private void loadAllData() {
        loadSoilData();
        loadStatistics();
    }

    /**
     * Fetch & display all soil-data cards.
     */
    private void loadSoilData() {
        try {
            List<SoilData> list = soilDataCRUD.getSoilDataByCropId(currentCropId);
            isFirstSoilData = list.isEmpty();
            displaySoilDataCards(list);
        } catch (SQLException e) {
            resultArea.setText("Error loading soil data: " + e.getMessage());
        }
    }

    private void displaySoilDataCards(List<SoilData> all) {
        soilDataCardsPane.getChildren().clear();
        for (SoilData sd : all) {
            soilDataCardsPane.getChildren().add(createCard(sd, all));
        }
    }

    private VBox createCard(SoilData sd, List<SoilData> all) {
        VBox card = new VBox(10);
        card.setPrefWidth(300);
        card.setPadding(new Insets(16));
        card.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:12;" +
                        "-fx-effect:dropshadow(three-pass-box, rgba(0,0,0,0.1),8,0,0,1);"
        );

        // header
        HBox top = new HBox();
        top.setAlignment(Pos.TOP_RIGHT);
        ImageView icon = new ImageView(
                new Image(getClass().getResource("/images/soildata.png").toExternalForm())
        );
        icon.setFitWidth(24);
        icon.setFitHeight(24);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button del = new Button("X");
        del.setStyle(
                "-fx-background-color:#e74c3c;" +
                        "-fx-text-fill:white;" +
                        "-fx-background-radius:20;"
        );
        del.setOnAction(evt -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete this soil data?", ButtonType.OK, ButtonType.CANCEL);
            a.setHeaderText("Confirm Deletion");
            a.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK) {
                    try {
                        soilDataCRUD.deleteSoilData(sd.getId());
                        loadAllData();
                        resultArea.setText("Deleted.");
                    } catch (SQLException ex) {
                        resultArea.setText("Delete failed: " + ex.getMessage());
                    }
                }
            });
        });
        top.getChildren().setAll(icon, spacer, del);

        // details
        Label title = new Label("Soil Data #" + sd.getId());
        title.setStyle("-fx-font-weight:bold;-fx-font-size:16;-fx-text-fill:#2c3e50;");
        Label type = new Label(sd.getType_sol());
        type.setStyle("-fx-text-fill:#666666;-fx-font-size:12;");
        Label h = new Label("Humidity: " + sd.getHumidite() + "%");
        Label p = new Label("pH Level: " + sd.getNiveau_ph());
        Label n = new Label("Nutrient: " + sd.getNiveau_nutriment());
        h.setStyle("-fx-text-fill:#2c3e50;");
        p.setStyle("-fx-text-fill:#2c3e50;");
        n.setStyle("-fx-text-fill:#2c3e50;");

        // actions
        HBox btns = new HBox(10);
        btns.setAlignment(Pos.CENTER);
        Button upd = new Button("Update");
        upd.setStyle(
                "-fx-background-color:#2e7d32;" +
                        "-fx-text-fill:white;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-radius:20;" +
                        "-fx-padding:6 16;"
        );
        upd.setOnAction(evt -> openUpdateForm(sd));

        btns.getChildren().setAll( upd);

        card.getChildren().addAll(top, title, type, h, p, n, btns);

        // locked indicator
        if (all.indexOf(sd) > 0) {
            Label lock = new Label("Soil type locked to first record");
            lock.setStyle(
                    "-fx-text-fill:#e74c3c;" +
                            "-fx-font-size:10;" +
                            "-fx-font-style:italic;"
            );
            card.getChildren().add(lock);
        }

        return card;
    }

    /**
     * Show the add-form.
     */

    @FXML
    private void showAddSoilDataForm() {
        try {
            // Check if this is the first record
            boolean isFirstRecord = isFirstSoilDataForCrop();
            openForm("add_soil_data.fxml", "Add New Soil Data", true, isFirstRecord);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Show the update-form for one record.
     */
    private void openUpdateForm(SoilData sd) {
        this.selectedSoilData = sd;

        // Check if this is the first record
        try {
            List<SoilData> allRecords = soilDataCRUD.getSoilDataByCropId(currentCropId);
            boolean isFirstRecord = !allRecords.isEmpty() && allRecords.get(0).getId() == sd.getId();

            openForm("update_soil_data.fxml", "Update Soil Data", false, isFirstRecord);
        } catch (SQLException e) {
            resultArea.setText("Error checking records: " + e.getMessage());
        }
    }

    private void openForm(String fxml, String title, boolean isAdd, boolean allowSoilTypeChange) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent root = loader.load();
            SoilDataController ctrl = loader.getController();
            ctrl.currentCropId = this.currentCropId;

            if (!isAdd) {
                ctrl.selectedSoilData = this.selectedSoilData;
                ctrl.populateFields(this.selectedSoilData, allowSoilTypeChange);
            } else {
                boolean isFirstRecord = isFirstSoilDataForCrop();
                ctrl.setIsFirstSoilData(isFirstRecord);
                if (!isFirstRecord) {
                    // For add form, set soil type to first record's type
                    List<SoilData> existingData = soilDataCRUD.getSoilDataByCropId(currentCropId);
                    ctrl.typeSolCombo.setValue(existingData.get(0).getType_sol());
                    ctrl.typeSolCombo.setDisable(true);
                    ctrl.typeSolCombo.setStyle("-fx-opacity: 0.7; -fx-background-color: #f0f0f0;");
                }
            }

            Stage st = new Stage();
            st.setTitle(title);
            st.setScene(new Scene(root));
            st.showAndWait();
            loadAllData();
        } catch (IOException | SQLException e) {
            resultArea.setText("Failed to open form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Called by Save button in add & update dialogs.
     */
    @FXML
    private void createSoilData() {
        if (!validateInputs()) return;

        try {
            String today = java.time.LocalDate.now().toString();
            List<SoilData> existingData = soilDataCRUD.getSoilDataByCropId(currentCropId);
            String chosenType;

            if (selectedSoilData != null) {
                // For updates, check if this is the first record
                boolean isFirstRecord = !existingData.isEmpty() &&
                        existingData.get(0).getId() == selectedSoilData.getId();

                if (isFirstRecord) {
                    // First record - use selected type and update all others
                    chosenType = typeSolCombo.getValue();
                    updateAllSoilTypes(chosenType);
                } else {
                    // Subsequent records - keep original type
                    chosenType = selectedSoilData.getType_sol();
                }
            } else {
                // For new records
                boolean isFirstRecord = existingData.isEmpty();
                chosenType = isFirstRecord ? typeSolCombo.getValue() : existingData.get(0).getType_sol();
            }

            SoilData sd = new SoilData(
                    selectedSoilData != null ? selectedSoilData.getId() : 0,
                    Double.parseDouble(humiditeField.getText()),
                    Double.parseDouble(niveauPhField.getText()),
                    Double.parseDouble(niveauNutrimentField.getText()),
                    chosenType,
                    currentCropId,
                    today
            );

            if (selectedSoilData != null) {
                soilDataCRUD.updateSoilData(sd);
                resultArea.setText("Updated!");
            } else {
                soilDataCRUD.createSoilData(sd);
                resultArea.setText("Created!");
            }

            ((Stage) humiditeField.getScene().getWindow()).close();
            loadAllData();
        } catch (SQLException e) {
            resultArea.setText("Save failed: " + e.getMessage());
        }
    }

    private void updateAllSoilTypes(String newSoilType) throws SQLException {
        List<SoilData> allRecords = soilDataCRUD.getSoilDataByCropId(currentCropId);
        if (allRecords.size() > 1) { // Only update if there are other records
            for (int i = 1; i < allRecords.size(); i++) {
                SoilData record = allRecords.get(i);
                // Create updated record with new soil type but keeping other values
                SoilData updatedRecord = new SoilData(
                        record.getId(),
                        record.getHumidite(),
                        record.getNiveau_ph(),
                        record.getNiveau_nutriment(),
                        newSoilType,  // Updated soil type
                        record.getCrop_id(),
                        record.getDate()
                );
                soilDataCRUD.updateSoilData(updatedRecord);
            }
        }
    }


    private void populateFields(SoilData sd, boolean allowSoilTypeChange) {
        humiditeField.setText(String.valueOf(sd.getHumidite()));
        niveauPhField.setText(String.valueOf(sd.getNiveau_ph()));
        niveauNutrimentField.setText(String.valueOf(sd.getNiveau_nutriment()));
        typeSolCombo.setValue(sd.getType_sol());

        // Only enable soil type editing if allowed (for first record)
        typeSolCombo.setDisable(!allowSoilTypeChange);

        // Visual styling
        if (!allowSoilTypeChange) {
            typeSolCombo.setStyle("-fx-opacity: 0.7; -fx-background-color: #f0f0f0;");
            typeSolCombo.setTooltip(new Tooltip("Soil type is locked to the first record's type"));
        } else {
            typeSolCombo.setStyle("");
            typeSolCombo.setTooltip(null);
        }
    }

    private boolean validateInputs() {
        try {
            // Check required fields
            if (humiditeField.getText().isEmpty()
                    || niveauPhField.getText().isEmpty()
                    || niveauNutrimentField.getText().isEmpty()) {
                resultArea.setText("All fields required");
                return false;
            }

            // Check numeric ranges
            double h = Double.parseDouble(humiditeField.getText());
            double p = Double.parseDouble(niveauPhField.getText());
            double n = Double.parseDouble(niveauNutrimentField.getText());
            if (h < 0 || h > 100 || p < 0 || p > 14 || n < 0) {
                resultArea.setText("Values out of range");
                return false;
            }

            // Only validate soil type if it's editable (first record or new record when no records exist)
            if (typeSolCombo.isDisabled() == false && typeSolCombo.getValue() == null) {
                resultArea.setText("Pick a soil type");
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            resultArea.setText("Enter valid numbers");
            return false;
        }
    }

    private boolean isFirstSoilDataForCrop() {
        try {
            return soilDataCRUD.getSoilDataByCropId(currentCropId).isEmpty();
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Refresh the three bar-charts.
     */
    private void loadStatistics() {
        try {
            List<SoilData> list = soilDataCRUD.getSoilDataByCropId(currentCropId);
            updateChart(humidityChart, list, "Humidity", SoilData::getHumidite);
            updateChart(phChart, list, "pH Level", SoilData::getNiveau_ph);
            updateChart(nutrientChart, list, "Nutrient", SoilData::getNiveau_nutriment);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private <T extends Number> void updateChart(BarChart<String, Number> chart,
                                                List<SoilData> data,
                                                String name,
                                                java.util.function.Function<SoilData, T> extractor) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(name);
        for (int i = 0; i < data.size(); i++) {
            series.getData().add(new XYChart.Data<>(
                    "D" + (i + 1),
                    extractor.apply(data.get(i))
            ));
        }
        chart.getData().add(series);
    }

    @FXML
    private void goBackToCrops() throws IOException {
        Parent root = FXMLLoader.load(
                getClass().getClassLoader().getResource("crop.fxml")
        );
        Stage st = (Stage) soilDataCardsPane.getScene().getWindow();
        st.setScene(new Scene(root));
        st.setTitle("Crop Management");
    }
    @FXML
    private void closeWindow() {
        // Get the stage from any control in the scene
        Stage stage = (Stage) humiditeField.getScene().getWindow();
        stage.close();
    }
    private void populateSoilTypesFromApi() {
        List<Map<String, Object>> types = apiSoilService.getSoilTypes();
        if (types != null && typeSolCombo != null) {
            Platform.runLater(() -> {
                typeSolCombo.getItems().clear();
                for (Map<String, Object> m : types) {
                    typeSolCombo.getItems().add((String) m.get("Name"));
                }
            });
        }
    }




    private void updateHumidityChart(List<SoilData> list) {
        humidityChart.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName("Humidity");
        for (int i = 0; i < list.size(); i++)
            s.getData().add(new XYChart.Data<>("D" + (i + 1), list.get(i).getHumidite()));
        humidityChart.getData().add(s);
    }

    private void updatePhChart(List<SoilData> list) {
        phChart.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName("pH");
        for (int i = 0; i < list.size(); i++)
            s.getData().add(new XYChart.Data<>("D" + (i + 1), list.get(i).getNiveau_ph()));
        phChart.getData().add(s);
    }

    private void updateNutrientChart(List<SoilData> list) {
        nutrientChart.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName("Nutrient");
        for (int i = 0; i < list.size(); i++)
            s.getData().add(new XYChart.Data<>("D" + (i + 1), list.get(i).getNiveau_nutriment()));
        nutrientChart.getData().add(s);
    }

} // (empty) stub for future soil-type pie charts


