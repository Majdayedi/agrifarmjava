package models;

import java.time.LocalDateTime;

public class Commande {
    private int id;
    private int userId;
    private double montantTotal;
    private LocalDateTime dateCreation;
    private String status;

    public Commande(int id, int userId, double montantTotal, LocalDateTime dateCreation, String status) {
        this.id = id;
        this.userId = userId;
        this.montantTotal = montantTotal;
        this.dateCreation = dateCreation;
        this.status = status;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 