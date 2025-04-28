package service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entite.CropInfo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CropInfoService {
    private static final String BASE_URL = "https://api-cropcalendar.apps.fao.org/api/v1";
    private static final Logger logger = Logger.getLogger(CropInfoService.class.getName());
    private final HttpClient httpClient;
    private final Gson gson;

    public CropInfoService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public List<CropInfo> getCropsForTunisia() {
        String apiUrl = String.format("%s/countries/TN/crops?language=en", BASE_URL);
        List<CropInfo> crops = new ArrayList<>();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.warning("Failed to fetch crop info: HTTP " + response.statusCode());
                return crops;
            }

            JsonArray cropsArray = gson.fromJson(response.body(), JsonArray.class);

            for (int i = 0; i < cropsArray.size(); i++) {
                JsonObject cropObject = cropsArray.get(i).getAsJsonObject();
                
                CropInfo cropInfo = new CropInfo(
                    getStringOrDefault(cropObject, "name", ""),
                    getStringOrDefault(cropObject, "scientific_name", ""),
                    getStringOrDefault(cropObject, "family", ""),
                    getStringOrDefault(cropObject, "image_url", ""),
                    getStringOrDefault(cropObject, "description", "")
                );
                
                crops.add(cropInfo);
            }

            return crops;
        } catch (Exception e) {
            logger.severe("API Request Failed: " + e.getMessage());
            e.printStackTrace();
            return crops;
        }
    }

    private String getStringOrDefault(JsonObject obj, String key, String defaultValue) {
        try {
            return obj.has(key) && !obj.get(key).isJsonNull() 
                ? obj.get(key).getAsString() 
                : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public CropInfo getCropByName(String cropName) {
        List<CropInfo> crops = getCropsForTunisia();
        return crops.stream()
                .filter(crop -> crop.getName().equalsIgnoreCase(cropName))
                .findFirst()
                .orElse(null);
    }
}

