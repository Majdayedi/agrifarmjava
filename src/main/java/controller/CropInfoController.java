package controller;

import entite.CropInfo;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import service.CropInfoService;
import javafx.concurrent.Task;
import javafx.beans.property.SimpleStringProperty;

import java.util.List;

public class CropInfoController {
    @FXML private TableView<CropInfo> cropInfoTable;
    @FXML private TableColumn<CropInfo, String> nameColumn;
    @FXML private TableColumn<CropInfo, String> scientificNameColumn;
    @FXML private TableColumn<CropInfo, String> familyColumn;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private VBox detailsPane;
    @FXML private ImageView cropImageView;
    @FXML private TextArea descriptionArea;

    private final CropInfoService cropInfoService;

    public CropInfoController() {
        this.cropInfoService = new CropInfoService();
    }

    @FXML
    public void initialize() {
        setupTable();
        loadCropInfo();

        // Add selection listener for details
        cropInfoTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> showCropDetails(newValue)
        );
    }

    private void setupTable() {
        nameColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getName()));
        scientificNameColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getScientificName()));
        familyColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getFamily()));
    }

    private void loadCropInfo() {
        loadingIndicator.setVisible(true);

        Task<List<CropInfo>> task = new Task<>() {
            @Override
            protected List<CropInfo> call() throws Exception {
                return cropInfoService.getCropsForTunisia();
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                cropInfoTable.setItems(FXCollections.observableArrayList(task.getValue()));
                loadingIndicator.setVisible(false);
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                showError("Failed to load crop information: " + 
                    task.getException().getMessage());
            });
        });

        new Thread(task).start();
    }

    private void showCropDetails(CropInfo crop) {
        if (crop == null) {
            detailsPane.setVisible(false);
            return;
        }

        detailsPane.setVisible(true);
        descriptionArea.setText(crop.getDescription());

        if (crop.getImageUrl() != null && !crop.getImageUrl().isEmpty()) {
            try {
                Image image = new Image(crop.getImageUrl());
                cropImageView.setImage(image);
            } catch (Exception e) {
                cropImageView.setImage(null);
            }
        } else {
            cropImageView.setImage(null);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error Loading Crop Information");
        alert.setContentText(message);
        alert.showAndWait();
    }
}

