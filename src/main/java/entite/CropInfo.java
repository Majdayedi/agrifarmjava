package entite;

public class CropInfo {
    private String name;
    private String scientificName;
    private String family;
    private String imageUrl;
    private String description;

    public CropInfo() {}

    public CropInfo(String name, String scientificName, String family, String imageUrl, String description) {
        this.name = name;
        this.scientificName = scientificName;
        this.family = family;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getScientificName() { return scientificName; }
    public void setScientificName(String scientificName) { this.scientificName = scientificName; }
    
    public String getFamily() { return family; }
    public void setFamily(String family) { this.family = family; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "CropInfo{" +
                "name='" + name + '\'' +
                ", scientificName='" + scientificName + '\'' +
                ", family='" + family + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}

