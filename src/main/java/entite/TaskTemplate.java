package entite;

import java.sql.Date;
import java.sql.Timestamp;

public class TaskTemplate {
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

    public TaskTemplate(String name, String description, String status, Date date,
                       String ressource, String responsable, String priority,
                       String estimatedDuration, Date deadline, int workers,
                       Timestamp lastUpdated, double paymentWorker, double total) {
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

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public Date getDate() { return date; }
    public String getRessource() { return ressource; }
    public String getResponsable() { return responsable; }
    public String getPriority() { return priority; }
    public String getEstimatedDuration() { return estimatedDuration; }
    public Date getDeadline() { return deadline; }
    public int getWorkers() { return workers; }
    public Timestamp getLastUpdated() { return lastUpdated; }
    public double getPaymentWorker() { return paymentWorker; }
    public double getTotal() { return total; }
} 