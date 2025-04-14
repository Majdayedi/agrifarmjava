package entite;

import java.util.List;

public class Field {
    private int id;
    private Farm farm;
    private double surface;
    private String name;
    private double budget;
    private double income;
    private double outcome;
    private double profit;
    private String description;
    private Crop crop;
    private List<Task> tasks;

    public Field() {
    }

    public Field(int id, Farm farm, double surface, String name, double budget,
                double income, double outcome, double profit, String description, Crop crop) {
        this.id = id;
        this.farm = farm;
        this.surface = surface;
        this.name = name;
        this.budget = budget;
        this.income = income;
        this.outcome = outcome;
        this.profit = profit;
        this.description = description;
        this.crop = crop;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Farm getFarm() {
        return farm;
    }

    public void setFarm(Farm farm) {
        this.farm = farm;
    }

    public double getSurface() {
        return surface;
    }

    public void setSurface(double surface) {
        this.surface = surface;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public double getIncome() {
        return income;
    }

    public void setIncome(double income) {
        this.income = income;
    }

    public double getOutcome() {
        return outcome;
    }

    public void setOutcome(double outcome) {
        this.outcome = outcome;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Crop getCrop() {
        return crop;
    }

    public void setCrop(Crop crop) {
        this.crop = crop;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public String toString() {
        return "Field{" +
                "id=" + id +
                ", surface=" + surface +
                ", name='" + name + '\'' +
                ", budget=" + budget +
                ", income=" + income +
                ", outcome=" + outcome +
                ", profit=" + profit +
                ", description='" + description + '\'' +
                '}';
    }
} 