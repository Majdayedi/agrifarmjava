package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.CropCRUD;
import service.CropCalendarService;
import service.SoilDataCRUD;
import entite.Crop;
import entite.CropCalendarEntry;
import entite.SoilData;

// QR Code imports
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.io.IOException;
import javafx.util.Pair;

public class CropController {
    // Class to store crop calendar data for validation
    private static class CropPeriods {
        private final String cropName;
        private final String plantingPeriod;
        private final String harvestPeriod;
        
        public CropPeriods(String cropName, String plantingPeriod, String harvestPeriod) {
            this.cropName = cropName;
            this.plantingPeriod = plantingPeriod;
            this.harvestPeriod = harvestPeriod;
        }
    }
    private final CropCRUD cropCRUD = new CropCRUD();
    private final CropCalendarService cropCalendarService = new CropCalendarService();
    private Crop selectedCrop;
    private CropPeriods selectedCropPeriods;
    private final ObservableList<Crop> cropData = FXCollections.observableArrayList();
    private final List<String> CROP_METHODS = Arrays.asList(
        "By Hand", "By Machine"
    );

    @FXML private TextField cropEventField;
    @FXML private ComboBox<String> typeCropCombo;
    @FXML private ComboBox<String> methodCropCombo;
    @FXML private DatePicker plantationDatePicker;
    @FXML private Spinner<Integer> plantationHourSpinner;
    @FXML private Spinner<Integer> plantationMinuteSpinner;
    @FXML private DatePicker cropDatePicker;
    @FXML private Spinner<Integer> cropHourSpinner;
    @FXML private Spinner<Integer> cropMinuteSpinner;
    @FXML private TextArea resultArea;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterTypeCombo;
    @FXML private FlowPane cropCardsPane;
    @FXML private TableView<Crop> cropsTable;
    @FXML private TableColumn<Crop, Integer> idColumn;
    @FXML private TableColumn<Crop, String> eventColumn;
    @FXML private TableColumn<Crop, String> typeColumn;
    @FXML private TableColumn<Crop, String> methodColumn;
    @FXML private TableColumn<Crop, String> plantationDateColumn;
    @FXML private TableColumn<Crop, String> cropDateColumn;
    @FXML private TableColumn<Crop, Void> actionsColumn;

    private boolean isUpdateMode = false;
    @FXML private ScrollPane cropScrollPane;


    @FXML
    public void initialize() {
        System.out.println("Initializing CropController...");
        try {
            // Initialize combo boxes
            if (typeCropCombo != null) {
                typeCropCombo.setPromptText("Loading crop types...");
                typeCropCombo.setDisable(true);
                
                // Add selection listener
                /*typeCropCombo.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            loadCropPeriods(newValue);
                        }
                    }
                );*/
                
                // Load crop types from API when initializing
                loadCropTypesFromAPI();
            }
            
            if (methodCropCombo != null) {
                methodCropCombo.getItems().clear();
                methodCropCombo.getItems().addAll(CROP_METHODS);
            }
            
            if (filterTypeCombo != null) {
                filterTypeCombo.getItems().clear();
                filterTypeCombo.getItems().add("Tous");
                // We'll update this with API data later when loadCropTypesFromAPI completes
                filterTypeCombo.getSelectionModel().selectFirst();
                filterTypeCombo.setOnAction(event -> applyFilter());
            }

            // Initialize spinners
            if (plantationHourSpinner != null) {
                SpinnerValueFactory<Integer> plantationHourFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0);
                plantationHourSpinner.setValueFactory(plantationHourFactory);
            }
            if (plantationMinuteSpinner != null) {
                SpinnerValueFactory<Integer> plantationMinuteFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
                plantationMinuteSpinner.setValueFactory(plantationMinuteFactory);
            }
            if (cropHourSpinner != null) {
                SpinnerValueFactory<Integer> cropHourFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0);
                cropHourSpinner.setValueFactory(cropHourFactory);
            }
            if (cropMinuteSpinner != null) {
                SpinnerValueFactory<Integer> cropMinuteFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
                cropMinuteSpinner.setValueFactory(cropMinuteFactory);
            }

            // Set up search field listener
            if (searchField != null) {
                searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.isEmpty()) {
                        handleSearchButtonAction();
                    } else {
                        loadAllCrops();
                    }
                });
            }

            // Only load crops if we're in the main view (where cropCardsPane exists)
            if (cropCardsPane != null || cropsTable != null) {
                loadAllCrops();
            }

            System.out.println("CropController initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing CropController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load crop types from API
    private void loadCropTypesFromAPI() {
        // Create a task for the API call
        Task<List<CropCalendarEntry>> task = new Task<>() {
            @Override
            protected List<CropCalendarEntry> call() throws Exception {
                return cropCalendarService.getCropsForTunisia();
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                List<CropCalendarEntry> allCrops = task.getValue();
                List<String> cropTypes = allCrops.stream()
                    .map(CropCalendarEntry::getCropName)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
                
                // Update the crop types combo box
                if (typeCropCombo != null) {
                    typeCropCombo.getItems().clear();
                    typeCropCombo.getItems().addAll(cropTypes);
                    typeCropCombo.setDisable(false);
                    typeCropCombo.setPromptText("Select crop type");
                    
                    // Show message if no crops found
                    if (cropTypes.isEmpty()) {
                        typeCropCombo.setPromptText("No crops available");
                    }
                }
                
                // Also update filter combo box
                if (filterTypeCombo != null) {
                    // Keep the "Tous" option at index 0
                    String firstItem = filterTypeCombo.getItems().get(0);
                    filterTypeCombo.getItems().clear();
                    filterTypeCombo.getItems().add(firstItem);
                    filterTypeCombo.getItems().addAll(cropTypes);
                    filterTypeCombo.getSelectionModel().selectFirst();
                }
            });
        });
        
        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                if (typeCropCombo != null) {
                    typeCropCombo.setDisable(false);
                    typeCropCombo.setPromptText("Error loading crops");
                }
                Throwable exception = task.getException();
                String errorMessage = "Failed to fetch crop data: " + 
                    (exception != null ? exception.getMessage() : "Unknown error");
                
                System.err.println(errorMessage);
                exception.printStackTrace();
                
                if (resultArea != null) {
                    resultArea.setText(errorMessage);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Failed to Load Crop Data");
                    alert.setContentText(errorMessage);
                    alert.showAndWait();
                }
            });
        });
        
        // Start the task in a new thread
        new Thread(task).start();
    }

    // Method to load crop periods when a crop is selected
    private void loadCropPeriods(String selectedCrop) {
        Task<List<CropCalendarEntry>> task = new Task<>() {
            @Override
            protected List<CropCalendarEntry> call() throws Exception {
                return cropCalendarService.getCropsForTunisia();
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                List<CropCalendarEntry> allCrops = task.getValue();
                Optional<CropCalendarEntry> matchingCrop = allCrops.stream()
                    .filter(crop -> crop.getCropName().equals(selectedCrop))
                    .findFirst();
                
                matchingCrop.ifPresent(crop -> {
                    selectedCropPeriods = new CropPeriods(
                        crop.getCropName(),
                        crop.getPlantingDate(),
                        crop.getHarvestDate()
                    );
                    System.out.println("Loaded periods for " + selectedCrop + 
                                      ": Planting=" + crop.getPlantingDate() + 
                                      ", Harvest=" + crop.getHarvestDate());
                });
            });
        });
        
        task.setOnFailed(event -> {
            System.err.println("Failed to load crop periods: " + task.getException().getMessage());
            task.getException().printStackTrace();
        });
        
        new Thread(task).start();
    }

    // Parse date range string (DD/MM - DD/MM) to LocalDate objects
    private Pair<LocalDate, LocalDate> parseDateRange(String dateRange) {
        String[] parts = dateRange.split(" - ");
        String startStr = parts[0];
        String endStr = parts[1];
        
        // Get current year
        int currentYear = LocalDate.now().getYear();
        
        // Parse start date
        String[] startParts = startStr.split("/");
        LocalDate startDate = LocalDate.of(currentYear, 
            Integer.parseInt(startParts[1]), Integer.parseInt(startParts[0]));
        
        // Parse end date
        String[] endParts = endStr.split("/");
        LocalDate endDate = LocalDate.of(currentYear, 
            Integer.parseInt(endParts[1]), Integer.parseInt(endParts[0]));
        
        // Handle year wrap (e.g., if planting period is Nov-Feb)
        if (endDate.isBefore(startDate)) {
            endDate = endDate.plusYears(1);
        }
        
        return new Pair<>(startDate, endDate);
    }

    // Helper method to check if a date falls within a range
    private boolean isDateInRange(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null || start == null || end == null) {
            return false;
        }
        
        // Adjust the date's year to match the range's year for proper comparison
        LocalDate adjustedDate = LocalDate.of(
            start.getYear(),
            date.getMonthValue(),
            date.getDayOfMonth()
        );
        
        // If the date is before the start date, try next year
        if (adjustedDate.isBefore(start)) {
            adjustedDate = adjustedDate.plusYears(1);
        }
        
        return !adjustedDate.isBefore(start) && !adjustedDate.isAfter(end);
    }

    @FXML
    public void showAddCropForm() {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/add_crop.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set it up
            CropController controller = loader.getController();
            controller.setUpdateMode(false);
            
            // Create and set up the stage
            Stage stage = new Stage();
            stage.setTitle("Add New Crop");
            stage.setScene(new Scene(root));
            
            // Set the main controller as user data
            stage.getScene().setUserData(this);
            
            // Set the owner of the new window
            if (cropEventField != null && cropEventField.getScene() != null) {
                stage.initOwner(cropEventField.getScene().getWindow());
            } else if (cropCardsPane != null && cropCardsPane.getScene() != null) {
                stage.initOwner(cropCardsPane.getScene().getWindow());
            }
            
            // Show the stage and wait for it to close
            stage.showAndWait();
            
            // Refresh the main view after the add window closes
            loadAllCrops();
        } catch (IOException e) {
            e.printStackTrace();
            if (resultArea != null) {
                resultArea.setText("Error loading add crop form: " + e.getMessage());
            }
        }
    }

    @FXML
    private void showCropList() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/crop.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Crops");
            stage.setScene(new Scene(root));
            stage.show();
            
            // Close the current window
            ((Stage) cropEventField.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
            resultArea.setText("Error loading crop list: " + e.getMessage());
        }
    }

    @FXML
    private void createCrop() {
        try {
            if (validateInputs()) {
                String plantationTime = String.format("%02d:%02d", 
                    plantationHourSpinner.getValue(), 
                    plantationMinuteSpinner.getValue());
                String cropTime = String.format("%02d:%02d", 
                    cropHourSpinner.getValue(), 
                    cropMinuteSpinner.getValue());

                Crop crop = new Crop(
                    isUpdateMode ? selectedCrop.getId() : 0,
                    cropEventField.getText(),
                    typeCropCombo.getValue(),
                    methodCropCombo.getValue(),
                    plantationDatePicker.getValue().format(DateTimeFormatter.ISO_DATE),
                    plantationTime,
                    cropDatePicker.getValue().format(DateTimeFormatter.ISO_DATE),
                    cropTime
                );

                // Save the crop
                cropCRUD.createCrop(crop);
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Crop created successfully!");
                alert.showAndWait();

                // Get the current window
                Stage currentStage = (Stage) cropEventField.getScene().getWindow();
                
                // Close the current window
                currentStage.close();
                
                // Get the owner window and refresh it
                if (currentStage.getOwner() != null) {
                    Scene ownerScene = currentStage.getOwner().getScene();
                    if (ownerScene != null && ownerScene.getUserData() instanceof CropController) {
                        CropController mainController = (CropController) ownerScene.getUserData();
                        mainController.loadAllCrops();
                    }
                }
            }
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error creating crop");
            alert.setContentText("An error occurred while saving the crop: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    private void updateCrop() {
        try {
            if (selectedCrop != null && validateInputs()) {
                String plantationTime = String.format("%02d:%02d", 
                    plantationHourSpinner.getValue(), 
                    plantationMinuteSpinner.getValue());
                String cropTime = String.format("%02d:%02d", 
                    cropHourSpinner.getValue(), 
                    cropMinuteSpinner.getValue());

                Crop updatedCrop = new Crop(
                    selectedCrop.getId(),
                    cropEventField.getText(),
                    typeCropCombo.getValue(),
                    methodCropCombo.getValue(),
                    plantationDatePicker.getValue().format(DateTimeFormatter.ISO_DATE),
                    plantationTime,
                    cropDatePicker.getValue().format(DateTimeFormatter.ISO_DATE),
                    cropTime
                );

                cropCRUD.updateCrop(updatedCrop);
                
                // Close the update window and refresh the list
                Stage stage = (Stage) cropEventField.getScene().getWindow();
                stage.close();
                loadAllCrops();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resultArea.setText("Error updating crop: " + e.getMessage());
        }
    }

    @FXML
    private void deleteCrop() {
        System.out.println("Deleting crop...");
        try {
            if (selectedCrop != null) {
                cropCRUD.deleteCrop(selectedCrop.getId());
                resultArea.setText("Crop deleted successfully!");
                clearCropFields();
                loadAllCrops();
                selectedCrop = null;
            } else {
                resultArea.setText("Please select a crop to delete");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting crop: " + e.getMessage());
            e.printStackTrace();
            resultArea.setText("Error deleting crop: " + e.getMessage());
        }
    }

    @FXML
    private void clearCropFields() {
        cropEventField.clear();
        typeCropCombo.getSelectionModel().clearSelection();
        methodCropCombo.getSelectionModel().clearSelection();
        plantationDatePicker.setValue(null);
        plantationHourSpinner.getValueFactory().setValue(0);
        plantationMinuteSpinner.getValueFactory().setValue(0);
        cropDatePicker.setValue(null);
        cropHourSpinner.getValueFactory().setValue(0);
        cropMinuteSpinner.getValueFactory().setValue(0);
    }

    @FXML
    private void handleSearchButtonAction() {
        String searchText = searchField.getText().trim();
        if (!searchText.isEmpty()) {
            try {
                List<Crop> searchResults = cropCRUD.searchCrops(searchText);
                cropData.clear();
                cropData.addAll(searchResults);
                displayCropCards();
            } catch (SQLException e) {
                System.err.println("Error searching crops: " + e.getMessage());
                e.printStackTrace();
                resultArea.setText("Error searching crops: " + e.getMessage());
            }
        } else {
            loadAllCrops();
        }
    }

    @FXML
    private void applyFilter() {
        String selectedType = filterTypeCombo.getValue();
        if (selectedType != null && !selectedType.equals("Tous")) {
            try {
                List<Crop> filteredCrops = cropCRUD.getCropsByType(selectedType);
                if (cropsTable != null) {
                    // If we're in admin view, update the table
                    ObservableList<Crop> data = FXCollections.observableArrayList(filteredCrops);
                    cropsTable.setItems(data);
                } else {
                    // If we're in card view, update the cards
                    cropData.clear();
                    cropData.addAll(filteredCrops);
                    displayCropCards();
                }
            } catch (SQLException e) {
                resultArea.setText("Error filtering crops: " + e.getMessage());
            }
        } else {
            // If "Tous" is selected or no type is selected, show all crops
            loadAllCrops();
        }
    }

    public void loadAllCrops() {
        try {
            List<Crop> allCrops = cropCRUD.getAllCrops();
            if (cropsTable != null) {
                // If we're in admin view, update the table
                ObservableList<Crop> data = FXCollections.observableArrayList(allCrops);
                cropsTable.setItems(data);
            } else if (cropCardsPane != null) {
                // If we're in card view, update the cards
                cropData.clear();
                cropData.addAll(allCrops);
                displayCropCards();
            }
        } catch (SQLException e) {
            if (resultArea != null) {
                resultArea.setText("Error loading crops: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    private void displayCropCards() {
        cropCardsPane.getChildren().clear();
        for (Crop crop : cropData) {
            VBox card = createCropCard(crop);
            cropCardsPane.getChildren().add(card);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
//  Creates a single crop card with the same look‑and‑feel as your soil cards
// ──────────────────────────────────────────────────────────────────────────
    /** Builds a single crop card that visually matches the Soil‑Data card style */
    private VBox createCropCard(Crop crop) {
        // ── Card container ────────────────────────────────────────────────────────
        VBox card = new VBox(10);                 // 10 px gap between vertical children
        card.setPadding(new Insets(16));          // same padding as SoilData cards
        card.setPrefWidth(300);                   // fixed width so cards align nicely
        card.setStyle("""
        -fx-background-color: white;
        -fx-background-radius: 12;
        -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 1);
    """);

        /* ── Top row : icon + spacer + delete button ──────────────────────────── */
        HBox topBox = new HBox();
        topBox.setAlignment(Pos.TOP_RIGHT);

        ImageView icon = new ImageView(
                getClass().getResource("/images/me.png").toExternalForm()); // <-- put your own icon here
        icon.setFitWidth(28);
        icon.setFitHeight(28);
        icon.setPreserveRatio(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("X");
        deleteBtn.setStyle("""
        -fx-background-color: #e74c3c;
        -fx-text-fill: white;
        -fx-background-radius: 20;
        -fx-min-width: 25; -fx-min-height: 25;
    """);
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete this crop?", ButtonType.OK, ButtonType.CANCEL);
            confirm.setHeaderText("Confirm deletion");
            confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
                try {
                    cropCRUD.deleteCrop(crop.getId());
                    loadAllCrops();
                    if (resultArea != null) resultArea.setText("Crop deleted!");
                } catch (SQLException ex) {
                    if (resultArea != null) resultArea.setText("Delete error: " + ex.getMessage());
                }
            });
        });

        topBox.getChildren().addAll(icon, spacer, deleteBtn);

        /* ── Title + subtitle ─────────────────────────────────────────────────── */
        Label title  = new Label(crop.getTypeCrop());   // Event as headline
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #2c3e50;");

        Label sub    = new Label("Event:       " + crop.getCropEvent());    // Type as sub‑title
        sub.setStyle("-fx-text-fill: #666666; -fx-font-size: 12;");
        /* ── Details section ──────────────────────────────────────────────────── */
        String detailStyle = "-fx-text-fill: #2c3e50;";
        Label event    = new Label("Event:       " + crop.getCropEvent());          event.setStyle(detailStyle);

        /* ── Action buttons row ───────────────────────────────────────────────── */
        HBox btnRow = new HBox(10);
        btnRow.setAlignment(Pos.CENTER);
        btnRow.setPadding(new Insets(5,0,0,0));

        Button updateBtn = new Button("Update");
        updateBtn.setStyle("-fx-background-color:#2e7d32;-fx-text-fill:white;-fx-background-radius:20;");
        updateBtn.setOnAction(ev -> openUpdateForm(crop));



        Button soilBtn = new Button("Soil Data");
        soilBtn.setStyle("-fx-background-color:#FF9800;-fx-text-fill:white;-fx-background-radius:20;");
        soilBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("soil_data.fxml"));
                Parent root = loader.load();
                SoilDataController ctrl = loader.getController();
                ctrl.setCropId(crop.getId());
                Stage s = (Stage) soilBtn.getScene().getWindow();
                s.setScene(new Scene(root));
                s.setTitle("Soil Data for Crop #" + crop.getId());
            } catch (IOException ex) {
                if (resultArea != null) resultArea.setText("Load error: " + ex.getMessage());
            }
        });

        Button qrBtn = new Button("QR Code");
        qrBtn.setStyle("-fx-background-color:#9b59b6;-fx-text-fill:white;-fx-background-radius:20;");
        qrBtn.setOnAction(ev -> showQRCode(crop));

        btnRow.getChildren().addAll( updateBtn, soilBtn, qrBtn);

        /* ── Assemble card ────────────────────────────────────────────────────── */
        card.getChildren().addAll(
                topBox,
                title,
                event,
                btnRow
        );

        return card;
    }


    /* Small helper so the lambda stays tidy */
    /**
     * Opens the Update dialog, pre-populating all fields
     * from the given crop, then waits for it to close.
     */
    private void openUpdateForm(Crop crop) {
        try {
            // Load the new FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/update_crop.fxml")
            );
            Parent root = loader.load();

            // Grab its controller, tell it we want "update" mode
            CropController ctrl = loader.getController();
            ctrl.setUpdateMode(true);
            ctrl.setSelectedCrop(crop);
            // This will fill in cropEventField, typeCombo, etc.
            ctrl.populateFields(crop);

            // Show as a modal dialog
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Update Crop");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            // After it's closed, reload the main list
            loadAllCrops();

        } catch (IOException e) {
            e.printStackTrace();
            if (resultArea != null)
                resultArea.setText("Error opening update form: " + e.getMessage());
        }
    }





    @FXML
    private void populateFields(Crop crop) {
        if (crop != null) {
            Platform.runLater(() -> {
                cropEventField.setText(crop.getCropEvent());
                typeCropCombo.setValue(crop.getTypeCrop());
                methodCropCombo.setValue(crop.getMethodCrop());
                
                // Set plantation date and time
                plantationDatePicker.setValue(LocalDate.parse(crop.getPlantationDate()));
                String[] plantationTime = crop.getHourPlantation().split(":");
                plantationHourSpinner.getValueFactory().setValue(Integer.parseInt(plantationTime[0]));
                plantationMinuteSpinner.getValueFactory().setValue(Integer.parseInt(plantationTime[1]));
                
                // Set crop date and time
                cropDatePicker.setValue(LocalDate.parse(crop.getCropDate()));
                String[] cropTime = crop.getHourCrop().split(":");
                cropHourSpinner.getValueFactory().setValue(Integer.parseInt(cropTime[0]));
                cropMinuteSpinner.getValueFactory().setValue(Integer.parseInt(cropTime[1]));
            });
        }
    }

    private boolean validateInputs() {
        StringBuilder errorMessages = new StringBuilder();
        boolean isValid = true;

        if (cropEventField.getText().trim().isEmpty()) {
            errorMessages.append("- Crop event cannot be empty\n");
            isValid = false;
        }

        if (typeCropCombo.getValue() == null) {
            errorMessages.append("- Please select a crop type\n");
            isValid = false;
        }

        if (methodCropCombo.getValue() == null) {
            errorMessages.append("- Please select a crop method\n");
            isValid = false;
        }

        if (plantationDatePicker.getValue() == null) {
            errorMessages.append("- Please select a plantation date\n");
            isValid = false;
        }

        if (cropDatePicker.getValue() == null) {
            errorMessages.append("- Please select a crop date\n");
            isValid = false;
        }

        // Check planting and harvest dates against periods
        if (isValid && selectedCropPeriods != null && 
            plantationDatePicker.getValue() != null && cropDatePicker.getValue() != null) {
            LocalDate plantationDate = plantationDatePicker.getValue();
            LocalDate cropDate = cropDatePicker.getValue();
            
            try {
                Pair<LocalDate, LocalDate> plantingPeriod = 
                    parseDateRange(selectedCropPeriods.plantingPeriod);
                Pair<LocalDate, LocalDate> harvestPeriod = 
                    parseDateRange(selectedCropPeriods.harvestPeriod);
                
                if (!isDateInRange(plantationDate, plantingPeriod.getKey(), plantingPeriod.getValue())) {
                    errorMessages.append("Warning: Selected plantation date is outside the optimal period!\n")
                        .append("The recommended plantation period for ")
                        .append(selectedCropPeriods.cropName)
                        .append(" is between ")
                        .append(plantingPeriod.getKey().format(DateTimeFormatter.ofPattern("dd/MM")))
                        .append(" and ")
                        .append(plantingPeriod.getValue().format(DateTimeFormatter.ofPattern("dd/MM")))
                        .append("\n");
                    isValid = false;
                }
                
                if (!isDateInRange(cropDate, harvestPeriod.getKey(), harvestPeriod.getValue())) {
                    errorMessages.append("Warning: Selected harvest date is outside the optimal period!\n")
                        .append("The recommended harvest period for ")
                        .append(selectedCropPeriods.cropName)
                        .append(" is between ")
                        .append(harvestPeriod.getKey().format(DateTimeFormatter.ofPattern("dd/MM")))
                        .append(" and ")
                        .append(harvestPeriod.getValue().format(DateTimeFormatter.ofPattern("dd/MM")))
                        .append("\n");
                    isValid = false;
                }
            } catch (Exception e) {
                System.err.println("Error parsing date ranges: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (!isValid) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following issues:");
            alert.setContentText(errorMessages.toString());
            alert.showAndWait();
        }

        return isValid;
    }

    @FXML
    private void showAdminView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("admin_crops.fxml"));
            Parent root = loader.load();
            
            CropController controller = loader.getController();
            controller.initializeAdminView();
            
            Stage stage = (Stage) cropCardsPane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            resultArea.setText("Error loading admin view: " + e.getMessage());
        }
    }

    private void initializeAdminView() {
        // Set up table columns
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        eventColumn.setCellValueFactory(cellData -> cellData.getValue().cropEventProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeCropProperty());
        methodColumn.setCellValueFactory(cellData -> cellData.getValue().methodCropProperty());
        plantationDateColumn.setCellValueFactory(cellData -> cellData.getValue().plantationDateProperty());
        cropDateColumn.setCellValueFactory(cellData -> cellData.getValue().cropDateProperty());

        // Add action buttons to the actions column
        actionsColumn.setCellFactory(param -> new TableCell<Crop, Void>() {
            private final Button deleteButton = new Button("Delete");
            private final Button editButton = new Button("Edit");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                deleteButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white;");
                editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                
                deleteButton.setOnAction(event -> {
                    Crop crop = getTableView().getItems().get(getIndex());
                    try {
                        cropCRUD.deleteCrop(crop.getId());
                        loadAllCrops();
                    } catch (SQLException e) {
                        resultArea.setText("Error deleting crop: " + e.getMessage());
                    }
                });

                editButton.setOnAction(event -> {
                    Crop crop = getTableView().getItems().get(getIndex());
                    showEditCropForm(crop);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });

        // Load all crops
        loadAllCrops();
    }

    @FXML
    private void goBackToCrops() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("crop.fxml"));
            Parent root = loader.load();
            
            // Get the current stage from any node in the scene
            Stage stage = (Stage) (cropsTable != null ? cropsTable.getScene().getWindow() : 
                                 cropCardsPane != null ? cropCardsPane.getScene().getWindow() : 
                                 resultArea.getScene().getWindow());
            
            // Set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Crop Management");
            stage.show();
        } catch (IOException e) {
            resultArea.setText("Error going back to crops: " + e.getMessage());
        }
    }

    @FXML
    private void showCropView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("crop.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) cropsTable.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            resultArea.setText("Error loading crop view: " + e.getMessage());
        }
    }

    @FXML
    private void showEditCropForm(Crop crop) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("add_crop.fxml"));
            Parent root = loader.load();
            
            CropController controller = loader.getController();
            controller.selectedCrop = crop;
            controller.populateFields(crop);
            
            Stage stage = new Stage();
            stage.setTitle("Edit Crop");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Refresh the table after editing
            loadAllCrops();
        } catch (IOException e) {
            resultArea.setText("Error loading edit form: " + e.getMessage());
        }
    }

    public void setUpdateMode(boolean updateMode) {
        this.isUpdateMode = updateMode;
    }

    public void setSelectedCrop(Crop crop) {
        this.selectedCrop = crop;
    }

    @FXML
    private void showCropCalendar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/crop_calendar.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Crop Calendar");
            stage.setScene(new Scene(root, 800, 600));
            
            // Set the owner window
            if (cropEventField != null && cropEventField.getScene() != null) {
                stage.initOwner(cropEventField.getScene().getWindow());
            } else if (cropCardsPane != null && cropCardsPane.getScene() != null) {
                stage.initOwner(cropCardsPane.getScene().getWindow());
            }
            
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            if (resultArea != null) {
                resultArea.setText("Error loading crop calendar: " + e.getMessage());
            }
        }
    }
    
    /**
     * Opens the disease detection view when the Disease Detection button is clicked
     */
    @FXML
    private void openDiseaseDetection() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/disease_detection.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) cropCardsPane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Disease Detection");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            if (resultArea != null) {
                resultArea.setText("Error loading disease detection view: " + e.getMessage());
            }
        }
    }
    
    /**
     * Opens the crop information view to display detailed crop data from the FAO API
     */
    @FXML
    private void showCropInfo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/crop_info.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Crop Information");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            if (resultArea != null) {
                resultArea.setText("Error loading crop information view: " + e.getMessage());
            }
        }
    }
    
    /**
     * Displays a QR code with crop and soil data information
     * @param crop The crop for which to generate the QR code
     */
    private void showQRCode(Crop crop) {
        try {
            // Get all soil data
            List<SoilData> soilDataList = new SoilDataCRUD().getSoilDataByCropId(crop.getId());
            
            // Format the data
            StringBuilder data = new StringBuilder();
            data.append("Crop Details:\n");
            data.append("ID: ").append(crop.getId()).append("\n");
            data.append("Event: ").append(crop.getCropEvent()).append("\n");
            data.append("Type: ").append(crop.getTypeCrop()).append("\n");
            data.append("Method: ").append(crop.getMethodCrop()).append("\n");
            data.append("Plantation: ").append(crop.getPlantationDate()).append(" ").append(crop.getHourPlantation()).append("\n");
            data.append("Harvest: ").append(crop.getCropDate()).append(" ").append(crop.getHourCrop()).append("\n\n");
            
            if (!soilDataList.isEmpty()) {
                data.append("Soil Data History:\n");
                for (SoilData soilData : soilDataList) {
                    data.append("\nMeasurement Date: ").append(soilData.getDate()).append("\n");
                    data.append("  Humidity: ").append(soilData.getHumidite()).append("%\n");
                    data.append("  pH Level: ").append(soilData.getNiveau_ph()).append("\n");
                    data.append("  Nutrient Level: ").append(soilData.getNiveau_nutriment()).append("\n");
                    data.append("  Soil Type: ").append(soilData.getType_sol()).append("\n");
                    data.append("  ------------------------\n");
                }
            } else {
                data.append("No soil data available for this crop.\n");
            }

            // Generate QR Code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data.toString(), BarcodeFormat.QR_CODE, 400, 400); // Increased size for more data
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            
            // Create popup window
            Stage qrStage = new Stage();
            qrStage.setTitle("QR Code - Crop #" + crop.getId());
            
            // Create layout
            VBox layout = new VBox(10);
            layout.setAlignment(Pos.CENTER);
            layout.setPadding(new Insets(20));
            
            // Add QR code image
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(400);
            imageView.setFitHeight(400);
            
            // Add a label showing preview of the data

            // Add close button
            Button closeButton = new Button("Close");
            closeButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
            closeButton.setOnAction(e -> qrStage.close());
            
            layout.getChildren().addAll(
                new Label("Scan this QR code to view crop and soil data:"),
                imageView,

                closeButton
            );
            
            // Show the window
            Scene scene = new Scene(layout);
            qrStage.setScene(scene);
            qrStage.initModality(Modality.APPLICATION_MODAL);
            qrStage.show();
            
        } catch (SQLException | WriterException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("QR Code Generation Error");
            alert.setContentText("Failed to generate QR code: " + e.getMessage());
            alert.showAndWait();
        }
    }
} 
