package controller;

import entite.Farm;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.BorderPane;
import service.FarmService;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.io.IOException;
import javafx.scene.Node;
import service.WeatherService;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import service.TwilioService;
public class FarmController {
    public Button cropButton;
    public Button weatherBtn;
    @FXML
    private GridPane farmgrid;
    
    @FXML
    private Button addFarmBtn;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private Button homeButton;

    @FXML private Label weatherTemp;
    @FXML private Label weatherDesc;
    @FXML private ImageView weatherIcon;

    private final FarmService farmService = new FarmService();
    private WeatherService.Weather weather;
    private List<WeatherService.Weather> forecasts;
    public void taks( List<WeatherService.Weather> forecasts) {
        this.forecasts=forecasts;
        this.weather=forecasts.get(0);
    }
    @FXML
    public void initialize() {
        farmgrid.setAlignment(Pos.CENTER);
        farmgrid.setHgap(30);
        farmgrid.setVgap(200);
        loadFarms();
        
        // Add Farm button handler
        addFarmBtn.setOnAction(e -> handleAddFarm());
        cropButton.setOnAction(e -> handleCropButton());

        // Add search functionality
        setupSearch();
    }

    private void setupSearch() {
        // Add listener to search field for real-time search
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                loadFarms(); // Show all farms if search is empty
            } else {
                searchFarms(newValue);
            }
        });

        // Add search button handler
        searchButton.setOnAction(e -> searchFarms(searchField.getText()));
    }

    private void searchFarms(String searchText) {
        // Clear existing content
        farmgrid.getChildren().clear();

        // Get all farms
        List<Farm> farms = farmService.readAll();
        int col = 0, row = 0;

        try {
            for (Farm farm : farms) {
                if (matchesSearch(farm, searchText)) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/farmcontainer.fxml"));
                    Pane card = loader.load();

                    // Set farm data
                    ((Label)card.lookup("#farmName")).setText(farm.getName());
                    ((Label)card.lookup("#farmLocation")).setText(farm.getAdress());
                    ((Label)card.lookup("#farmSurface")).setText(String.format("%.2f ha", farm.getSurface()));
                    ((Label)card.lookup("#farmBudget")).setText(String.format("$%.2f", farm.getBudget()));

                    // Get buttons and add event handlers
                    Button deleteBtn = (Button) card.lookup("#deleteBtn");
                    Button modifyBtn = (Button) card.lookup("#modifyBtn");
                    Button detailsBtn = (Button) card.lookup("#detailsBtn");

                    // Setup delete button
                    deleteBtn.setOnAction(e -> handleDelete(farm, card));

                    // Setup modify button
                    modifyBtn.setOnAction(e -> handleModify(farm));
                    // Setup details button
                    detailsBtn.setOnAction(e -> handleDetails(farm, weather));

                    // Add to grid
                    farmgrid.add(card, col % 3, row);
                    col++;
                    if (col % 3 == 0) row++;
                }
            }
        } catch (Exception e) {
            showError("Error Loading Farms", "Failed to load farms: " + e.getMessage());
        }
    }

    private boolean matchesSearch(Farm farm, String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            return true;
        }

        String searchLower = searchText.toLowerCase();
        return farm.getName().toLowerCase().contains(searchLower) ||
               farm.getAdress().toLowerCase().contains(searchLower) ||
               String.valueOf(farm.getSurface()).contains(searchText) ||
               String.valueOf(farm.getBudget()).contains(searchText);
    }

    private BorderPane getMainContainer() {
        Node current = farmgrid;
        while (current != null && !(current instanceof BorderPane)) {
            current = current.getParent();
        }
        return (BorderPane) current;
    }

    public void loadFarms() {
        // Clear existing content in the grid before refreshing
        farmgrid.getChildren().clear();

        List<Farm> farms = farmService.readAll();
        int col = 0, row = 0;

        try {
            for (Farm farm : farms) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/farmcontainer.fxml"));
                Pane card = loader.load();
                double lat = farm.getLat();
                double lon = farm.getLon();
                String weatherData = WeatherService.getWeatherData(lat, lon);
                // Debug: print the full API response
                System.out.println("Full OpenWeatherMap API response:\n" +"farm: "+farm.getId() +" data:" +weatherData);
                List<WeatherService.Weather> forecast = WeatherService.getWeatherForecasts(weatherData);
                WeatherForecastController first = new WeatherForecastController();
                first.now(forecast, farm);
                taks(forecast);
                List<String> dangerousWeathers = Arrays.asList(
                        "light rain", "thunderstorm", "hail", "extreme heat",
                        "frost", "strong winds", "blizzard", "tornado",
                        "dense fog", "extreme cold"
                );

                if (dangerousWeathers.contains(farm.getWeather())) {
                    TwilioService.sendWeatherAlert("+21655771406", farm.getName(), weather.getDescription(), weather.getTemperature());
                }

                ((Label)card.lookup("#farmName")).setText(farm.getName());
                ((Label)card.lookup("#farmweather")).setText(farm.getWeather());

                ((Label)card.lookup("#farmLocation")).setText(farm.getAdress());
                ((Label)card.lookup("#farmSurface")).setText(String.format("%.2f ha", farm.getSurface()));
                ((Label)card.lookup("#farmBudget")).setText(String.format("$%.2f", farm.getBudget()));

                // Get buttons and add event handlers
                Button deleteBtn = (Button) card.lookup("#deleteBtn");
                Button modifyBtn = (Button) card.lookup("#modifyBtn");
                Button detailsBtn = (Button) card.lookup("#detailsBtn");

                // Setup delete button
                deleteBtn.setOnAction(e -> handleDelete(farm, card));

                // Setup modify button
                modifyBtn.setOnAction(e -> handleModify(farm));

                detailsBtn.setOnAction(e -> handleDetails(farm, weather));

                farmgrid.add(card, col % 3, row);
                col++;
                if (col % 3 == 0) row++;
            }
        } catch (Exception e) {
            showError("Error Loading Farms", "Failed to load farms: " + e.getMessage());
        }
    }

    private void handleAddFarm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/add.fxml"));
            Pane addForm = loader.load();

            BorderPane mainContainer = getMainContainer();
            if (mainContainer != null) {
                // Replace the center content with the add form
                mainContainer.setCenter(addForm);
            } else {
                showError("Error", "Could not find main container");
            }

        } catch (IOException e) {
            showError("Error", "Could not load add farm form: " + e.getMessage());
        }
    }

    private void handleCropButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/crop.fxml"));
            Pane cropPane = loader.load();

            BorderPane mainContainer = getMainContainer();
            if (mainContainer != null) {
                mainContainer.setCenter(cropPane);
            } else {
                showError("Error", "Could not find main container");
            }
        } catch (IOException e) {
            showError("Error", "Could not load crop view: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete(Farm farm, Pane card) {
        // Create confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Farm");
        alert.setContentText("Are you sure you want to delete the farm at " + farm.getAdress() + "?\nThis action cannot be undone.");

        // Show dialog and wait for response
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // User clicked OK, proceed with deletion
                farmService.delete(farm);
                
                // Show success message
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Farm Deleted");
                successAlert.setContentText("The farm has been successfully deleted.");
                successAlert.showAndWait();

                // Refresh the grid
                loadFarms();
            } catch (Exception ex) {
                showError("Delete Failed", "Failed to delete the farm: " + ex.getMessage());
            }
        }
    }

    private void handleModify(Farm farm) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/update.fxml"));
            Pane modifyForm = loader.load();

            // Pass the farm object to the modify form controller
            AddFarmController modifyController = loader.getController();
            modifyController.setFarm(farm);

            BorderPane mainContainer = getMainContainer();
            if (mainContainer != null) {
                // Replace the center content with the modify form
                mainContainer.setCenter(modifyForm);
            } else {
                showError("Error", "Could not find main container");
            }

        } catch (IOException e) {
            showError("Error", "Could not load modify farm form: " + e.getMessage());
        }
    }

    private void handleDetails(Farm farm, WeatherService.Weather first) {
        try {
            System.out.println("DEBUG: handleDetails called for farm: " + farm);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fielddisplay.fxml"));
            Pane detailsForm = loader.load();
            System.out.println("DEBUG: fielddisplay.fxml loaded");

            FieldController fieldController = loader.getController();
            System.out.println("DEBUG: FieldController obtained: " + fieldController);
            fieldController.loadField(farm, first);
            System.out.println("DEBUG: loadField called");

            BorderPane mainContainer = getMainContainer();
            if (mainContainer != null) {
                mainContainer.setCenter(detailsForm);
            } else {
                showError("Error", "Could not find main container");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not load farm details form: " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Error");
        errorAlert.setHeaderText(title);
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }

    @FXML
    public void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent homeView = loader.load();
            
            Scene currentScene = homeButton.getScene();
            Stage primaryStage = (Stage) currentScene.getWindow();
            primaryStage.setTitle("AgriFarm System");
            primaryStage.setScene(new Scene(homeView, 900, 600));
        } catch (IOException e) {
            showError("Error", "Could not navigate to home: " + e.getMessage());
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
        if (weather != null && weatherIcon != null) {
            try {
                String iconUrl = "http://openweathermap.org/img/wn/" + weather.getIcon() + "@2x.png";
                Image image = new Image(iconUrl);
                weatherIcon.setImage(image);
                weatherTemp.setText(String.format("%.1f°C", weather.getTemperature()));
                weatherDesc.setText(weather.getDescription());
            } catch (Exception e) {
                System.err.println("Error loading weather icon: " + e.getMessage());
                // Set a default icon or clear the current one
                weatherIcon.setImage(null);
            }
        }
    }
}