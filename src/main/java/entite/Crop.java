package entite;

import java.sql.Date;
import java.sql.Time;

public class Crop {
    private int id;
    private String cropEvent;
    private String typeCrop;
    private String methodeCrop;
    private Date datePlantation;
    private Time heureCrop;
    private Date dateCrop;
    private Time heurePlantation;
    private float income;
    private SoilData soilData;

    public Crop() {
    }

    public Crop(int id, String cropEvent, String typeCrop, String methodeCrop,
                Date datePlantation, Time heureCrop, Date dateCrop, Time heurePlantation,
                float income) {
        this.id = id;
        this.cropEvent = cropEvent;
        this.typeCrop = typeCrop;
        this.methodeCrop = methodeCrop;
        this.datePlantation = datePlantation;
        this.heureCrop = heureCrop;
        this.dateCrop = dateCrop;
        this.heurePlantation = heurePlantation;
        this.income = income;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCropEvent() {
        return cropEvent;
    }

    public void setCropEvent(String cropEvent) {
        this.cropEvent = cropEvent;
    }

    public String getTypeCrop() {
        return typeCrop;
    }

    public void setTypeCrop(String typeCrop) {
        this.typeCrop = typeCrop;
    }

    public String getMethodeCrop() {
        return methodeCrop;
    }

    public void setMethodeCrop(String methodeCrop) {
        this.methodeCrop = methodeCrop;
    }

    public Date getDatePlantation() {
        return datePlantation;
    }

    public void setDatePlantation(Date datePlantation) {
        this.datePlantation = datePlantation;
    }

    public Time getHeureCrop() {
        return heureCrop;
    }

    public void setHeureCrop(Time heureCrop) {
        this.heureCrop = heureCrop;
    }

    public Date getDateCrop() {
        return dateCrop;
    }

    public void setDateCrop(Date dateCrop) {
        this.dateCrop = dateCrop;
    }

    public Time getHeurePlantation() {
        return heurePlantation;
    }

    public void setHeurePlantation(Time heurePlantation) {
        this.heurePlantation = heurePlantation;
    }

    public float getIncome() {
        return income;
    }

    public void setIncome(float income) {
        this.income = income;
    }

    public SoilData getSoilData() {
        return soilData;
    }

    public void setSoilData(SoilData soilData) {
        this.soilData = soilData;
    }

    @Override
    public String toString() {
        return "Crop{" +
                "id=" + id +
                ", cropEvent='" + cropEvent + '\'' +
                ", typeCrop='" + typeCrop + '\'' +
                ", methodeCrop='" + methodeCrop + '\'' +
                ", datePlantation=" + datePlantation +
                ", heureCrop=" + heureCrop +
                ", dateCrop=" + dateCrop +
                ", heurePlantation=" + heurePlantation +
                '}';
    }
} 