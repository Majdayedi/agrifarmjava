package entite;

import java.sql.Date;
import java.util.Objects;

public class Produit {
    private int id;
    private String nom;
    private int quantite;
    private double prix;
    private String categories;
    private Date date_creation_produit;
    private Date date_modification_produit;
    private boolean approved;
    private String description;
    private String image_file_name;

    public Produit() {
    }

    public Produit(String nom, int quantite, double prix, String categories, 
                  String description, String image_file_name) {
        this.nom = nom;
        this.quantite = quantite;
        this.prix = prix;
        this.categories = categories;
        this.description = description;
        this.image_file_name = image_file_name;
        this.date_creation_produit = new Date(System.currentTimeMillis());
        this.date_modification_produit = this.date_creation_produit;
        this.approved = false;
    }

    public Produit(int id, String nom, int quantite, double prix, String categories, 
                  Date date_creation_produit, Date date_modification_produit, 
                  boolean approved, String description, String image_file_name) {
        this.id = id;
        this.nom = nom;
        this.quantite = quantite;
        this.prix = prix;
        this.categories = categories;
        this.date_creation_produit = date_creation_produit;
        this.date_modification_produit = date_modification_produit;
        this.approved = approved;
        this.description = description;
        this.image_file_name = image_file_name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public Date getDate_creation_produit() {
        return date_creation_produit;
    }

    public void setDate_creation_produit(Date date_creation_produit) {
        this.date_creation_produit = date_creation_produit;
    }

    public Date getDate_modification_produit() {
        return date_modification_produit;
    }

    public void setDate_modification_produit(Date date_modification_produit) {
        this.date_modification_produit = date_modification_produit;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage_file_name() {
        return image_file_name;
    }

    public void setImage_file_name(String image_file_name) {
        this.image_file_name = image_file_name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produit produit = (Produit) o;
        return id == produit.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", quantite=" + quantite +
                ", prix=" + prix +
                ", categories='" + categories + '\'' +
                ", approved=" + approved +
                '}';
    }
}
