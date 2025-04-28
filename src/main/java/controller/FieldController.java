    package controller;

    import entite.Farm;
    import entite.Field;
    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.Node;
    import javafx.scene.control.Button;
    import javafx.scene.control.Label;
    import javafx.scene.control.TextField;
    import javafx.scene.control.ComboBox;
    import javafx.scene.image.Image;
    import javafx.scene.image.ImageView;
    import javafx.scene.layout.GridPane;
    import javafx.scene.layout.Pane;
    import javafx.scene.layout.BorderPane;
    import service.FieldService;

    import java.io.IOException;
    import java.io.InputStream;
    import java.util.List;
    import javafx.stage.Stage;
    import javafx.scene.Parent;
    import javafx.scene.Scene;
    import service.WeatherService;
import service.WeatherService.Weather;
    public class FieldController {

        private final FieldService fieldService = new FieldService();
        public Button weatherBtn;
        public ImageView weatherIcon;
        public Label weatherTemp;
        public Label weatherDesc;

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
                        fieldgrid.add(card, col % 3, row);
                        col++;
                        if (col % 3 == 0) row++;
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

    }