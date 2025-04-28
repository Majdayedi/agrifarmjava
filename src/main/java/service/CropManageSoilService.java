package service;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CropManageSoilService {
    private static final String SOIL_TYPES_URL = "https://api.cropmanage.ucanr.edu/v2/soil-types.json";
    private final CropManageAuthService authService;

    public CropManageSoilService(CropManageAuthService authService) {
        this.authService = authService;
    }

    public List<Map<String, Object>> getSoilTypes() {
        List<Map<String, Object>> soilTypes = new ArrayList<>();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(SOIL_TYPES_URL);

            // Set authorization header
            String accessToken = authService.getAccessToken();
            if (accessToken == null) {
                accessToken = authService.authenticate();
            }

            httpGet.setHeader("Authorization", "Bearer " + accessToken);

            // Execute request
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> rawSoilTypes = mapper.readValue(jsonResponse, List.class);

                // Remove duplicates (case-insensitive)
                List<String> seenNames = new ArrayList<>();
                for (Map<String, Object> soil : rawSoilTypes) {
                    String name = ((String) soil.get("Name")).toLowerCase();
                    if (!seenNames.contains(name)) {
                        seenNames.add(name);
                        soilTypes.add(soil);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return soilTypes;
    }
}