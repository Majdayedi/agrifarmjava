package controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;



/**
 * Controller for the disease detection view
 * Handles image upload, API integration, and result display
 */
public class DiseaseDetectionController {


    // FXML injected components
    @FXML private TextField imagePathField;
    @FXML private ImageView imagePreview;
    @FXML private Label noImageLabel;
    @FXML private Button detectButton;
    @FXML private HBox loadingBox;
    @FXML private VBox resultsContainer;
    @FXML private Label noResultsLabel;
    @FXML private ScrollPane resultsScrollPane;
    @FXML private Button helpButton;
    @FXML private TitledPane chatbotPane;
    @FXML private ListView<String> chatMessages;
    @FXML private TextField chatInput;
    private String currentDiseaseName;
    @FXML private HBox chatbotLoadingBox;


    // Class variables
    private File selectedImageFile;
    private final String API_URL = "https://plant.id/api/v3/health_assessment";
    private final String API_KEY = "bilyoReEgk6RYrvQEntEBmNuvcQH3qZXwFOdYUaYlPBmzGLqrc";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String GEMINI_API_KEY = "AIzaSyA9sfaCGC0UiiQR8VQp99Mybr11XgqDuvA";
    private String currentDiseaseDetails; // Store the full disease details from API

    /**
     * Initialize controller after FXML is loaded
     */
    @FXML
    public void initialize() {
        // Set initial state
        detectButton.setDisable(true);
        loadingBox.setVisible(false);
    }

    /**
     * Opens file browser to select an image
     */
    @FXML
    private void browseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Plant Image");

        // Set file extension filters
        FileChooser.ExtensionFilter imageFilter =
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().add(imageFilter);

        // Show dialog and get selected file
        Stage stage = (Stage) imagePathField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedImageFile = file;
            imagePathField.setText(file.getAbsolutePath());

            try {
                // Load and display the image preview
                Image image = new Image(file.toURI().toString());
                imagePreview.setImage(image);
                noImageLabel.setVisible(false);

                // Enable the detect button
                detectButton.setDisable(false);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Image Error",
                         "Failed to load the selected image: " + e.getMessage());
                imagePathField.setText("");
                detectButton.setDisable(true);
            }
        }
    }

    /**
     * Sends the image to Plant.id API for disease detection
     */
    @FXML
    private void detectDisease() {
        if (selectedImageFile == null || !selectedImageFile.exists()) {
            showAlert(Alert.AlertType.WARNING, "No Image Selected",
                     "Please select an image first.");
            return;
        }

        // Clear previous results
        resultsContainer.getChildren().clear();
        resultsContainer.getChildren().add(noResultsLabel);
        noResultsLabel.setVisible(false);

        // Show loading indicator
        loadingBox.setVisible(true);

        // Disable detect button while processing
        detectButton.setDisable(true);

        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return sendImageToAPI(selectedImageFile);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                // Hide loading indicator
                loadingBox.setVisible(false);

                // Process the results
                String response = task.getValue();
                processAPIResponse(response);

                // Re-enable detect button
                detectButton.setDisable(false);
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                // Hide loading indicator
                loadingBox.setVisible(false);

                // Show error message
                Throwable exception = task.getException();
                String errorMessage = "Failed to detect disease: " +
                    (exception != null ? exception.getMessage() : "Unknown error");

                showAlert(Alert.AlertType.ERROR, "API Error", errorMessage);

                noResultsLabel.setText("Error: " + errorMessage);
                noResultsLabel.setVisible(true);
                noResultsLabel.getStyleClass().clear();
                noResultsLabel.getStyleClass().add("disease-error-text");

                // Re-enable detect button
                detectButton.setDisable(false);
            });
        });

        // Run the API call in a background thread
        new Thread(task).start();
    }
    /**
     * Sends an image to the Plant.id API and returns the JSON response
     */
    private String sendImageToAPI(File imageFile) throws IOException {
        // Read the image file and encode it to base64
        byte[] fileContent = Files.readAllBytes(imageFile.toPath());
        String base64Image = Base64.getEncoder().encodeToString(fileContent);

        // Create the API request JSON
        JSONObject requestJson = new JSONObject();
        requestJson.put("api_key", API_KEY);

        // Add the images array with the base64 encoded image
        JSONArray imagesArray = new JSONArray();
        imagesArray.put(base64Image);
        requestJson.put("images", imagesArray);
        // Add API parameters

        requestJson.put("health", "all");
        requestJson.put("similar_images", true);


        // Add location parameters (optional)
        requestJson.put("latitude", 49.207);
        requestJson.put("longitude", 16.608);

        // Create the HTTP client and POST request
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_URL);
            httpPost.setHeader("Content-Type", "application/json");

            // Set the request body
            StringEntity entity = new StringEntity(requestJson.toString());
            httpPost.setEntity(entity);

            // Execute the request and get the response
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity responseEntity = response.getEntity();

                if (responseEntity != null) {
                    return EntityUtils.toString(responseEntity);
                } else {
                    throw new IOException("Empty response from API");
                }
            }
        }
    }

    /**
    /**
     * Processes and displays the API response
     */
    private void processAPIResponse(String jsonResponse) {
        try {
            if (!jsonResponse.trim().startsWith("{")) {
                throw new JSONException("Invalid JSON response: " + jsonResponse);
            }

            JSONObject response = new JSONObject(jsonResponse);

            // Check status first
            if (!response.has("status") || !response.getString("status").equals("COMPLETED")) {
                noResultsLabel.setText("Analysis not completed. Please try again.");
                noResultsLabel.setVisible(true);
                noResultsLabel.getStyleClass().clear();
                noResultsLabel.getStyleClass().add("disease-error-text");
                return;
            }

            if (!response.has("result")) {
                noResultsLabel.setText("No results found in the response");
                noResultsLabel.setVisible(true);
                noResultsLabel.getStyleClass().clear();
                noResultsLabel.getStyleClass().add("disease-error-text");
                return;
            }

            JSONObject result = response.getJSONObject("result");

            // Check if it's a plant first
            if (result.has("is_plant")) {
                JSONObject isPlant = result.getJSONObject("is_plant");
                if (!isPlant.getBoolean("binary")) {
                    noResultsLabel.setText("The image does not appear to be of a plant");
                    noResultsLabel.setVisible(true);
                    noResultsLabel.getStyleClass().clear();
                    noResultsLabel.getStyleClass().add("disease-warning-text");
                    return;
                }
            }

            // Check health status
            if (result.has("is_healthy")) {
                JSONObject isHealthy = result.getJSONObject("is_healthy");
                if (isHealthy.getBoolean("binary")) {
                    noResultsLabel.setText("The plant appears to be healthy!");
                    noResultsLabel.setVisible(true);
                    noResultsLabel.getStyleClass().clear();
                    noResultsLabel.getStyleClass().add("disease-success-text");
                    return;
                }
            }

            // Process disease suggestions
            if (result.has("disease")) {
                JSONObject disease = result.getJSONObject("disease");
                if (disease.has("suggestions")) {
                    JSONArray suggestions = disease.getJSONArray("suggestions");

                    if (suggestions.length() == 0) {
                        noResultsLabel.setText("No specific diseases detected, but the plant may need attention.");
                        noResultsLabel.setVisible(true);
                        noResultsLabel.getStyleClass().clear();
                        noResultsLabel.getStyleClass().add("disease-warning-text");
                        return;
                    }

                    // Clear existing results
                    resultsContainer.getChildren().clear();

                    // Add a title for the results section
                    Label resultsTitle = new Label("Most Likely Disease:");
                    resultsTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
                    resultsContainer.getChildren().add(resultsTitle);

                    // Find suggestion with highest probability
                    JSONObject highestProbSuggestion = suggestions.getJSONObject(0);
                    double highestProb = highestProbSuggestion.getDouble("probability");

                    for (int i = 1; i < suggestions.length(); i++) {
                        JSONObject suggestion = suggestions.getJSONObject(i);
                        if (suggestion.getDouble("probability") > highestProb) {
                            highestProb = suggestion.getDouble("probability");
                            highestProbSuggestion = suggestion;
                        }
                    }

                    // Display only the highest probability suggestion
                    VBox resultCard = createResultCard(highestProbSuggestion, 1);
                    resultsContainer.getChildren().add(resultCard);
                }
            }
            if (result.has("disease")) {
                JSONObject disease = result.getJSONObject("disease");
                if (disease.has("suggestions")) {
                    JSONArray suggestions = disease.getJSONArray("suggestions");
                    JSONObject highestProbSuggestion = suggestions.getJSONObject(0);

                    // Store the disease name and details for chatbot use
                    currentDiseaseName = highestProbSuggestion.optString("name", "Unknown Disease");
                    currentDiseaseDetails = highestProbSuggestion.toString(2); // Pretty-printed JSON

                    // Enable the help button
                    Platform.runLater(() -> helpButton.setDisable(false));

                    // ... rest of your existing code ...
                }
            }
        } catch (JSONException e) {
            noResultsLabel.setText("Error parsing API response: " + e.getMessage());
            noResultsLabel.setVisible(true);
            noResultsLabel.getStyleClass().clear();
            noResultsLabel.getStyleClass().add("disease-error-text");
            resultsContainer.getChildren().clear();
            resultsContainer.getChildren().add(noResultsLabel);
        }
    }
    /**
     * Creates a card UI for displaying a disease result
     */
    private VBox createResultCard(JSONObject suggestion, int index) {
        VBox card = new VBox(10);
        card.getStyleClass().add("disease-result-item");

        // Get basic details from the suggestion
        String name = suggestion.optString("name", "Unknown Disease");
        double probability = suggestion.optDouble("probability", 0) * 100;

        // Create header with name and probability
        Label nameText = new Label(index + ". " + name);
        nameText.getStyleClass().add("disease-result-title");

        Label probabilityLabel = new Label(String.format("Confidence: %.1f%%", probability));
        probabilityLabel.getStyleClass().add("disease-result-probability");

        // Add appropriate confidence style class based on probability level
        if (probability > 70) {
            probabilityLabel.getStyleClass().add("disease-confidence-high");
        } else if (probability > 50) {
            probabilityLabel.getStyleClass().add("disease-confidence-medium");
        } else {
            probabilityLabel.getStyleClass().add("disease-confidence-low");
        }

        // Add the header labels to the card
        card.getChildren().addAll(nameText, probabilityLabel);

        // Get detailed information if available
        if (suggestion.has("details")) {
            try {
                JSONObject details = suggestion.getJSONObject("details");

                // Add description if available
                if (details.has("description")) {
                    addSection(card, "Description", details.getString("description"));
                }

                // Add cause if available
                if (details.has("cause")) {
                    addSection(card, "Cause", details.getString("cause"));
                }

                // Add common names if available
                if (details.has("common_names")) {
                    JSONArray commonNames = details.getJSONArray("common_names");
                    StringBuilder namesStr = new StringBuilder();
                    for (int i = 0; i < commonNames.length(); i++) {
                        if (i > 0) namesStr.append(", ");
                        namesStr.append(commonNames.getString(i));
                    }
                    addSection(card, "Common Names", namesStr.toString());
                }

                // Add treatment information if available
                if (details.has("treatment")) {
                    JSONObject treatment = details.getJSONObject("treatment");

                    // Add each treatment section if available
                    if (treatment.has("prevention")) {
                        addSection(card, "Prevention", treatment.getString("prevention"));
                    }
                    if (treatment.has("biological")) {
                        addSection(card, "Biological Treatment", treatment.getString("biological"));
                    }
                    if (treatment.has("chemical")) {
                        addSection(card, "Chemical Treatment", treatment.getString("chemical"));
                    }
                    // Add any general treatment information
                    if (treatment.has("general")) {
                        addSection(card, "General Treatment", treatment.getString("general"));
                    }
                }

                // Add URL if available
                if (details.has("url")) {
                    addSection(card, "More Information", details.getString("url"));
                }

            } catch (JSONException e) {
                Label errorLabel = new Label("Error parsing disease details: " + e.getMessage());
                errorLabel.getStyleClass().addAll("disease-error-text", "disease-message-text");
                errorLabel.setWrapText(true);
                card.getChildren().add(errorLabel);
            }
        }

        if (card.getChildren().size() <= 2) {
            Label noDetailsLabel = new Label("No detailed information available for this disease.");
            noDetailsLabel.getStyleClass().addAll("disease-info-text", "disease-message-text");
            noDetailsLabel.setWrapText(true);
            card.getChildren().add(noDetailsLabel);
        }

        return card;
    }

    /**
     * Adds a section to the result card with a title and content
     * @param parent The parent VBox to add the section to
     * @param title The section title
     * @param content The section content text
     */




























    private void addSection(VBox parent, String title, String content) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }

        Label titleText = new Label(title);
        titleText.getStyleClass().add("disease-section-title");

        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("disease-result-description");

        VBox section = new VBox(5, titleText, contentLabel);
        section.getStyleClass().add("disease-section-container");

        parent.getChildren().add(section);
    }

    /**
     * Navigate back to the Crops view
     */
    @FXML
    private void backToCrops() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/crop.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) imagePathField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Crop Management");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                     "Error returning to Crops view: " + e.getMessage());
        }
    }

    /**
     * Shows an alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    /**
     * Shows the chatbot pane when help is requested
     */
    @FXML
    private void showChatbot() {
        if (chatbotPane != null) {
            chatbotPane.setExpanded(true); // Force open
        }

        // Clear previous messages if you want fresh start
        chatMessages.getItems().clear();

        // Add a welcome message
        if (currentDiseaseName != null && !currentDiseaseName.isEmpty()) {
            addChatMessage("Bot", "Hello! I'm your crop health assistant. I can help you with treatment information for: " +
                    currentDiseaseName + ".\nWhat would you like to know?");
        } else {
            addChatMessage("Bot", "Hello! Please detect a disease first, then I can assist you with treatment information.");
        }
    }


    /**
     * Handles user messages in the chatbot using Gemini API
     */


    /**
     * Calls the Gemini API with the user's question
     */
    private String askGemini(String userMessage) throws IOException {
        // Create the request JSON
        JSONObject requestJson = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();

        // Craft a detailed prompt including the disease information
        String prompt = "You are an agricultural expert helping with plant disease treatment. " +
                "The user has a plant with the following disease: " + currentDiseaseName + "\n\n" +
                "Disease details from our system:\n" + currentDiseaseDetails + "\n\n" +
                "User question: " + userMessage + "\n\n" +
                "Please provide detailed, practical treatment advice suitable for home gardeners. " +
                "If the question is not related to plant disease treatment, politely explain that " +
                "you specialize in plant disease treatment.";

        part.put("text", prompt);
        parts.put(part);
        content.put("parts", parts);
        contents.put(content);
        requestJson.put("contents", contents);

        // Create HTTP client and request
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(GEMINI_API_URL + "?key=" + GEMINI_API_KEY);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(requestJson.toString()));

        // Execute the request
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String responseString = EntityUtils.toString(response.getEntity());
            JSONObject jsonResponse = new JSONObject(responseString);

            // Parse the Gemini response
            JSONArray candidates = jsonResponse.getJSONArray("candidates");
            JSONObject firstCandidate = candidates.getJSONObject(0);
            JSONObject contentResponse = firstCandidate.getJSONObject("content");
            JSONArray partsResponse = contentResponse.getJSONArray("parts");
            return partsResponse.getJSONObject(0).getString("text");
        }
    }
    private void addChatMessage(String sender, String message) {
        chatMessages.getItems().add(sender + ": " + message);
    }

    // Add this field

    // Update handleChatMessage
    @FXML
    private void handleChatMessage() {
        String userMessage = chatInput.getText().trim();
        if (userMessage.isEmpty()) return;

        addChatMessage("You", userMessage);
        chatInput.clear();
        chatInput.setDisable(true);
        chatbotLoadingBox.setVisible(true);

        Task<String> geminiTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return askGemini(userMessage);
            }
        };

        geminiTask.setOnSucceeded(e -> {
            String response = geminiTask.getValue();
            addChatMessage("Bot", response);
            chatInput.setDisable(false);
            chatbotLoadingBox.setVisible(false);
        });

        geminiTask.setOnFailed(e -> {
            addChatMessage("Bot", "Sorry, I encountered an error. Please try again later.");
            chatInput.setDisable(false);
            chatbotLoadingBox.setVisible(false);
        });

        new Thread(geminiTask).start();
    }
}

