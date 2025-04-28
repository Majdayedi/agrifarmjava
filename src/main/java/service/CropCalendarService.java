package service;

import entite.CropCalendarEntry;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CropCalendarService {
    private static final String BASE_URL = "https://api-cropcalendar.apps.fao.org/api/v1";
    private static final Logger logger = Logger.getLogger(CropCalendarService.class.getName());
    private final HttpClient httpClient;

    public CropCalendarService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public List<CropCalendarEntry> getCropsForTunisia() {
        String apiUrl = String.format("%s/countries/TN/cropCalendar?language=en", BASE_URL);
        List<CropCalendarEntry> entries = new ArrayList<>();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.warning("Failed to fetch crop calendar: HTTP " + response.statusCode());
                return entries;
            }

            JSONArray crops = new JSONArray(response.body());

            for (int i = 0; i < crops.length(); i++) {
                JSONObject cropData = crops.getJSONObject(i);
                JSONObject crop = cropData.getJSONObject("crop");
                JSONObject aez = cropData.getJSONObject("aez");
                JSONArray sessions = cropData.getJSONArray("sessions");

                // Process each growing session for the crop
                for (int j = 0; j < sessions.length(); j++) {
                    JSONObject session = sessions.getJSONObject(j);

                    // Format planting date range
                    String plantingDate = formatDateRange(
                            session.getJSONObject("early_sowing"),
                            session.getJSONObject("later_sowing")
                    );

                    // Format harvest date range
                    String harvestDate = formatDateRange(
                            session.getJSONObject("early_harvest"),
                            session.getJSONObject("late_harvest")
                    );

                    // Extract growing period from JSON
                    int growingPeriod = extractGrowingPeriod(session, crop.getString("name"));

                    // Create and add the entry
                    CropCalendarEntry entry = createEntry(
                            crop.getString("name"),
                            aez.getString("name"),
                            session.getString("additional_information"),
                            plantingDate,
                            harvestDate,
                            growingPeriod
                    );
                    
                    entries.add(entry);
                }
            }

        } catch (Exception e) {
            logger.severe("API Request Failed: " + e.getMessage());
            e.printStackTrace();
        }

        return entries;
    }

    private int extractGrowingPeriod(JSONObject session, String cropName) {
        try {
            JSONObject growingPeriodObj = session.getJSONObject("growing_period");
            int growingPeriod = growingPeriodObj.getInt("value");
            
            // Debug: Print the extracted growing period
            System.out.println(String.format(
                "Extracted growing period for crop '%s': %d days",
                cropName,
                growingPeriod
            ));
            
            return growingPeriod;
        } catch (Exception e) {
            logger.warning("Failed to extract growing period for crop '" + 
                         cropName + "': " + e.getMessage());
            return -1;
        }
    }

    private CropCalendarEntry createEntry(String cropName, String region, String additionalInfo,
                                        String plantingDate, String harvestDate, int growingPeriod) {
        // Debug: Print entry details before creation
        System.out.println(String.format(
            "Creating entry:%n" +
            "Crop: %s%n" +
            "Region: %s%n" +
            "Planting period: %s%n" +
            "Harvest period: %s%n" +
            "Growing period: %d days",
            cropName,
            region,
            plantingDate,
            harvestDate,
            growingPeriod
        ));

        CropCalendarEntry entry = new CropCalendarEntry(
                cropName,
                region,
                additionalInfo,
                plantingDate,
                harvestDate,
                growingPeriod
        );
        
        // Debug: Print the entry details after creation
        System.out.println(String.format(
            "Added entry:%n" +
            "Crop: %s%n" +
            "Planting: %s%n" +
            "Harvest: %s%n" +
            "Growing period: %d days",
            entry.getCropName(),
            entry.getPlantingDate(),
            entry.getHarvestDate(),
            entry.getGrowingPeriod()
        ));
        
        return entry;
    }

    private String formatDateRange(JSONObject early, JSONObject late) {
        String startDate = formatDate(early.getInt("day"), early.getInt("month"));
        String endDate = formatDate(late.getInt("day"), late.getInt("month"));
        return startDate + " - " + endDate;
    }

    private String formatDate(int day, int month) {
        // Ensure consistent DD/MM format with leading zeros
        return String.format("%02d/%02d", day, month);
    }
}