package entite;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Commande {
    private int id;
    private int userId; // Nouveau champ pour l'ID de l'utilisateur
    private int quantite;
    private double prix;
    private String type_commande; // Par exemple: "Achat direct", "Précommande", etc.
    private String status; // Par exemple: "En attente", "Confirmée", "Livrée", "Annulée"
    private String adress;
    private String paiment; // Mode de paiement
    private Date date_creation_commande;
    private List<Produit> produits; // Relation many-to-many avec Produit
    private Map<Integer, Integer> quantitesParProduit; // Map stockant la quantité pour chaque ID de produit

    // Constructeur par défaut
    public Commande() {
        this.produits = new ArrayList<>();
        this.quantitesParProduit = new HashMap<>();
        this.date_creation_commande = new Date(System.currentTimeMillis());
        this.status = "En attente";
    }

    // Constructeur avec paramètres (sans ID)
    public Commande(int userId, int quantite, double prix, String type_commande, String status, 
                   String adress, String paiment) {
        this.userId = userId;
        this.quantite = quantite;
        this.prix = prix;
        this.type_commande = type_commande;
        this.status = status;
        this.adress = adress;
        this.paiment = paiment;
        this.date_creation_commande = new Date(System.currentTimeMillis());
        this.produits = new ArrayList<>();
        this.quantitesParProduit = new HashMap<>();
    }

    // Constructeur complet
    public Commande(int id, int userId, int quantite, double prix, String type_commande, String status, 
                   String adress, String paiment, Date date_creation_commande) {
        this.id = id;
        this.userId = userId;
        this.quantite = quantite;
        this.prix = prix;
        this.type_commande = type_commande;
        this.status = status;
        this.adress = adress;
        this.paiment = paiment;
        this.date_creation_commande = date_creation_commande;
        this.produits = new ArrayList<>();
        this.quantitesParProduit = new HashMap<>();
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public String getType_commande() {
        return type_commande;
    }

    public void setType_commande(String type_commande) {
        this.type_commande = type_commande;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public String getPaiment() {
        return paiment;
    }

    public void setPaiment(String paiment) {
        this.paiment = paiment;
    }

    public Date getDate_creation_commande() {
        return date_creation_commande;
    }

    public void setDate_creation_commande(Date date_creation_commande) {
        this.date_creation_commande = date_creation_commande;
    }

    public List<Produit> getProduits() {
        return produits;
    }

    public void setProduits(List<Produit> produits) {
        this.produits = produits;
    }

    public Map<Integer, Integer> getQuantitesParProduit() {
        return quantitesParProduit;
    }

    public void setQuantitesParProduit(Map<Integer, Integer> quantitesParProduit) {
        this.quantitesParProduit = quantitesParProduit;
    }
    
    // Méthode pour ajouter un produit avec sa quantité spécifique
    public void addProduit(Produit produit, int quantite) {
        if (!this.produits.contains(produit)) {
            this.produits.add(produit);
            this.quantitesParProduit.put(produit.getId(), quantite);
        } else {
            // Si le produit existe déjà, mettre à jour sa quantité
            this.quantitesParProduit.put(produit.getId(), quantite);
        }
    }
    
    // Méthodes pour gérer la relation many-to-many
    public void addProduit(Produit produit) {
        if (!this.produits.contains(produit)) {
            this.produits.add(produit);
            // Par défaut, quantité = 1
            this.quantitesParProduit.put(produit.getId(), 1);
        }
    }
    
    public void removeProduit(Produit produit) {
        this.produits.remove(produit);
        this.quantitesParProduit.remove(produit.getId());
    }
    
    @Override
    public String toString() {
        return "Commande{" + "id=" + id + ", quantite=" + quantite + ", prix=" + prix + 
                ", type_commande=" + type_commande + ", status=" + status + 
                ", adress=" + adress + ", paiment=" + paiment + 
                ", date_creation_commande=" + date_creation_commande + '}';
    }
} 