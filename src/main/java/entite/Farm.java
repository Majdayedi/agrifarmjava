package entite;

import java.util.List;

public class Farm {
    private int id;
    private String location;
    private String name;
    private double surface;
    private String adress;
    private double budget;
    private String weather;
    private String description;
    private boolean bir;
    private boolean photovoltaic;
    private boolean fence;
    private boolean irrigation;
    private boolean cabin;
    private float lon;
    private float lat;
    private List<Field> fields;

    public Farm() {
    }

    public Farm(int id, String location, String name, double surface, String adress,
                double budget, String weather, String description, boolean bir,
                boolean photovoltaic, boolean fence, boolean irrigation,
                boolean cabin, float lon, float lat) {
        this.id = id;
        this.location = location;
        this.name = name;
        this.surface = surface;
        this.adress = adress;
        this.budget = budget;
        this.weather = weather;
        this.description = description;
        this.bir = bir;
        this.photovoltaic = photovoltaic;
        this.fence = fence;
        this.irrigation = irrigation;
        this.cabin = cabin;
        this.lon = lon;
        this.lat = lat;
    }

    public Farm(String location, String name, double surface, String adress,
                double budget, String weather, String description, boolean bir,
                boolean photovoltaic, boolean fence, boolean irrigation,
                boolean cabin, float lon, float lat) {
        this.location = location;
        this.name = name;
        this.surface = surface;
        this.adress = adress;
        this.budget = budget;
        this.weather = weather;
        this.description = description;
        this.bir = bir;
        this.photovoltaic = photovoltaic;
        this.fence = fence;
        this.irrigation = irrigation;
        this.cabin = cabin;
        this.lon = lon;
        this.lat = lat;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSurface() {
        return surface;
    }

    public void setSurface(double surface) {
        this.surface = surface;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isBir() {
        return bir;
    }

    public void setBir(boolean bir) {
        this.bir = bir;
    }

    public boolean isPhotovoltaic() {
        return photovoltaic;
    }

    public void setPhotovoltaic(boolean photovoltaic) {
        this.photovoltaic = photovoltaic;
    }

    public boolean isFence() {
        return fence;
    }

    public void setFence(boolean fence) {
        this.fence = fence;
    }

    public boolean isIrrigation() {
        return irrigation;
    }

    public void setIrrigation(boolean irrigation) {
        this.irrigation = irrigation;
    }

    public boolean isCabin() {
        return cabin;
    }

    public void setCabin(boolean cabin) {
        this.cabin = cabin;
    }

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "Farm{" +
                "id=" + id +
                ", location='" + location + '\'' +
                ", name='" + name + '\'' +
                ", surface=" + surface +
                ", adress='" + adress + '\'' +
                ", budget=" + budget +
                ", weather='" + weather + '\'' +
                ", description='" + description + '\'' +
                ", bir=" + bir +
                ", photovoltaic=" + photovoltaic +
                ", fence=" + fence +
                ", irrigation=" + irrigation +
                ", cabin=" + cabin +
                ", lon=" + lon +
                ", lat=" + lat +
                '}';
    }
}