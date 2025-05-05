package service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import entite.Farm;
import org.json.JSONObject;
import org.json.JSONArray;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WeatherService {
    private static final Logger LOGGER = Logger.getLogger(WeatherService.class.getName());
    private static String API_KEY;
    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast";
    private Farm currentFarm;
    private static boolean isInitialized = false;

    private static void initializeIfNeeded() {
        if (!isInitialized) {
            try {
                Properties props = new Properties();
                InputStream input = WeatherService.class.getClassLoader().getResourceAsStream("config.properties");
                if (input != null) {
                    props.load(input);
                    API_KEY = props.getProperty("openweathermap.api.key");
                    if (API_KEY == null || API_KEY.trim().isEmpty()) {
                        LOGGER.severe("OpenWeatherMap API key is not configured in config.properties");
                        return;
                    }
                    isInitialized = true;
                } else {
                    LOGGER.severe("Unable to find config.properties");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error loading OpenWeatherMap configuration", e);
            }
        }
    }

    public static class Weather {
        private LocalDateTime dateTime;
        private double temperature;
        private double feelsLike;
        private int humidity;
        private String description;
        private String icon;
        private double windSpeed;
        private int clouds;
        public Weather(LocalDateTime dateTime, double temperature, double feelsLike,
                      int humidity, String description, String icon, 
                      double windSpeed, int clouds) {
            this.dateTime = dateTime;
            this.temperature = temperature;
            this.feelsLike = feelsLike;
            this.humidity = humidity;
            this.description = description;
            this.icon = icon;
            this.windSpeed = windSpeed;
            this.clouds = clouds;
        }

        // Getters
        public LocalDateTime getDateTime() { return dateTime; }
        public double getTemperature() { return temperature; }
        public double getFeelsLike() { return feelsLike; }
        public int getHumidity() { return humidity; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
        public double getWindSpeed() { return windSpeed; }
        public int getClouds() { return clouds; }

        @Override
        public String toString() {
            return String.format(
                "Time: %s\nTemperature: %.1f°C\nFeels like: %.1f°C\nHumidity: %d%%\n" +
                "Weather: %s\nIcon: %s\nWind Speed: %.1f m/s\nClouds: %d%%",
                dateTime.toLocalTime(), temperature, feelsLike, humidity,
                description, icon, windSpeed, clouds
            );
        }
    }

    public void setCurrentFarm(Farm currentFarm) {
        this.currentFarm = currentFarm;
    }

    public static void main(String[] args) {
        try {
            // Test coordinates (Tunis, Tunisia)
            double lat = 36.8065;
            double lon = 10.1815;

            
            String weatherData = getWeatherData(lat, lon);
            
            // Get list of weather forecasts
            List<Weather> forecasts = getWeatherForecasts(weatherData);
            
            // Display forecasts
            displayForecasts(forecasts);
            
        } catch (Exception e) {
            System.err.println("Error testing weather API: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String getWeatherData(double lat, double lon) throws Exception {
        initializeIfNeeded();
        if (!isInitialized) {
            throw new Exception("Weather service is not properly initialized");
        }

        // Build the API URL with parameters
        String url = String.format("%s?lat=%f&lon=%f&appid=%s&units=metric", BASE_URL, lat, lon, API_KEY);
        
        // Create HTTP client
        HttpClient client = HttpClient.newHttpClient();
        
        // Create HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();
        
        // Send request and get response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Check if request was successful
        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new Exception("Failed to get weather data. Status code: " + response.statusCode());
        }
    }

    public static List<Weather> getWeatherForecasts(String weatherData) {
        List<Weather> forecasts = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        try {
            JSONObject json = new JSONObject(weatherData);
            JSONArray list = json.getJSONArray("list");
            
            for (int i = 0; i < list.length(); i++) {
                JSONObject item = list.getJSONObject(i);
                
                // Parse date and time
                String dtTxt = item.getString("dt_txt");
                LocalDateTime dateTime = LocalDateTime.parse(dtTxt, formatter);
                
                // Extract weather information
                JSONObject main = item.getJSONObject("main");
                JSONObject weather = item.getJSONArray("weather").getJSONObject(0);
                JSONObject wind = item.getJSONObject("wind");
                JSONObject clouds = item.getJSONObject("clouds");
                
                // Create Weather object
                Weather forecast = new Weather(
                    dateTime,
                    main.getDouble("temp"),
                    main.getDouble("feels_like"),
                    main.getInt("humidity"),
                    weather.getString("description"),
                    weather.getString("icon"),
                    wind.getDouble("speed"),
                    clouds.getInt("all")
                );
                
                forecasts.add(forecast);
            }
        } catch (Exception e) {
            System.err.println("Error parsing forecast data: " + e.getMessage());
            e.printStackTrace();
        }
        
        return forecasts;
    }

    public static String getFirstWeatherDescription(String weatherData) {
        try {
            JSONObject json = new JSONObject(weatherData);
            JSONArray list = json.getJSONArray("list");
            if (list.length() > 0) {
                JSONObject firstEntry = list.getJSONObject(0);
                JSONArray weatherArray = firstEntry.getJSONArray("weather");
                if (weatherArray.length() > 0) {
                    return weatherArray.getJSONObject(0).getString("description");
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting first weather description: " + e.getMessage());
        }
        return "";
    }

    public void now() {


    }

    private static void displayForecasts(List<Weather> forecasts) {
        System.out.println("\n5-Day Weather Forecast for Tunis, Tunisia");
        System.out.println("=========================================");
        
        String currentDate = "";
        for (Weather forecast : forecasts) {
            String date = forecast.getDateTime().toLocalDate().toString();
            
            // Print date header if it's a new day
            if (!date.equals(currentDate)) {
                System.out.println("\n" + date);
                System.out.println("-------------------");
                currentDate = date;
            }
            
            // Print weather details
            System.out.println(forecast);
            System.out.println("-------------------");
        }
    }

} 