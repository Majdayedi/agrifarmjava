package controller;

import entite.CropCalendarEntry;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import service.CropCalendarService;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressIndicator;
import javafx.concurrent.Task;
import javafx.beans.property.SimpleStringProperty;

import java.net.URL;
import java.util.ResourceBundle;

public class CropCalendarController {
    @FXML private TableView<CropCalendarEntry> calendarTable;
    @FXML private TableColumn<CropCalendarEntry, String> cropNameColumn;
    @FXML private TableColumn<CropCalendarEntry, String> regionColumn;
    @FXML private TableColumn<CropCalendarEntry, String> additionalInfoColumn;
    @FXML private TableColumn<CropCalendarEntry, String> plantingDateColumn;
    @FXML private TableColumn<CropCalendarEntry, String> harvestDateColumn;
    @FXML private TableColumn<CropCalendarEntry, String> growingPeriodColumn;
    @FXML private ProgressIndicator loadingIndicator;

    private final CropCalendarService calendarService;

    public CropCalendarController() {
        this.calendarService = new CropCalendarService();
    }

    @FXML
    public void initialize() {
        calendarTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        setupTable();
        loadCalendarData();
    }

    private void setupTable() {
        // Set up cell value factories for all columns
        cropNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCropName()));

        regionColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRegion()));

        additionalInfoColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAdditionalInfo()));

        // Fix: Add missing planting date column cell value factory
        plantingDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPlantingDate()));

        harvestDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getHarvestDate()));

        // Growing period column with formatted output
        growingPeriodColumn.setCellValueFactory(cellData -> {
            int days = cellData.getValue().getGrowingPeriod();
            return new SimpleStringProperty(days > 0 ? days + " days" : "Unknown");
        });
    }

    private void loadCalendarData() {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(true);
        }

        Task<ObservableList<CropCalendarEntry>> task = new Task<>() {
            @Override
            protected ObservableList<CropCalendarEntry> call() throws Exception {
                return FXCollections.observableArrayList(calendarService.getCropsForTunisia());
            }
        };

        task.setOnSucceeded(e -> {
            calendarTable.setItems(task.getValue());
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(false);
            }
            
            // Debug output to verify data loading
            task.getValue().forEach(entry -> {
                System.out.println(String.format(
                    "Loaded entry: %s%n" +
                    "Planting: %s%n" +
                    "Harvest: %s%n" +
                    "Growing Period: %d days",
                    entry.getCropName(),
                    entry.getPlantingDate(),
                    entry.getHarvestDate(),
                    entry.getGrowingPeriod()
                ));
            });
        });

        task.setOnFailed(e -> {
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(false);
            }
            showError("Failed to load crop calendar data: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Failed to load crop calendar");
        alert.setContentText(message);
        alert.showAndWait();
    }
}