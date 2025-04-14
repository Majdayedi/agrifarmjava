package entite;

public class SoilData {
    private int id;
    private double niveauPh;
    private double humidite;
    private double niveauNutriment;
    private String typeSol;
    private Crop crop;

    public SoilData() {
    }

    public SoilData(int id, double niveauPh, double humidite, double niveauNutriment,
                    String typeSol, Crop crop) {
        this.id = id;
        this.niveauPh = niveauPh;
        this.humidite = humidite;
        this.niveauNutriment = niveauNutriment;
        this.typeSol = typeSol;
        this.crop = crop;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getNiveauPh() {
        return niveauPh;
    }

    public void setNiveauPh(double niveauPh) {
        this.niveauPh = niveauPh;
    }

    public double getHumidite() {
        return humidite;
    }

    public void setHumidite(double humidite) {
        this.humidite = humidite;
    }

    public double getNiveauNutriment() {
        return niveauNutriment;
    }

    public void setNiveauNutriment(double niveauNutriment) {
        this.niveauNutriment = niveauNutriment;
    }

    public String getTypeSol() {
        return typeSol;
    }

    public void setTypeSol(String typeSol) {
        this.typeSol = typeSol;
    }

    public Crop getCrop() {
        return crop;
    }

    public void setCrop(Crop crop) {
        this.crop = crop;
    }

    @Override
    public String toString() {
        return "SoilData{" +
                "id=" + id +
                ", niveauPh=" + niveauPh +
                ", humidite=" + humidite +
                ", niveauNutriment=" + niveauNutriment +
                ", typeSol='" + typeSol + '\'' +
                '}';
    }
} 