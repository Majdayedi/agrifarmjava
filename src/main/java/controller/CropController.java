package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import service.CropCRUD;
import entite.Crop;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.io.IOException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CropController {
    private CropCRUD cropCRUD = new CropCRUD();
    private Crop selectedCrop;
    private final ObservableList<Crop> cropData = FXCollections.observableArrayList();
    
    private final List<String> CROP_TYPES = Arrays.asList(
        "Bean", "Wheat", "watermelon", "Apple", "Orange"
    );
    
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

    @FXML
    public void initialize() {
        System.out.println("Initializing CropController...");
        try {
            // Initialize combo boxes if they exist
            if (typeCropCombo != null) {
                typeCropCombo.getItems().addAll(CROP_TYPES);
            }
            if (methodCropCombo != null) {
                methodCropCombo.getItems().addAll(CROP_METHODS);
            }
            if (filterTypeCombo != null) {
                filterTypeCombo.getItems().add("Tous");
                filterTypeCombo.getItems().addAll(CROP_TYPES);
                filterTypeCombo.getSelectionModel().selectFirst();
                filterTypeCombo.setOnAction(event -> applyFilter());
            }

            // Initialize spinners if they exist
            if (plantationHourSpinner != null) {
                plantationHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
            }
            if (plantationMinuteSpinner != null) {
                plantationMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
            }
            if (cropHourSpinner != null) {
                cropHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
            }
            if (cropMinuteSpinner != null) {
                cropMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
            }

            // Set up search field listener if it exists
            if (searchField != null) {
                searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.isEmpty()) {
                        handleSearchButtonAction();
                    } else {
                        loadAllCrops();
                    }
                });
            }

            // Load initial data
            loadAllCrops();
            System.out.println("CropController initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing CropController: " + e.getMessage());
            e.printStackTrace();
            if (resultArea != null) {
                resultArea.setText("Error initializing: " + e.getMessage());
            }
        }
    }

    @FXML
    private void showAddCropForm() {
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

                if (isUpdateMode) {
                    cropCRUD.updateCrop(crop);
                    if (resultArea != null) {
                        resultArea.setText("Crop updated successfully!");
                    }
                } else {
                    cropCRUD.createCrop(crop);
                    if (resultArea != null) {
                        resultArea.setText("Crop created successfully!");
                    }
                }

                // Close the window
                Stage stage = (Stage) cropEventField.getScene().getWindow();
                stage.close();

                // Get the main stage and refresh its view
                Stage mainStage = (Stage) stage.getOwner();
                if (mainStage != null) {
                    Scene mainScene = mainStage.getScene();
                    if (mainScene != null) {
                        // Get the controller from the main view
                        CropController mainController = (CropController) mainScene.getUserData();
                        if (mainController != null) {
                            mainController.loadAllCrops();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            if (resultArea != null) {
                resultArea.setText("Error saving crop: " + e.getMessage());
            }
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
        System.out.println("Clearing crop fields...");
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
        if (selectedType != null && !selectedType.equals("All")) {
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

    private void loadAllCrops() {
        try {
            List<Crop> allCrops = cropCRUD.getAllCrops();
            if (cropsTable != null) {
                // If we're in admin view, update the table
                ObservableList<Crop> data = FXCollections.observableArrayList(allCrops);
                cropsTable.setItems(data);
            } else {
                // If we're in card view, update the cards
                cropData.clear();
                cropData.addAll(allCrops);
                displayCropCards();
            }
        } catch (SQLException e) {
            resultArea.setText("Error loading crops: " + e.getMessage());
        }
    }

    private void displayCropCards() {
        cropCardsPane.getChildren().clear();
        for (Crop crop : cropData) {
            VBox card = createCropCard(crop);
            cropCardsPane.getChildren().add(card);
        }
    }

    private VBox createCropCard(Crop crop) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(300);
        card.setPrefHeight(250);

        // Header with title and delete button
        StackPane header = new StackPane();
        header.setStyle("-fx-background-color: #4CAF50; -fx-background-radius: 10 10 0 0;");
        header.setPrefHeight(40);
        
        Label titleLabel = new Label("Crop #" + crop.getId());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        
        Button deleteButton = new Button("X");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 0; -fx-min-width: 25; -fx-min-height: 25; -fx-padding: 0; -fx-border-width: 0;");
        deleteButton.setOnAction(event -> {
            try {
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Confirm Delete");
                confirmDialog.setHeaderText("Delete Crop");
                confirmDialog.setContentText("Are you sure you want to delete this crop? This action cannot be undone.");
                
                if (confirmDialog.showAndWait().get() == ButtonType.OK) {
                    cropCRUD.deleteCrop(crop.getId());
                    loadAllCrops();
                    resultArea.setText("Crop deleted successfully!");
                }
            } catch (SQLException e) {
                resultArea.setText("Error deleting crop: " + e.getMessage());
            }
        });
        
        StackPane.setAlignment(deleteButton, Pos.TOP_RIGHT);
        StackPane.setMargin(deleteButton, new Insets(5, 5, 0, 0));
        
        header.getChildren().addAll(titleLabel, deleteButton);
        
        // Crop details
        VBox details = new VBox(10);
        details.setAlignment(Pos.CENTER_LEFT);
        details.setPadding(new Insets(20));
        details.setStyle("-fx-background-color: white;");
        
        Label eventLabel = new Label("Event: " + crop.getCropEvent());
        eventLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        
        Label typeLabel = new Label("Type: " + crop.getTypeCrop());
        typeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        
        Label methodLabel = new Label("Method: " + crop.getMethodCrop());
        methodLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        
        Label plantationLabel = new Label("Plantation: " + crop.getPlantationDate() + " " + crop.getHourPlantation());
        plantationLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        
        Label cropLabel = new Label("Crop: " + crop.getCropDate() + " " + crop.getHourCrop());
        cropLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        
        // Action buttons
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(10));
        buttons.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 0 0 10 10;");
        
        Button updateButton = new Button("Update");
        updateButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 5;");
        updateButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("add_crop.fxml"));
                Parent root = loader.load();
                
                CropController controller = loader.getController();
                controller.setUpdateMode(true);
                controller.setSelectedCrop(crop);
                controller.populateFields(crop);
                
                Stage stage = new Stage();
                stage.setTitle("Modify Crop");
                stage.setScene(new Scene(root));
                stage.showAndWait();
                
                // Refresh the main view after the modify window closes
                loadAllCrops();
            } catch (IOException e) {
                resultArea.setText("Error loading update form: " + e.getMessage());
            }
        });
        
        Button detailsButton = new Button("Details");
        detailsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 5;");
        detailsButton.setOnAction(event -> {
            resultArea.setText(crop.toString());
        });

        Button soilDataButton = new Button("Soil Data");
        soilDataButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 5;");
        soilDataButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("soil_data.fxml"));
                Parent root = loader.load();
                
                SoilDataController controller = loader.getController();
                controller.setCropId(crop.getId());
                
                Scene scene = new Scene(root);
                Stage stage = (Stage) soilDataButton.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Soil Data for Crop #" + crop.getId());
            } catch (IOException e) {
                resultArea.setText("Error loading soil data: " + e.getMessage());
            }
        });
        
        buttons.getChildren().addAll(updateButton, detailsButton, soilDataButton);
        
        details.getChildren().addAll(eventLabel, typeLabel, methodLabel, plantationLabel, cropLabel);
        card.getChildren().addAll(header, details, buttons);
        
        return card;
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

        if (!isValid) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
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
} 