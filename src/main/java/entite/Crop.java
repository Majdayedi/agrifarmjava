package entite;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

public class Crop {
    private final IntegerProperty id;
    private final StringProperty cropEvent;
    private final StringProperty typeCrop;
    private final StringProperty methodCrop;
    private final StringProperty plantationDate;
    private final StringProperty hourPlantation;
    private final StringProperty cropDate;
    private final StringProperty hourCrop;

    public Crop(int id, String cropEvent, String typeCrop, String methodCrop,
                String plantationDate, String hourPlantation, String cropDate,
                String hourCrop) {
        this.id = new SimpleIntegerProperty(id);
        this.cropEvent = new SimpleStringProperty(cropEvent);
        this.typeCrop = new SimpleStringProperty(typeCrop);
        this.methodCrop = new SimpleStringProperty(methodCrop);
        this.plantationDate = new SimpleStringProperty(plantationDate);
        this.hourPlantation = new SimpleStringProperty(hourPlantation);
        this.cropDate = new SimpleStringProperty(cropDate);
        this.hourCrop = new SimpleStringProperty(hourCrop);
    }

    // Property getters
    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty cropEventProperty() {
        return cropEvent;
    }

    public StringProperty typeCropProperty() {
        return typeCrop;
    }

    public StringProperty methodCropProperty() {
        return methodCrop;
    }

    public StringProperty plantationDateProperty() {
        return plantationDate;
    }

    public StringProperty hourPlantationProperty() {
        return hourPlantation;
    }

    public StringProperty cropDateProperty() {
        return cropDate;
    }

    public StringProperty hourCropProperty() {
        return hourCrop;
    }

    // Regular getters
    public int getId() {
        return id.get();
    }

    public String getCropEvent() {
        return cropEvent.get();
    }

    public String getTypeCrop() {
        return typeCrop.get();
    }

    public String getMethodCrop() {
        return methodCrop.get();
    }

    public String getPlantationDate() {
        return plantationDate.get();
    }

    public String getHourPlantation() {
        return hourPlantation.get();
    }

    public String getCropDate() {
        return cropDate.get();
    }

    public String getHourCrop() {
        return hourCrop.get();
    }

    // Setters
    public void setId(int id) {
        this.id.set(id);
    }

    public void setCropEvent(String cropEvent) {
        this.cropEvent.set(cropEvent);
    }

    public void setTypeCrop(String typeCrop) {
        this.typeCrop.set(typeCrop);
    }

    public void setMethodCrop(String methodCrop) {
        this.methodCrop.set(methodCrop);
    }

    public void setPlantationDate(String plantationDate) {
        this.plantationDate.set(plantationDate);
    }

    public void setHourPlantation(String hourPlantation) {
        this.hourPlantation.set(hourPlantation);
    }

    public void setCropDate(String cropDate) {
        this.cropDate.set(cropDate);
    }

    public void setHourCrop(String hourCrop) {
        this.hourCrop.set(hourCrop);
    }

    @Override
    public String toString() {
        return "Crop{" +
                "id=" + id.get() +
                ", cropEvent='" + cropEvent.get() + '\'' +
                ", typeCrop='" + typeCrop.get() + '\'' +
                ", methodCrop='" + methodCrop.get() + '\'' +
                ", plantationDate='" + plantationDate.get() + '\'' +
                ", hourPlantation='" + hourPlantation.get() + '\'' +
                ", cropDate='" + cropDate.get() + '\'' +
                ", hourCrop='" + hourCrop.get() + '\'' +
                '}';
    }
} 