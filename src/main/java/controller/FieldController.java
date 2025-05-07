package controller;

import entite.Farm;
import entite.Field;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import service.FieldService;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import service.WeatherService;
import service.WeatherService.Weather;

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
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javafx.stage.Modality;
public class FieldController {

    private final FieldService fieldService = new FieldService();
    public Button weatherBtn;
    public ImageView weatherIcon;
    public Label weatherTemp;
    public Label weatherDesc;
    private Field selectedField;
    private final Map<Integer, String> fieldAccessCodes = new HashMap<>();

    @FXML
    private GridPane fieldgrid;
    private Farm currentFarm;


    @FXML
    private Button addFieldBtn;

    @FXML
    private Button backButton;

    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;

    @FXML
    private ComboBox<String> cropTypeCombo;
    @FXML
    private TextField surfaceMinField;
    @FXML
    private TextField surfaceMaxField;
    @FXML
    private Button filterButton;
    private WeatherService.Weather weather;
    public void setFirst(WeatherService.Weather first) {
        this.weather= first;
    }
    @FXML
    public void initialize() {

        fieldgrid.setHgap(30);
        fieldgrid.setVgap(200);

        if (backButton != null) {
            backButton.setOnAction(e -> handleBackToFarm());
        }
        setupSearch();

    }

    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.isEmpty()) {
                    if (currentFarm != null) loadField(currentFarm, weather);
                } else {
                    searchFields(newValue);
                }
            });
        }
        if (searchButton != null) {
            searchButton.setOnAction(e -> searchFields(searchField.getText()));
        }
    }

    private void setupFilters() {
        FieldService fieldService = new FieldService();
        List <String> cropTypes = fieldService.getCropList(currentFarm);


        cropTypeCombo.getItems().addAll(cropTypes);
        cropTypeCombo.getSelectionModel().selectFirst();
        filterButton.setOnAction(e -> applyFilters());
    }


        private void searchFields(String searchText) {
            fieldgrid.getChildren().clear();
            if (currentFarm == null) return;
            List<Field> fields = fieldService.getFieldsByFarm(currentFarm.getId());
            int col = 0, row = 0;
            try {
                for (Field field : fields) {
                    if (matchesSearch(field, searchText)) {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/field.fxml"));
                        Pane card = loader.load();
                        ((Label) card.lookup("#Namef")).setText(field.getName());
                        ((Label) card.lookup("#Surfacef")).setText(String.format("%.2f ha", field.getSurface()));
                        ((Label) card.lookup("#Budgetf")).setText(String.format("$%.2f", field.getBudget()));
                        ((Label) card.lookup("#incomef")).setText(String.format("$%.2f", field.getIncome()));
                        ((Label) card.lookup("#outcomef")).setText(String.format("$%.2f", field.getOutcome()));
                        ((Label) card.lookup("#cropf")).setText(field.getCrop() != null ? field.getCrop().getTypeCrop() : "No Crop");
                        ((Label) card.lookup("#descriptionf")).setText(field.getDescription());
                        Button deleteBtn = (Button) card.lookup("#deleteBtn");
                        Button modifyBtn = (Button) card.lookup("#modifyBtn");
                        Button detailsBtn = (Button) card.lookup("#detailsBtn");
                        deleteBtn.setOnAction(e -> handleDelete(field, card));
                        modifyBtn.setOnAction(e -> handleModify(field));
                        detailsBtn.setOnAction(e -> handleDetails(field));
                        fieldgrid.add(card, col % 5, row);
                        col++;
                        if (col % 5 == 0) row++;
                    }
                }
            }
        } catch (Exception e) {
            showError("Error Loading Fields", "Failed to load fields: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean matchesSearch(Field field, String searchText) {
        if (searchText == null || searchText.isEmpty()) return true;
        String searchLower = searchText.toLowerCase();
        return field.getName().toLowerCase().contains(searchLower)
                || String.valueOf(field.getSurface()).contains(searchText)
                || String.valueOf(field.getBudget()).contains(searchText)
                || String.valueOf(field.getIncome()).contains(searchText)
                || String.valueOf(field.getOutcome()).contains(searchText)
                || (field.getCrop() != null && field.getCrop().getTypeCrop().toLowerCase().contains(searchLower))
                || (field.getDescription() != null && field.getDescription().toLowerCase().contains(searchLower));
    }

    @FXML
    private void handleSearch() {
        searchFields(searchField.getText());
    }

    public void setFarm(Farm farm) {
        this.currentFarm = farm;
    }
    public void loadField(Farm farm, Weather first) {
        // Debug: Print farm ID
        System.out.println("Debug - Farm ID: " + farm.getId());

        // Clear existing content in the grid before refreshing
        fieldgrid.getChildren().clear();
        setFarm(farm);
        setFirst(first);
        fieldgrid.setStyle("-fx-padding: 20; -fx-background-color: #f0f0f0;");
        Image image = new Image("http://openweathermap.org/img/wn/" + weather.getIcon() + "@2x.png");

        weatherIcon.setImage(image);
        weatherTemp.setText(String.format("%.1f°C", weather.getTemperature()));
        weatherDesc.setText(weather.getDescription());
        setupFilters();
        // Set up the add field button
        if (addFieldBtn != null) {
            addFieldBtn.setOnAction(e -> handleAddField(currentFarm));
        }
        weatherBtn.setOnAction(e -> handleWeather());


        // Load fields for the farm
        List<Field> fields = fieldService.getFieldsByFarm(farm.getId());
        int col = 0, row = 0;

        try {
            for (Field field : fields) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/field.fxml"));
                Pane card = loader.load();

                // Set field data
                ((Label) card.lookup("#Namef")).setText(field.getName());
                ((Label) card.lookup("#Surfacef")).setText(String.format("%.2f ha", field.getSurface()));

                ((Label) card.lookup("#Budgetf")).setText(String.format("$%.2f", field.getBudget()));

                ((Label) card.lookup("#incomef")).setText(String.format("$%.2f", field.getIncome()));
                ((Label) card.lookup("#outcomef")).setText(String.format("$%.2f", field.getOutcome()));
                System.out.println(field.toString());
                ((Label) card.lookup("#cropf")).setText(field.getCrop() != null ? field.getCrop().getTypeCrop() : "No Crop");

                ((Label) card.lookup("#descriptionf")).setText(field.getDescription());


                // Get buttons and add event handlers
                Button deleteBtn = (Button) card.lookup("#deleteBtn");
                Button modifyBtn = (Button) card.lookup("#modifyBtn");
                Button detailsBtn = (Button) card.lookup("#detailsBtn");

                // Setup delete button
                deleteBtn.setOnAction(e -> handleDelete(field, card));

                // Setup modify button
                modifyBtn.setOnAction(e -> handleModify(field));

                // Setup details button
                detailsBtn.setOnAction(e -> handleDetails(field));
                // Add QR code and verify buttons
                Button qrCodeButton = new Button("QR Code");
                qrCodeButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");
                qrCodeButton.setOnAction(e -> showQRCode(field));

                Button verifyButton = new Button("Verify");
                verifyButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                verifyButton.setOnAction(e -> verifyAccessCode(field));

                // Add QR code and verify buttons to the buttonBox
                setupButtonBox(card, field, qrCodeButton, verifyButton);

                // Add to grid
                fieldgrid.add(card, col % 3, row);
                col++;
                if (col % 3 == 0) row++;
            }
        } catch (Exception e) {
            showError("Error Loading Fields", "Failed to load fields: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void handleAddField(Farm farm) {
        try {
            System.out.println("Debug - Farm ID: " + farm.getId());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addfield.fxml"));
            Pane addForm = loader.load();

            AddFieldController addFieldController = loader.getController();
            addFieldController.setFarm(farm);
            addFieldController.setFirst(weather);

            BorderPane mainContainer = getMainContainer();
            if (mainContainer != null) {
                mainContainer.setCenter(addForm);
            } else {
                showError("Error", "Could not find main container");
            }
        } catch (IOException e) {
            showError("Error", "Could not load add field form: " + e.getMessage());
        }
    }

    private void handleModify(Field field) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/updatefield.fxml"));
            Pane modifyForm = loader.load();

            // Récupérer le contrôleur et passer l'objet Field
            AddFieldController addFieldController = loader.getController();
            addFieldController.setField(field);

            BorderPane mainContainer = getMainContainer();
            if (mainContainer != null) {
                mainContainer.setCenter(modifyForm);
            } else {
                showError("Error", "Could not find main container");
            }
        } catch (IOException e) {
            showError("Error", "Could not load modify field form: " + e.getMessage());
        }
    }
    @FXML
    private void handleWeather() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/WeatherForecast.fxml"));
            Pane weatherPane = loader.load();
            WeatherForecastController controller = loader.getController();
            controller.setFirst(weather);
            controller.initialize(currentFarm);
            BorderPane mainContainer = getMainContainer();
            if (mainContainer != null) {
                mainContainer.setCenter(weatherPane);
            } else {
                showError("Error", "Could not find main container");
            }
        } catch (IOException e) {
            showError("Error", "Could not load weather forecast: " + e.getMessage());
        }
    }

    private void handleDetails(Field field) {
        try {
            // Use getResourceAsStream for more reliable loading
            InputStream fxmlStream = getClass().getResourceAsStream("/TaskDetails.fxml");
            if (fxmlStream == null) {
                System.err.println("Error: TaskDetails.fxml not found in resources");
                return;
            }

            FXMLLoader loader = new FXMLLoader();
            Pane details = loader.load(fxmlStream);

            // Pass both the field and farm to TaskController
            TaskController taskController = loader.getController();
            if (taskController != null) {
                taskController.LoadTasks(field);
                taskController.setCurrentFarm(currentFarm);
                taskController.setFirst(weather);// Pass the farm object
            } else {
                System.err.println("Error: TaskController not initialized");
            }

            // Replace main content
            BorderPane mainContainer = getMainContainer();
            if (mainContainer != null) {
                mainContainer.setCenter(details);
            } else {
                showError("Error", "Could not find main container");
            }
        } catch (IOException e) {
            showError("Error", "Could not load task details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDelete(Field field, Pane card) {
        fieldService.delete(field);
        fieldgrid.getChildren().remove(card);
    }

    private BorderPane getMainContainer() {
        Node current = fieldgrid;
        while (current != null && !(current instanceof BorderPane)) {
            current = current.getParent();
        }
        return (BorderPane) current;
    }

    private void showError(String title, String message) {
        System.err.println(title + ": " + message);
    }
    @FXML
    private void handleBack(Farm farm) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/farmdisplay.fxml"));
            Pane fieldDisplay = loader.load();

            // Get the FieldController and pass the farm
            FieldController fieldController = loader.getController();
            if (fieldController != null) {
                fieldController.loadField(farm, weather);
            }

            // Replace main content
            BorderPane mainContainer = getMainContainer();
            if (mainContainer != null) {
                mainContainer.setCenter(fieldDisplay);
            } else {
                showError("Error", "Could not find main container");
            }
        } catch (IOException e) {
            showError("Error", "Could not navigate back: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleBackToFarm() {
        try {
            // Get the current stage
            Stage stage = (Stage) backButton.getScene().getWindow();

            // Load the farm display view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/farmdisplay.fxml"));
            Parent root = loader.load();

            // Get the controller and load farms
            FarmController controller = loader.getController();
            controller.loadFarms1();

            // Set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Error", "Could not navigate back to farm: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setCurrentFarm(Farm farm) {
        this.currentFarm = farm;
    }

    private void applyFilters() {
        // Read user input
        String cropType = cropTypeCombo.getValue();
        String minSurfaceStr = surfaceMinField.getText();
        String maxSurfaceStr = surfaceMaxField.getText();

        // Convert surface input to numbers (if given)
        Double minSurface = null, maxSurface = null;
        try {
            if (minSurfaceStr != null && !minSurfaceStr.isEmpty()) {
                minSurface = Double.valueOf(minSurfaceStr);
            }
        } catch (NumberFormatException e) {
            showError("Invalid Input", "Min Surface must be a number.");
            return;
        }

        try {
            if (maxSurfaceStr != null && !maxSurfaceStr.isEmpty()) {
                maxSurface = Double.valueOf(maxSurfaceStr);
            }
        } catch (NumberFormatException e) {
            showError("Invalid Input", "Max Surface must be a number.");
            return;
        }

        // Debug info
        System.out.println("Filtering: cropType='" + cropType + "', minSurface=" + minSurface + ", maxSurface=" + maxSurface);

        // Clear the current grid
        fieldgrid.getChildren().clear();
        if (currentFarm == null) return;

        // Fetch fields from the service
        List<Field> fields = fieldService.getFieldsByFarm(currentFarm.getId());

        int col = 0, row = 0;

        try {
            for (Field field : fields) {
                // --- Apply Filters ---

                // Filter by crop type
                if (cropType != null && !cropType.trim().equalsIgnoreCase("Select crop")) {
                    String fieldCrop = (field.getCrop() != null) ? field.getCrop().getTypeCrop() : "No Crop";
                    if (!fieldCrop.equalsIgnoreCase(cropType)) {
                        continue; // Skip this field
                    }
                }

                // Filter by minimum surface
                if (minSurface != null && field.getSurface() < minSurface) {
                    continue; // Skip this field
                }

                // Filter by maximum surface
                if (maxSurface != null && field.getSurface() > maxSurface) {
                    continue; // Skip this field
                }

                // --- If passed all filters, load and display the field card ---

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/field.fxml"));
                Pane card = loader.load();

                // Fill card labels
                ((Label) card.lookup("#Namef")).setText(field.getName());
                ((Label) card.lookup("#Surfacef")).setText(String.format("%.2f ha", field.getSurface()));
                ((Label) card.lookup("#Budgetf")).setText(String.format("$%.2f", field.getBudget()));
                ((Label) card.lookup("#incomef")).setText(String.format("$%.2f", field.getIncome()));
                ((Label) card.lookup("#outcomef")).setText(String.format("$%.2f", field.getOutcome()));
                ((Label) card.lookup("#cropf")).setText(field.getCrop() != null ? field.getCrop().getTypeCrop() : "No Crop");
                ((Label) card.lookup("#descriptionf")).setText(field.getDescription());

                // Setup button actions
                Button deleteBtn = (Button) card.lookup("#deleteBtn");
                Button modifyBtn = (Button) card.lookup("#modifyBtn");
                Button detailsBtn = (Button) card.lookup("#detailsBtn");

                deleteBtn.setOnAction(e -> handleDelete(field, card));
                modifyBtn.setOnAction(e -> handleModify(field));
                detailsBtn.setOnAction(e -> handleDetails(field));


                // Add the card to the grid
                fieldgrid.add(card, col % 3, row);

                col++;
                if (col % 3 == 0) {
                    row++;
                }
            }
        } catch (Exception e) {
            showError("Error Loading Fields", "Failed to load fields: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Add this method to receive weather data and update the UI
    public void setWeather(WeatherService.Weather weather) {
        this.weather = weather;
        updateWeatherCard();
    }

    // Add this helper method to update the weather card UI
    private void updateWeatherCard() {
        if (weather != null) {
            weatherTemp.setText(String.format("%.1f°C", weather.getTemperature()));
            weatherDesc.setText(weather.getDescription());
            weatherIcon.setImage(new Image("http://openweathermap.org/img/wn/" + weather.getIcon() + "@2x.png"));
        }
    }
    private void setupButtonBox(Pane card, Field field, Button qrCodeButton, Button verifyButton) {
        try {
            // Try to find existing buttonBox
            HBox buttonBox = (HBox) card.lookup("#buttonBox");
            System.out.println("Debug - ButtonBox lookup result for field " + field.getId() + ": " +
                    (buttonBox != null ? "found" : "not found"));

            if (buttonBox != null) {
                // If buttonBox exists in FXML, add buttons to it
                buttonBox.getChildren().addAll(qrCodeButton, verifyButton);
                System.out.println("Debug - Added buttons to existing buttonBox for field: " + field.getName());
            } else {
                // Log error for debugging
                System.err.println("Warning: buttonBox not found in field.fxml for field: " + field.getName() +
                        " (ID: " + field.getId() + "). Creating one programmatically.");

                // Create buttonBox programmatically if not found in FXML
                buttonBox = new HBox(10); // 10 is the spacing
                buttonBox.setAlignment(Pos.CENTER);
                buttonBox.setId("buttonBox"); // Set the ID for future reference
                buttonBox.getStyleClass().add("button-container"); // Add CSS class
                buttonBox.getChildren().addAll(qrCodeButton, verifyButton);

                // Find the VBox with the farm-details class as defined in the FXML
                VBox fieldDetails = (VBox) card.lookup(".farm-details");
                System.out.println("Debug - VBox farm-details lookup result for field " + field.getId() + ": " +
                        (fieldDetails != null ? "found" : "not found"));

                if (fieldDetails != null) {
                    // Add the buttonBox to the VBox with farm-details class
                    fieldDetails.getChildren().add(buttonBox);
                    System.out.println("Debug - Added buttonBox to VBox.farm-details for field: " + field.getName());
                } else {
                    System.err.println("Warning: Could not find VBox with farm-details class for field: " + field.getName());

                    // Try alternative lookup strategies
                    if (card instanceof VBox) {
                        boolean added = false;
                        ObservableList<Node> children = ((VBox) card).getChildren();
                        for (Node child : children) {
                            if (child instanceof VBox) {
                                VBox vbox = (VBox) child;
                                vbox.getChildren().add(buttonBox);
                                System.out.println("Debug - Added buttonBox to child VBox for field: " + field.getName());
                                added = true;
                                break;
                            }
                        }

                        if (!added) {
                            // If we couldn't find a child VBox, add directly to the main VBox
                            ((VBox) card).getChildren().add(buttonBox);
                            System.out.println("Debug - Added buttonBox directly to main VBox for field: " + field.getName());
                        }
                    } else {
                        // Last resort - add directly to whatever pane we have
                        ((Pane) card).getChildren().add(buttonBox);
                        System.out.println("Debug - Added buttonBox directly to card Pane for field: " + field.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error setting up buttonBox for field " + field.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showQRCode(Field field) {
        try {
            // Generate new random code
            String accessCode = generateRandomCode();
            fieldAccessCodes.put(field.getId(), accessCode);

            // Generate QR Code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(accessCode, BarcodeFormat.QR_CODE, 300, 300);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);

            // Create popup window
            Stage qrStage = new Stage();
            qrStage.setTitle("Field Access Code");

            VBox layout = new VBox(10);
            layout.setAlignment(Pos.CENTER);
            layout.setPadding(new Insets(20));

            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(300);
            imageView.setFitHeight(300);

            Button closeButton = new Button("Close");
            closeButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
            closeButton.setOnAction(e -> qrStage.close());

            layout.getChildren().addAll(
                    new Label("Scan this QR code to get the access code"),
                    imageView,
                    closeButton
            );

            Scene scene = new Scene(layout);
            qrStage.setScene(scene);
            qrStage.initModality(Modality.APPLICATION_MODAL);
            qrStage.show();

        } catch (WriterException e) {
            showError("QR Code Error", "Failed to generate QR code: " + e.getMessage());
        }
    }

    private String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    private void verifyAccessCode(Field field) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Verify Access Code");
        dialog.setHeaderText("Enter the access code from the QR code");

        ButtonType verifyButtonType = new ButtonType("Verify", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(verifyButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField codeField = new TextField();
        codeField.setPromptText("Enter access code");
        grid.add(new Label("Access Code:"), 0, 0);
        grid.add(codeField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        Node verifyButton = dialog.getDialogPane().lookupButton(verifyButtonType);
        verifyButton.setDisable(true);

        codeField.textProperty().addListener((observable, oldValue, newValue) -> {
            verifyButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == verifyButtonType) {
                return codeField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(code -> {
            String storedCode = fieldAccessCodes.get(field.getId());
            if (code.equals(storedCode)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Access code verified successfully!");
                alert.showAndWait();
                handleDetails(field);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Invalid access code. Please try again.");
                alert.showAndWait();
            }
        });
    }

    private GridPane findGridPaneInCard(Pane card) {
        System.out.println("Debug - Searching for GridPane in field card");

        try {
            // Try looking up GridPane within .farm-details VBox first
            VBox farmDetails = (VBox) card.lookup(".farm-details");
            if (farmDetails != null) {
                System.out.println("Debug - Found .farm-details VBox, searching for GridPane inside");

                // Log the structure of farmDetails for debugging
                System.out.println("Debug - .farm-details children count: " + farmDetails.getChildren().size());
                int childIndex = 0;

                for (Node child : farmDetails.getChildren()) {
                    System.out.println("Debug - Child " + childIndex + " type: " + child.getClass().getSimpleName());
                    childIndex++;

                    if (child instanceof GridPane) {
                        System.out.println("Debug - Found GridPane in .farm-details children");
                        return (GridPane) child;
                    }
                }
            } else {
                System.out.println("Debug - .farm-details VBox not found");
            }

            // Try with CSS selectors
            for (Node node : card.lookupAll("GridPane")) {
                if (node instanceof GridPane) {
                    System.out.println("Debug - Found GridPane using CSS selector");
                    return (GridPane) node;
                }
            }

            // If not found, try general DOM traversal
            System.out.println("Debug - Performing recursive search for GridPane");
            Node result = findFirstChildOfType(card, GridPane.class);
            if (result != null) {
                System.out.println("Debug - Found GridPane using recursive search");
                return (GridPane) result;
            }

            // Fallback - no GridPane found
            System.out.println("Debug - Failed to find GridPane, returning null");
            return null;
        } catch (Exception e) {
            System.err.println("Error finding GridPane: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    private void setLabelTextSafely(Pane container, String selector, String text) {
        Label label = (Label) container.lookup(selector);
        if (label != null) {
            label.setText(text);
        } else {
            System.err.println("Warning: Label with selector '" + selector + "' not found");
        }
    }

    /**
     * Finds the first child of a specific type in the node hierarchy
     */
    private Node findFirstChildOfType(Pane container, Class<?> type) {
        for (Node node : container.getChildren()) {
            if (type.isInstance(node)) {
                return node;
            } else if (node instanceof Pane) {
                Node result = findFirstChildOfType((Pane) node, type);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
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
            if (fieldgrid != null && fieldgrid.getScene() != null) {
                stage.initOwner(fieldgrid.getScene().getWindow());
            }

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Error loading crop calendar: " + e.getMessage());
        }
    }

}