package entite;

import entite.Commande;
import entite.Produit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe Panier qui gère les produits sélectionnés par l'utilisateur 
 * avant de finaliser une commande
 */
public class Panier {
    private static Panier instance;
    private Map<Produit, Integer> produitsQuantites;
    
    private Panier() {
        produitsQuantites = new HashMap<>();
    }
    
    /**
     * Récupère l'instance unique du panier (Singleton)
     * @return l'instance du panier
     */
    public static synchronized Panier getInstance() {
        if (instance == null) {
            instance = new Panier();
        }
        return instance;
    }
    
    /**
     * Ajoute un produit au panier avec une quantité donnée
     * @param produit le produit à ajouter
     * @param quantite la quantité à ajouter
     */
    public void ajouterProduit(Produit produit, int quantite) {
        if (produit == null || quantite <= 0) {
            return;
        }
        
        // Si le produit existe déjà, on ajoute la quantité
        if (produitsQuantites.containsKey(produit)) {
            int quantiteActuelle = produitsQuantites.get(produit);
            produitsQuantites.put(produit, quantiteActuelle + quantite);
        } else {
            produitsQuantites.put(produit, quantite);
        }
    }
    
    /**
     * Supprime un produit du panier
     * @param produit le produit à supprimer
     */
    public void supprimerProduit(Produit produit) {
        if (produit != null) {
            produitsQuantites.remove(produit);
        }
    }
    
    /**
     * Modifie la quantité d'un produit dans le panier
     * @param produit le produit à modifier
     * @param nouvelleQuantite la nouvelle quantité
     */
    public void modifierQuantite(Produit produit, int nouvelleQuantite) {
        if (produit == null || nouvelleQuantite <= 0) {
            return;
        }
        
        if (produitsQuantites.containsKey(produit)) {
            produitsQuantites.put(produit, nouvelleQuantite);
        }
    }
    
    /**
     * Vide le panier
     */
    public void viderPanier() {
        produitsQuantites.clear();
    }
    
    /**
     * Récupère tous les produits du panier
     * @return la liste des produits dans le panier
     */
    public List<Produit> getProduits() {
        return new ArrayList<>(produitsQuantites.keySet());
    }
    
    /**
     * Récupère la quantité d'un produit dans le panier
     * @param produit le produit
     * @return la quantité du produit
     */
    public int getQuantite(Produit produit) {
        return produitsQuantites.getOrDefault(produit, 0);
    }
    
    /**
     * Récupère la map des produits et leurs quantités
     * @return la map des produits et quantités
     */
    public Map<Produit, Integer> getProduitsQuantites() {
        return new HashMap<>(produitsQuantites);
    }
    
    /**
     * Calcule le prix total du panier
     * @return le prix total
     */
    public double calculerTotal() {
        double total = 0;
        for (Map.Entry<Produit, Integer> entry : produitsQuantites.entrySet()) {
            Produit produit = entry.getKey();
            int quantite = entry.getValue();
            total += produit.getPrix() * quantite;
        }
        return total;
    }
    
    /**
     * Vérifie si le panier est vide
     * @return true si le panier est vide, false sinon
     */
    public boolean estVide() {
        return produitsQuantites.isEmpty();
    }
    
    /**
     * Nombre total d'articles dans le panier (somme des quantités)
     * @return le nombre total d'articles
     */
    public int getNombreTotalArticles() {
        int total = 0;
        for (int quantite : produitsQuantites.values()) {
            total += quantite;
        }
        return total;
    }
    
    /**
     * Crée une commande à partir du contenu du panier
     * @param adresse l'adresse de livraison
     * @param typePaiement le mode de paiement
     * @param typeCommande le type de commande
     * @return la commande créée
     */
    public Commande creerCommande(String adresse, String typePaiement, String typeCommande) {
        if (estVide()) {
            return null;
        }
        
        double total = calculerTotal();
        int quantiteTotale = getNombreTotalArticles();
        
        Commande commande = new Commande(
            quantiteTotale,
            total,
            typeCommande,
            "En attente",
            adresse,
            typePaiement
        );
        
        // Ajouter tous les produits à la commande
        for (Produit produit : produitsQuantites.keySet()) {
            commande.addProduit(produit);
        }
        
        return commande;
    }
} 