package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;

public class HuggingFaceSummarizationService {
    private static final Logger logger = Logger.getLogger(HuggingFaceSummarizationService.class.getName());
    private static final String API_URL = "https://api-inference.huggingface.co/models/facebook/bart-large-cnn";
    private static final int TIMEOUT_MS = 2000; // 60 seconds timeout
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000; // 5 seconds between retries
    private final String apiKey;
    private final Map<String, String> cache;

    public HuggingFaceSummarizationService(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty");
        }
        this.apiKey = apiKey;
        this.cache = new ConcurrentHashMap<>();
    }

    public String summarize(String text) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            return "No text provided for summarization";
        }

        // Check cache first
        String cacheKey = text.trim();
        if (cache.containsKey(cacheKey)) {
            logger.info("Returning cached summary");
            return cache.get(cacheKey);
        }

        IOException lastException = null;
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                if (attempt > 0) {
                    logger.info("Retry attempt " + (attempt + 1) + " of " + MAX_RETRIES);
                    TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS);
                }

                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                conn.setDoOutput(true);

                String payload = "{\"inputs\": \"" + escapeJson(text) + "\", \"parameters\": {\"max_length\": 130, \"min_length\": 30}}";
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            response.append(line);
                        }
                        String json = response.toString();

                        // Handle array response
                        json = json.replaceAll("^\\[|\\]$", "").trim();
                        if (json.isEmpty()) {
                            continue; // Retry if empty response
                        }

                        // Extract summary_text using improved parsing
                        int startIndex = json.indexOf("\"summary_text\":\"");
                        if (startIndex != -1) {
                            startIndex += 15; // Length of "summary_text":"
                            int endIndex = json.indexOf("\"", startIndex);
                            if (endIndex > startIndex) {
                                String summary = json.substring(startIndex, endIndex)
                                        .replace("\\n", " ")
                                        .replace("\\r", " ")
                                        .replaceAll("\\s+", " ")
                                        .trim();
                                if (!summary.isEmpty()) {
                                    cache.put(cacheKey, summary);
                                    return summary;
                                }
                            }
                        }
                        logger.warning("Failed to parse summary from response: " + json);
                    }
                } else if (responseCode == 503) {
                    logger.info("Model is loading, will retry in " + RETRY_DELAY_MS + "ms");
                    continue; // Retry on 503
                } else {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            response.append(line);
                        }
                        String error = response.toString();
                        logger.warning("API Error (" + responseCode + "): " + error);
                        lastException = new IOException("API Error: " + error);
                    }
                }
            } catch (IOException e) {
                logger.warning("Error during API call (attempt " + (attempt + 1) + "): " + e.getMessage());
                lastException = e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Summarization interrupted", e);
            }
        }

        if (lastException != null) {
            throw new IOException("Failed to get summary after " + MAX_RETRIES + " attempts: " + lastException.getMessage(), lastException);
        }
        return "Failed to generate summary after " + MAX_RETRIES + " attempts. Please try again later.";
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}