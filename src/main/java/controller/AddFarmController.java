package controller;

import entite.Farm;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import service.CommandeService;
import service.FarmService;
import org.json.JSONArray;
import org.json.JSONObject;
import netscape.javascript.JSObject;
import entite.Field;
import service.FieldService;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class AddFarmController {
    @FXML private TextField nameField;
    @FXML private TextField surfaceField;
    @FXML private TextField addressField;
    @FXML private TextField budgetField;
    @FXML private TextField weatherField;
    @FXML private TextField locationField;
    @FXML private TextField longitudeField;
    @FXML private TextField latitudeField;
    @FXML private TextArea descriptionField;
    @FXML private WebView webView;
    @FXML private TextField searchField;
    @FXML private Label locationLabel;

    @FXML private CheckBox bircheck;
    @FXML private CheckBox irrigationCheck;
    @FXML private CheckBox photoCheck;
    @FXML private CheckBox fence;
    @FXML private CheckBox cabincheck;

    private final FarmService farmService = new FarmService();
    private Farm currentFarm;
    private WebEngine webEngine;
    private double currentLat = 0;
    private double currentLon = 0;
    private FieldService fieldservice= new FieldService();

    @FXML
    public void initialize() {
        System.out.println("Initializing AddFarmController"); // Debug log
        webEngine = webView.getEngine();

        // Load the map HTML
        String mapHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Farm Location Map</title>
                    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
                    <style>
                        html, body {
                            height: 100%;
                            width: 100%;
                            margin: 0;
                            padding: 0;
                        }
                        #map {
                            height: 100%;
                            width: 100%;
                        }
                    </style>
                </head>
                <body>
                    <div id="map"></div>
                    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                    <script>
                        let map;
                        let marker;
                
                        // Initialize the map
                        function initMap() {
                            if (!map) {
                                map = L.map('map').setView([36.8065, 10.1815], 6); // Set default view
                                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                                    attribution: '© OpenStreetMap contributors'
                                }).addTo(map);
                
                                // Add a marker for the initial location (optional)
                
                                // Add click listener for the map
                              // Instead of calling reverse geocoding every time, check if the location is cached first
                                              let locationCache = {}; // Simple cache for demonstration
                
                                              map.on('click', function(e) {
                                                  var lat = e.latlng.lat;
                                                  var lng = e.latlng.lng;
                                                 \s
                                                  if (locationCache[lat + "," + lng]) {
                                                      const displayName = locationCache[lat + "," + lng];
                                                      updateMarkerPosition(lat, lng);
                                                      if (window.javaConnector && window.javaConnector.onLocationSelected) {
                                                          window.javaConnector.onLocationSelected(lat, lng, displayName);
                                                      }
                                                  } else {
                                                      const url = `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`;
                                                     \s
                                                      fetch(url)
                                                          .then(response => response.json())
                                                          .then(data => {
                                                              const displayName = data.display_name;
                                                              console.log('Clicked location: ', lat, lng, displayName);
                                                              locationCache[lat + "," + lng] = displayName; // Cache the result
                                                              updateMarkerPosition(lat, lng);
                                                              if (window.javaConnector && window.javaConnector.onLocationSelected) {
                                                                  window.javaConnector.onLocationSelected(lat, lng, displayName);
                                                              }
                                                          })
                                                          .catch(error => console.error('Error fetching location:', error));
                                                  }
                                              });
                
                
                            }
                        }
                        moveToLocation = function(lat, lon) {
                            if (!map) initMap(); // Ensure the map is initialized
                            map.setView([lon, lat], 13); // Move the map to the new location
                            updateMarkerPosition(lat, lon); // Update marker position
                        };
                        // Update the marker position
                        function updateMarkerPosition(lat, lng) {
                            if (!map) initMap(); // Ensure the map is initialized
                            if (marker) {
                                marker.setLatLng([lat, lng]); // Update marker position
                            } else {
                                marker = L.marker([lat, lng]).addTo(map); // Add new marker if it doesn't exist
                            }
                           
                        }
                
                        // Make sure the map initializes when the page loads
                        window.onload = initMap;
                
                        // Expose the update function for JavaScript call
                        window.updateMarkerPosition = updateMarkerPosition;
                
                    </script>
                </body>
                </html>
                
            """;

        System.out.println("Loading map HTML"); // Debug log
        webEngine.loadContent(mapHtml);
        webEngine.setOnAlert(event -> System.out.println("JS Alert: " + event.getData()));
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                System.out.println("Map loaded successfully"); // Debug log
                webEngine.executeScript("initMap();");
            }
        });
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                JavaConnector connector = new JavaConnector(locationField, latitudeField, longitudeField);
                window.setMember("javaConnector", connector);

                // Optional: Log from JS to make sure it's connected
                webEngine.executeScript("console.log('JavaConnector set')");
            }
        });

    }

    public void handleUpdate(ActionEvent actionEvent) {
            try {
                Farm farm = currentFarm != null ? currentFarm : new Farm();
                farm.setName(nameField.getText());
                farm.setSurface(Float.parseFloat(surfaceField.getText()));
                farm.setAdress(addressField.getText());
                farm.setBudget(Float.parseFloat(budgetField.getText()));
                farm.setWeather(" ");
                farm.setLocation(locationField.getText());
                farm.setLat((float) currentLat);
                farm.setLon((float) currentLon);
                farm.setDescription(descriptionField.getText());
                farm.setBir(bircheck.isSelected());
                farm.setIrrigation(irrigationCheck.isSelected());
                farm.setPhotovoltaic(photoCheck.isSelected());
                farm.setFence(fence.isSelected());
                farm.setCabin(cabincheck.isSelected());

                if (currentFarm != null) {
                    farmService.update(farm);
                }

                refreshMainView();
            } catch (NumberFormatException e) {
                showError("Input Error", "Please enter valid numbers for numeric fields");
            } catch (Exception e) {
                showError("Error", "Could not save farm: " + e.getMessage());
                System.out.println("Could not save farm: " + e.getMessage());;
            }
        }



    public class JavaConnector {
        private final TextField locationField;
        private final TextField latitudeField;
        private final TextField longitudeField;

        public JavaConnector(TextField locationField, TextField latitudeField, TextField longitudeField) {
            this.locationField = locationField;
            this.latitudeField = latitudeField;
            this.longitudeField = longitudeField;
        }

        public void onLocationSelected(double lat, double lon, String displayName) {
            System.out.println("Clicked coordinates: " + lat + ", " + lon);

            Platform.runLater(() -> {
                latitudeField.setText(String.format(Locale.US, "%.6f", lat));
                longitudeField.setText(String.format(Locale.US, "%.6f", lon));
                locationField.setText(displayName); // You can replace this with proper reverse geocode
            });
        }
    }



    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            return;
        }

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String searchUrl = String.format(
                "https://nominatim.openstreetmap.org/search?format=json&q=%s&limit=1",
                encodedQuery
        );

        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(searchUrl);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "JavaFX Farm Management App");

                if (conn.getResponseCode() == 200) {
                    String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    JSONArray results = new JSONArray(response);

                    if (results.length() > 0) {
                        JSONObject result = results.getJSONObject(0);
                        double lat = result.getDouble("lat");
                        double lon = result.getDouble("lon");

                        Platform.runLater(() -> {
                            // Only move the map to the searched location
                            webEngine.executeScript(String.format(
                                    "moveToLocation(%f, %f);",
                                    lat, lon
                            ));
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void setFarm(Farm farm) {
        this.currentFarm = farm;
        if (farm != null) {
        nameField.setText(farm.getName());
            surfaceField.setText(String.valueOf(farm.getSurface()));
        addressField.setText(farm.getAdress());
        budgetField.setText(String.valueOf(farm.getBudget()));
            locationField.setText(farm.getLocation());
            longitudeField.setText(String.valueOf(farm.getLon()));
            latitudeField.setText(String.valueOf(farm.getLat()));
        descriptionField.setText(farm.getDescription());
        bircheck.setSelected(farm.isBir());
        irrigationCheck.setSelected(farm.isIrrigation());
        photoCheck.setSelected(farm.isPhotovoltaic());
        fence.setSelected(farm.isFence());
        cabincheck.setSelected(farm.isCabin());

            // Update map marker
            if (farm.getLat() != 0 && farm.getLon() != 0) {
                currentLat = farm.getLat();
                currentLon = farm.getLon();
                updateMapMarker(currentLat, currentLon);
            }
        }
    }
    private void updateMapMarker(double lat, double lon) {
        if (webEngine != null) {
            try {
                webEngine.executeScript(String.format(
                        "if(typeof updateMarkerPosition === 'function') { updateMarkerPosition(%f, %f); }",
                        lat, lon
                ));
                currentLat = lat;
                currentLon = lon;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    private void handleSave() {
        try {
            Farm farm = currentFarm != null ? currentFarm : new Farm();
            farm.setName(nameField.getText());
            farm.setSurface(Float.parseFloat(surfaceField.getText()));
            farm.setAdress(addressField.getText());
            farm.setBudget(Float.parseFloat(budgetField.getText()));
            farm.setWeather(" ");
            farm.setLocation(locationField.getText());
            farm.setLat((float) currentLat);
            farm.setLon((float) currentLon);
            farm.setDescription(descriptionField.getText());
            farm.setBir(bircheck.isSelected());
            farm.setIrrigation(irrigationCheck.isSelected());
            farm.setPhotovoltaic(photoCheck.isSelected());
            farm.setFence(fence.isSelected());
            farm.setCabin(cabincheck.isSelected());

            if (currentFarm != null) {
                farmService.update(farm);
            } else {
            farmService.create(farm);
            Field field = new Field(farm, 0, "Main field", 0.0, 0, 0, 0, "",null);
            fieldservice.create(field);

            }

            refreshMainView();
        } catch (NumberFormatException e) {
            showError("Input Error", "Please enter valid numbers for numeric fields");
        } catch (Exception e) {
            showError("Error", "Could not save farm: " + e.getMessage());
            System.out.println("Could not save farm: " + e.getMessage());;
        }
    }

    @FXML
    private void handleCancel() {
        refreshMainView();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void refreshMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/farmdisplay.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Error", "Could not refresh view: " + e.getMessage());
        }
    }
}