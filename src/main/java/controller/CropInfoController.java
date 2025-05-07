package controller;

import entite.CropInfo;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import service.CropInfoService;
import javafx.concurrent.Task;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class CropInfoController {
    @FXML private TableView<CropInfo> cropInfoTable;
    @FXML private TableColumn<CropInfo, String> nameColumn;
    @FXML private TableColumn<CropInfo, String> scientificNameColumn;
    @FXML private TableColumn<CropInfo, String> familyColumn;
    @FXML private TableColumn<CropInfo, String> groupColumn;
    @FXML private TableColumn<CropInfo, Void> imageColumn;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private VBox detailsPane;
    @FXML private ImageView cropImageView;
    @FXML private TextArea descriptionArea;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> familyFilterCombo;

    private final CropInfoService cropInfoService;
    private ObservableList<CropInfo> allCrops;
    private FilteredList<CropInfo> filteredCrops;

    public CropInfoController() {
        this.cropInfoService = new CropInfoService();
    }

    @FXML
    public void initialize() {
        System.out.println("Initializing CropInfoController...");
        System.out.println("nameColumn: " + nameColumn);
        System.out.println("scientificNameColumn: " + scientificNameColumn);
        System.out.println("familyColumn: " + familyColumn);
        System.out.println("groupColumn: " + groupColumn); // This will show null if not found
        System.out.println("imageColumn: " + imageColumn);
        setupTable();
        setupSearchAndFilter();
        loadCropInfo();

        // Add selection listener for details
        cropInfoTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showCropDetails(newValue)
        );
    }

    private void setupTable() {
        // Set up cell value factories
        nameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getName()));
        scientificNameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getScientificName()));
        familyColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFamily()));
        groupColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getGroupName()));

        // Set up image column
        imageColumn.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitHeight(40);
                imageView.setFitWidth(40);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    CropInfo crop = getTableView().getItems().get(getIndex());
                    if (crop.getImageUrl() != null && !crop.getImageUrl().isEmpty()) {
                        try {
                            Image image = new Image(crop.getImageUrl(), true);
                            imageView.setImage(image);
                        } catch (Exception e) {
                            imageView.setImage(null);
                        }
                    } else {
                        imageView.setImage(null);
                    }
                    setGraphic(imageView);
                }
            }
        });
    }

    private void setupSearchAndFilter() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (filteredCrops != null) {
                filteredCrops.setPredicate(crop -> {
                    if (newVal == null || newVal.isEmpty()) {
                        return true;
                    }

                    String lowerCaseFilter = newVal.toLowerCase();
                    return crop.getName().toLowerCase().contains(lowerCaseFilter) ||
                            crop.getScientificName().toLowerCase().contains(lowerCaseFilter) ||
                            (crop.getAdditionalInfo() != null &&
                                    crop.getAdditionalInfo().toLowerCase().contains(lowerCaseFilter));
                });
            }
        });

        familyFilterCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (filteredCrops != null) {
                filteredCrops.setPredicate(crop -> {
                    if (newVal == null || newVal.isEmpty() || newVal.equals("All Families")) {
                        return true;
                    }
                    return crop.getFamily().equals(newVal);
                });
            }
        });
    }

    private void loadCropInfo() {
        loadingIndicator.setVisible(true);
        detailsPane.setVisible(false);

        Task<List<CropInfo>> task = new Task<>() {
            @Override
            protected List<CropInfo> call() throws Exception {
                return cropInfoService.getCropsForTunisia();
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                allCrops = FXCollections.observableArrayList(task.getValue());
                filteredCrops = new FilteredList<>(allCrops);
                cropInfoTable.setItems(filteredCrops);

                // Populate family filter combo
                List<String> families = allCrops.stream()
                        .map(CropInfo::getFamily)
                        .distinct()
                        .sorted()
                        .toList();
                familyFilterCombo.getItems().clear();
                familyFilterCombo.getItems().add("All Families");
                familyFilterCombo.getItems().addAll(families);
                familyFilterCombo.getSelectionModel().selectFirst();

                loadingIndicator.setVisible(false);
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                showError("Failed to load crop information: " +
                        task.getException().getMessage());

                // Add retry button
                Button retryButton = new Button("Retry");
                retryButton.setOnAction(event -> loadCropInfo());
                detailsPane.getChildren().add(retryButton);
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

        // Build detailed description
        StringBuilder description = new StringBuilder();
        description.append("Name: ").append(crop.getName()).append("\n\n");
        description.append("Scientific Name: ").append(crop.getScientificName()).append("\n\n");
        description.append("Family: ").append(crop.getFamily()).append("\n\n");
        description.append("Group: ").append(crop.getGroupName()).append("\n\n");
        if (crop.getAdditionalInfo() != null && !crop.getAdditionalInfo().isEmpty()) {
            description.append("Additional Information:\n").append(crop.getAdditionalInfo());
        }

        descriptionArea.setText(description.toString());

        // Load image
        if (crop.getImageUrl() != null && !crop.getImageUrl().isEmpty()) {
            try {
                Image image = new Image(crop.getImageUrl(), true); // true for background loading
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