package entite;

public class SoilData {
    private int id;
    private double humidite;
    private double niveau_ph;
    private double niveau_nutriment;
    private String type_sol;
    private int crop_id;

    public SoilData(int id, double humidite, double niveau_ph, double niveau_nutriment, String type_sol, int crop_id) {
        this.id = id;
        this.humidite = humidite;
        this.niveau_ph = niveau_ph;
        this.niveau_nutriment = niveau_nutriment;
        this.type_sol = type_sol;
        this.crop_id = crop_id;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getHumidite() {
        return humidite;
    }

    public void setHumidite(double humidite) {
        this.humidite = humidite;
    }

    public double getNiveau_ph() {
        return niveau_ph;
    }

    public void setNiveau_ph(double niveau_ph) {
        this.niveau_ph = niveau_ph;
    }

    public double getNiveau_nutriment() {
        return niveau_nutriment;
    }

    public void setNiveau_nutriment(double niveau_nutriment) {
        this.niveau_nutriment = niveau_nutriment;
    }

    public String getType_sol() {
        return type_sol;
    }

    public void setType_sol(String type_sol) {
        this.type_sol = type_sol;
    }

    public int getCrop_id() {
        return crop_id;
    }

    public void setCrop_id(int crop_id) {
        this.crop_id = crop_id;
    }

    @Override
    public String toString() {
        return "SoilData{" +
                "id=" + id +
                ", humidite=" + humidite +
                ", niveau_ph=" + niveau_ph +
                ", niveau_nutriment=" + niveau_nutriment +
                ", type_sol='" + type_sol + '\'' +
                ", crop_id=" + crop_id +
                '}';
    }
} 