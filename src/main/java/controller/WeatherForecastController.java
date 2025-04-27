package controller;

import entite.Farm;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import service.FarmService;
import service.WeatherService;
import service.WeatherService.Weather;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.geometry.Insets;

public class WeatherForecastController {
    @FXML private Label warningLabel;
    @FXML private HBox forecastCards;
    @FXML private VBox detailsDashboard;
    @FXML private Label detailsDate;
    @FXML private LineChart<String, Number> temperatureChart;
    @FXML private GridPane detailsGrid;
    @FXML private Label noDataLabel;
    @FXML private VBox rootVBox;

    private Map<String, List<Weather>> groupedForecast;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM d");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public Farm currentFarm;
    private final FarmService farmService = new FarmService();
    private WeatherService.Weather weather;
    public void setFirst(WeatherService.Weather first) {
        this.weather= first;
    }
    @FXML
    public void initialize(Farm farm) {
        try {
            // Remove or minimize outer padding
            if (rootVBox != null) {
                rootVBox.setPadding(new Insets(0, 0, 0, 0));
            }

            this.currentFarm = farm;
            double lat = currentFarm.getLat();
            double lon = currentFarm.getLon();
            String weatherData = WeatherService.getWeatherData(lat, lon);
            // Debug: print the full API response
            System.out.println("Full OpenWeatherMap API response:\n" + weatherData);
            List<Weather> forecasts = WeatherService.getWeatherForecasts(weatherData);
            now(forecasts,currentFarm);
        groupedForecast = new LinkedHashMap<>();
            for (Weather forecast : forecasts) {
                String date = forecast.getDateTime().toLocalDate().toString();
                groupedForecast.computeIfAbsent(date, k -> new ArrayList<>()).add(forecast);
            }
            
            // Show alert with weather attribute (current temp and description)
            if (!forecasts.isEmpty() && warningLabel != null) {
                Weather first = forecasts.get(0);
                warningLabel.setText(String.format("Current: %.1f°C, %s", first.getTemperature(), first.getDescription()));
                warningLabel.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-padding: 8; -fx-background-radius: 5;");
            }
            
            // Display forecasts
            displayForecasts();
            
        } catch (Exception e) {
            if (warningLabel != null) {
                warningLabel.setText("Error loading weather data: " + e.getMessage());
                warningLabel.setStyle("-fx-background-color: rgba(255,0,0,0.1);");
            }
            e.printStackTrace();
        }
    }
    public void now(List<Weather> forecasts,Farm farm) {
        if (!forecasts.isEmpty()) {
            Weather first = forecasts.get(0);
            farm.setWeather(first.getDescription());
            farmService.updateWeather(farm);
        }
    }
    private void displayForecasts() {
        if (forecastCards == null || groupedForecast == null || groupedForecast.isEmpty()) {
            if (noDataLabel != null) {
                noDataLabel.setVisible(true);
            }
            return;
        }

        forecastCards.getChildren().clear();
        int index = 1;
        for (Map.Entry<String, List<Weather>> entry : groupedForecast.entrySet()) {
            String date = entry.getKey();
            List<Weather> forecasts = entry.getValue();
            
            if (!forecasts.isEmpty()) {
                // Create card for each day
                VBox card = createForecastCard(date, forecasts.get(0), index);
                forecastCards.getChildren().add(card);
                index++;
            }
        }
    }

    private VBox createForecastCard(String date, Weather forecast, int index) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.09); -fx-background-radius: 8; -fx-padding: 10; -fx-min-width: 120; -fx-max-width: 140;");
        card.setPrefWidth(175);
        card.setMaxWidth(175);
        
        // Date header
        Label dateLabel = new Label(LocalDate.parse(date).format(DATE_FORMATTER));
        dateLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13;");
        
        // Weather icon
        ImageView iconView = new ImageView();
        iconView.setFitWidth(28);
        iconView.setFitHeight(28);
        try {
            Image image = new Image("http://openweathermap.org/img/wn/" + forecast.getIcon() + "@2x.png");
            iconView.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading weather icon: " + e.getMessage());
        }
        
        // Temperature
        Label tempLabel = new Label(String.format("%.1f°C", forecast.getTemperature()));
        tempLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15;");
        
        // Description
        Label descLabel = new Label(forecast.getDescription());
        descLabel.setStyle("-fx-text-fill: white; -fx-opacity: 0.8; -fx-font-size: 11;");
        
        card.getChildren().addAll(dateLabel, iconView, tempLabel, descLabel);
        
        // Add click handler
        card.setOnMouseClicked(e -> showDetails(index));
        
        return card;
    }

    @FXML
    private void showDetails(int index) {
        if (groupedForecast == null || groupedForecast.isEmpty() || detailsDashboard == null) {
            return;
        }

        List<String> dates = new ArrayList<>(groupedForecast.keySet());
        if (index <= 0 || index > dates.size()) {
            return;
        }

        String selectedDate = dates.get(index - 1);
        List<Weather> forecasts = groupedForecast.get(selectedDate);
        
        if (forecasts == null || forecasts.isEmpty()) {
            return;
        }

        // Update date label
        if (detailsDate != null) {
            detailsDate.setText(LocalDate.parse(selectedDate).format(DATE_FORMATTER));
        }
        
        // Clear previous details
        if (detailsGrid != null) {
            detailsGrid.getChildren().clear();
        }
        
        // Add temperature chart data
        if (temperatureChart != null) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Temperature");
            
            int row = 0;
            for (Weather forecast : forecasts) {
                // Debug print
                System.out.println("Time: " + forecast.getDateTime() + " Temp: " + forecast.getTemperature());
                // Add to chart
                series.getData().add(new XYChart.Data<>(
                    forecast.getDateTime().format(TIME_FORMATTER),
                    forecast.getTemperature()
                ));
                // Add detailed info (8 per row)
                if (detailsGrid != null) {
                    VBox detailBox = createDetailBox(forecast);
                    detailsGrid.add(detailBox, row % 8, row / 8);
                }
                row++;
            }
            
            temperatureChart.getData().clear();
            temperatureChart.getData().add(series);
        }
        
        // Show dashboard (do NOT hide forecast cards)
        detailsDashboard.setVisible(true);
    }

    private VBox createDetailBox(Weather forecast) {
        VBox box = new VBox(7); // Smaller spacing
        box.setStyle("-fx-background-color: rgba(255,255,255,0.13); -fx-background-radius: 7; -fx-padding: 10; -fx-min-width: 110; -fx-max-width: 140;");

        // Add weather icon
        ImageView iconView = new ImageView();
        iconView.setFitWidth(30);
        iconView.setFitHeight(22);
        try {
            Image image = new Image("http://openweathermap.org/img/wn/" + forecast.getIcon() + "@2x.png");
            iconView.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading weather icon: " + e.getMessage());
        }

        Label timeLabel = new Label(forecast.getDateTime().format(TIME_FORMATTER));
        timeLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12;");

        Label humidityLabel = new Label(String.format("Humidity: %d%%", forecast.getHumidity()));
        Label windLabel = new Label(String.format("Wind: %.1f m/s", forecast.getWindSpeed()));
        Label feelsLikeLabel = new Label(String.format("Feels like: %.1f°C", forecast.getFeelsLike()));
        Label cloudsLabel = new Label(String.format("Clouds: %d%%", forecast.getClouds()));

        for (Label label : Arrays.asList(humidityLabel, windLabel, feelsLikeLabel, cloudsLabel)) {
            label.setStyle("-fx-text-fill: white; -fx-font-size: 11;");
        }

        box.getChildren().addAll(iconView, timeLabel, humidityLabel, windLabel, feelsLikeLabel, cloudsLabel);
        return box;
    }

    @FXML
    private void hideDetails() {
        if (detailsDashboard != null) {
            detailsDashboard.setVisible(false);
        }
    }
} 