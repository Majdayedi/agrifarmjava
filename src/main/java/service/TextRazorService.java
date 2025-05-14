package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TextRazorService {
    private static final String API_URL = "https://api.textrazor.com/";
    private final String apiKey;

    public TextRazorService(String apiKey) {
        this.apiKey = apiKey;
    }

    public List<String> generateTags(String text) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("x-textrazor-key", apiKey);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        // Encode text and parameters properly
        String encodedText = URLEncoder.encode(text.trim(), StandardCharsets.UTF_8.toString());
        String params = "extractors=topics,entities&text=" + encodedText;

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = params.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        Set<String> tags = new HashSet<>();

        if (conn.getResponseCode() == 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                // Parse JSON response
                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

                // Extract topics
                if (jsonResponse.has("response") && jsonResponse.getAsJsonObject("response").has("topics")) {
                    JsonArray topics = jsonResponse.getAsJsonObject("response").getAsJsonArray("topics");
                    for (JsonElement topic : topics) {
                        JsonObject topicObj = topic.getAsJsonObject();
                        if (topicObj.has("label")) {
                            String label = topicObj.get("label").getAsString().toLowerCase();
                            // Filter out very long tags
                            if (label.length() <= 30) {
                                tags.add(label);
                            }
                        }
                    }
                }

                // Extract entities
                if (jsonResponse.has("response") && jsonResponse.getAsJsonObject("response").has("entities")) {
                    JsonArray entities = jsonResponse.getAsJsonObject("response").getAsJsonArray("entities");
                    for (JsonElement entity : entities) {
                        JsonObject entityObj = entity.getAsJsonObject();
                        if (entityObj.has("entityId")) {
                            String entityId = entityObj.get("entityId").getAsString().toLowerCase();
                            // Filter out very long tags and Wikipedia URLs
                            if (entityId.length() <= 30 && !entityId.startsWith("http")) {
                                tags.add(entityId);
                            }
                        }
                    }
                }
            }
        } else {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                throw new IOException("TextRazor API error: " + response.toString());
            }
        }

        // Convert to list and limit to top 10 tags
        List<String> tagList = new ArrayList<>(tags);
        return tagList.subList(0, Math.min(tags.size(), 10));
    }
}