package entite;

import java.time.format.DateTimeFormatter;

public class CropCalendarEntry {
    private String cropName;
    private String region;
    private String additionalInfo;
    private String plantingDate;
    private String harvestDate;
    private int growingPeriodDays;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");

    /**
     * Constructor with manually specified growing period from the API
     */
    public CropCalendarEntry(String cropName, String region, String additionalInfo,
                             String plantingDate, String harvestDate, int growingPeriodDays) {
        this.cropName = cropName;
        this.region = region;
        this.additionalInfo = additionalInfo;
        this.plantingDate = plantingDate;
        this.harvestDate = harvestDate;
        this.growingPeriodDays = growingPeriodDays;
        
        // Debug output
        System.out.println(String.format(
            "Created entry for crop '%s' with API-provided growing period: %d days",
            cropName, 
            growingPeriodDays
        ));
    }
    
    /**
     * Constructor for backward compatibility that calculates growing period
     */
    public CropCalendarEntry(String cropName, String region, String additionalInfo,
                             String plantingDate, String harvestDate) {
        this(cropName, region, additionalInfo, plantingDate, harvestDate, -1);
        System.out.println("Warning: Using deprecated constructor without growing period parameter");
    }
    
    /**
     * @deprecated This method is no longer used as growing period is now provided by the API
     */
    private int calculateGrowingPeriod() {
        System.out.println("Warning: calculateGrowingPeriod() called but this method is deprecated");
        return -1; // Return default value
    }

    // Getters
    public String getCropName() { return cropName; }
    public String getRegion() { return region; }
    public String getAdditionalInfo() { return additionalInfo; }
    public String getPlantingDate() { return plantingDate; }
    public String getHarvestDate() { return harvestDate; }
    public int getGrowingPeriod() { return growingPeriodDays; }
    
    // For backward compatibility
    public String getGrowingPeriodAsString() { 
        return growingPeriodDays > 0 ? growingPeriodDays + " days" : "Unknown"; 
    }

    // Setters
    public void setCropName(String cropName) { this.cropName = cropName; }
    public void setRegion(String region) { this.region = region; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }
    public void setPlantingDate(String plantingDate) { 
        this.plantingDate = plantingDate;
    }
    public void setHarvestDate(String harvestDate) { 
        this.harvestDate = harvestDate;
    }
    public void setGrowingPeriod(int growingPeriodDays) { this.growingPeriodDays = growingPeriodDays; }
}
