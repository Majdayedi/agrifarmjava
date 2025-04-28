package entite;

import java.sql.Date;
import java.sql.Timestamp;

public class Task {
    private int id;
    private Field field;
    private String name;
    private String description;
    private String status;
    private Date date;
    private String ressource;
    private String responsable;
    private String priority;
    private String estimatedDuration;
    private Date deadline;
    private int workers;
    private Timestamp lastUpdated;
    private double paymentWorker;
    private double total;

    public Task() {
    }

    public Task(String name, String description, String status, Date date, String responsable, 
                String priority, int workers, double paymentWorker, Field field) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.date = date;
        this.responsable = responsable;
        this.priority = priority;
        this.workers = workers;
        this.paymentWorker = paymentWorker;
        this.field = field;
        this.total = workers * paymentWorker;
        this.lastUpdated = new Timestamp(System.currentTimeMillis());
    }

        public Task( Field field, String name, String description, String status,
                    Date date, String ressource, String responsable, String priority,
                    String estimatedDuration, Date deadline, int workers, Timestamp lastUpdated,
                    double paymentWorker, double total) {
        this.field = field;
        this.name = name;
        this.description = description;
        this.status = status;
        this.date = date;
        this.ressource = ressource;
        this.responsable = responsable;
        this.priority = priority;
        this.estimatedDuration = estimatedDuration;
        this.deadline = deadline;
        this.workers = workers;
        this.lastUpdated = lastUpdated;
        this.paymentWorker = paymentWorker;
        this.total = total;
    }
    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getRessource() {
        return ressource;
    }

    public void setRessource(String ressource) {
        this.ressource = ressource;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(String estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public int getWorkers() {
        return workers;
    }

    public void setWorkers(int workers) {
        this.workers = workers;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public double getPaymentWorker() {
        return paymentWorker;
    }

    public void setPaymentWorker(double paymentWorker) {
        this.paymentWorker = paymentWorker;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }


    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", date=" + date +
                ", ressource='" + ressource + '\'' +
                ", responsable='" + responsable + '\'' +
                ", priority='" + priority + '\'' +
                ", estimatedDuration='" + estimatedDuration + '\'' +
                ", deadline=" + deadline +
                ", workers=" + workers +
                ", lastUpdated=" + lastUpdated +
                ", paymentWorker=" + paymentWorker +
                ", total=" + total +
                '}';
    }
}
