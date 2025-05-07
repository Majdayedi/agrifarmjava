package service;

import entite.Commande;
import entite.Produit;
import entite.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import utils.DatabaseConnection;
import utils.Connections;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandeService {
    private static final Logger logger = Logger.getLogger(CommandeService.class.getName());
    private static final Logger LOGGER = Logger.getLogger(CommandeService.class.getName());

    public CommandeService() {
        // No need to initialize connection here as we're using getConnection() method
    }

    // Méthode pour obtenir une connexion fraîche à chaque fois
    private Connection getConnection() {
        return Connections.getInstance().getConnection();
    }

    // Méthode pour compter le nombre de commandes d'un utilisateur
    private int countUserOrders(int userId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        int count = 0;

        try {
            connection = getConnection();
            if (connection == null) {
                logger.log(Level.SEVERE, "Cannot count user orders: Database connection is null");
                return count;
            }

            String sql = "SELECT COUNT(*) as order_count FROM commande WHERE user_id = ?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            rs = statement.executeQuery();

            if (rs.next()) {
                count = rs.getInt("order_count");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error counting user orders", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error closing resources", e);
            }
        }
        return count;
    }

    // Méthode pour appliquer la réduction si nécessaire
    private double applyDiscount(double price, int userId) {
        int orderCount = countUserOrders(userId);
        // Si c'est la 5ème commande (ou multiple de 5)
        if ((orderCount + 1) % 5 == 0) {
            // Appliquer une réduction de 10%
            return price * 0.9;
        }
        return price;
    }

    public boolean create(Commande commande) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;

        try {
            connection = getConnection();
            if (connection == null) {
                System.err.println("Cannot create commande: Database connection is null");
                return false;
            }

            // Appliquer la réduction si nécessaire
            double originalPrice = commande.getPrix();
            double discountedPrice = applyDiscount(originalPrice, commande.getUserId());
            if (discountedPrice != originalPrice) {
                commande.setPrix(discountedPrice);
                logger.info("Réduction de 10% appliquée pour l'utilisateur " + commande.getUserId() +
                        ". Prix original: " + originalPrice + "€, Prix après réduction: " + discountedPrice + "€");
            }

            // Vérifier si la table a la colonne quantite
            try {
                String sql = "INSERT INTO commande (quantite, prix, type_commande, status, adress, " +
                        "paiment, date_creation_commande, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                statement.setInt(1, commande.getQuantite());
                statement.setDouble(2, commande.getPrix());
                statement.setString(3, commande.getType_commande());
                statement.setString(4, commande.getStatus());
                statement.setString(5, commande.getAdress());
                statement.setString(6, commande.getPaiment());

                // Use current timestamp if date_creation_commande is null
                Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
                statement.setTimestamp(7, commande.getDate_creation_commande() != null ?
                        new Timestamp(commande.getDate_creation_commande().getTime()) : currentTimestamp);

                // Set the user_id
                statement.setInt(8, commande.getUserId());

            } catch (SQLException e) {
                System.err.println("Erreur lors de la vérification ou modification de la structure de la table: " + e.getMessage());

                // Si la vérification échoue, on utilise une requête sans la colonne quantite
                String sql = "INSERT INTO commande (prix, type_commande, status, adress, " +
                        "paiment, date_creation_commande, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

                statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                statement.setDouble(1, commande.getPrix());
                statement.setString(2, commande.getType_commande());
                statement.setString(3, commande.getStatus());
                statement.setString(4, commande.getAdress());
                statement.setString(5, commande.getPaiment());

                // Use current timestamp if date_creation_commande is null
                Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
                statement.setTimestamp(6, commande.getDate_creation_commande() != null ?
                        new Timestamp(commande.getDate_creation_commande().getTime()) : currentTimestamp);

                // Set the user_id
                statement.setInt(7, commande.getUserId());
            }

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    commande.setId(generatedKeys.getInt(1));

                    // Maintenant, insérer les relations many-to-many avec les produits
                    return insertCommandeProduits(connection, commande);
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error creating commande: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (statement != null) statement.close();
                // Ne pas fermer la connexion ici car elle est gérée par le pool
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    private boolean insertCommandeProduits(Connection connection, Commande commande) throws SQLException {
        PreparedStatement statement = null;
        try {
            System.out.println("Insertion des produits pour la commande ID: " + commande.getId());

            // Vérification de l'existence de la table commande_produit
            try {
                statement = connection.prepareStatement("SHOW TABLES LIKE 'commande_produit'");
                ResultSet tablesRS = statement.executeQuery();
                boolean tableExists = tablesRS.next();
                statement.close();

                if (!tableExists) {
                    System.out.println("La table commande_produit n'existe pas, création...");
                    statement = connection.prepareStatement(
                            "CREATE TABLE IF NOT EXISTS commande_produit (" +
                                    "commande_id INT NOT NULL, " +
                                    "produit_id INT NOT NULL, " +
                                    "quantite INT NOT NULL DEFAULT 1, " +
                                    "PRIMARY KEY (commande_id, produit_id), " +
                                    "FOREIGN KEY (commande_id) REFERENCES commande(id) ON DELETE CASCADE, " +
                                    "FOREIGN KEY (produit_id) REFERENCES produit(id) ON DELETE CASCADE)"
                    );
                    statement.executeUpdate();
                    statement.close();
                } else {
                    // Vérifier si la colonne 'quantite' existe
                    boolean quantiteColumnExists = false;
                    statement = connection.prepareStatement("SHOW COLUMNS FROM commande_produit");
                    ResultSet columnsRS = statement.executeQuery();
                    while (columnsRS.next()) {
                        if ("quantite".equalsIgnoreCase(columnsRS.getString("Field"))) {
                            quantiteColumnExists = true;
                            break;
                        }
                    }
                    statement.close();

                    // Si la colonne n'existe pas, l'ajouter
                    if (!quantiteColumnExists) {
                        System.out.println("La colonne quantite n'existe pas dans la table commande_produit, ajout...");
                        statement = connection.prepareStatement(
                                "ALTER TABLE commande_produit ADD COLUMN quantite INT NOT NULL DEFAULT 1"
                        );
                        statement.executeUpdate();
                        statement.close();
                    }
                }
            } catch (SQLException e) {
                System.out.println("Erreur lors de la vérification de la table: " + e.getMessage());
                e.printStackTrace();
            }

            // Pour chaque produit dans la commande
            for (int i = 0; i < commande.getProduits().size(); i++) {
                Produit produit = commande.getProduits().get(i);
                int quantite = 1; // Valeur par défaut

                // Récupérer la quantité spécifique du produit depuis la commande si disponible
                if (commande.getQuantitesParProduit() != null && commande.getQuantitesParProduit().containsKey(produit.getId())) {
                    quantite = commande.getQuantitesParProduit().get(produit.getId());
                }

                System.out.println("Ajout du produit ID: " + produit.getId() + " avec quantité: " + quantite);

                try {
                    // Essayer d'abord sans la colonne quantite au cas où la modification n'aurait pas fonctionné
                    statement = connection.prepareStatement(
                            "INSERT INTO commande_produit (commande_id, produit_id) VALUES (?, ?)"
                    );
                    statement.setInt(1, commande.getId());
                    statement.setInt(2, produit.getId());
                    statement.executeUpdate();
                } catch (SQLException e) {
                    // Si l'insertion a échoué, essayer avec la colonne quantite
                    try {
                        statement = connection.prepareStatement(
                                "INSERT INTO commande_produit (commande_id, produit_id, quantite) VALUES (?, ?, ?)"
                        );
                        statement.setInt(1, commande.getId());
                        statement.setInt(2, produit.getId());
                        statement.setInt(3, quantite);
                        statement.executeUpdate();
                    } catch (SQLException e2) {
                        System.err.println("Erreur lors de l'insertion avec quantite: " + e2.getMessage());
                        e2.printStackTrace();
                        return false;
                    }
                } finally {
                    if (statement != null && !statement.isClosed()) {
                        statement.close();
                    }
                }

                // Mise à jour de la quantité du produit (déduction de la quantité commandée)
                try {
                    // D'abord récupérer la quantité actuelle du produit
                    PreparedStatement getQuantityStmt = connection.prepareStatement(

                            "SELECT quantite FROM produit WHERE id = ?"
                    );
                    getQuantityStmt.setInt(1, produit.getId());
                    ResultSet quantityRS = getQuantityStmt.executeQuery();

                    if (quantityRS.next()) {
                        int currentQuantity = quantityRS.getInt("quantite");

                        // Vérification des logs
                        System.out.println("Produit ID: " + produit.getId() + " - " + produit.getNom());
                        System.out.println("Quantité actuelle en stock: " + currentQuantity);
                        System.out.println("Quantité commandée: " + quantite);

                        // Vérification que la quantité ne sera pas négative
                        if (currentQuantity < quantite) {
                            System.out.println("ATTENTION: La quantité commandée dépasse le stock disponible!");
                            // Si la quantité est insuffisante, on ajuste à 0 (stock épuisé)
                            quantite = currentQuantity;
                            System.out.println("La quantité a été ajustée à: " + quantite);
                        }


                        int newQuantity = Math.max(0, currentQuantity - quantite); // Éviter les quantités négatives

                        // Mise à jour de la quantité du produit
                        PreparedStatement updateQuantityStmt = connection.prepareStatement(
                                "UPDATE produit SET quantite = ? WHERE id = ?"
                        );
                        updateQuantityStmt.setInt(1, newQuantity);
                        updateQuantityStmt.setInt(2, produit.getId());
                        int rowsUpdated = updateQuantityStmt.executeUpdate();
                        updateQuantityStmt.close();


                        System.out.println("Quantité du produit ID: " + produit.getId() +
                                " (" + produit.getNom() + ") mise à jour de " +
                                currentQuantity + " à " + newQuantity +
                                " - Lignes mises à jour: " + rowsUpdated);
                    } else {
                        System.out.println("ERREUR: Produit ID " + produit.getId() + " non trouvé dans la base de données!");
                    }


                    quantityRS.close();
                    getQuantityStmt.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la mise à jour de la quantité du produit: " + e.getMessage());
                    e.printStackTrace();
                    // Ne pas retourner false ici pour permettre la poursuite de la commande même en cas d'échec de mise à jour
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion des produits pour la commande: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (statement != null && !statement.isClosed()) {
                statement.close();
            }
        }
    }

    public Commande read(int id) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            if (connection == null) {
                System.err.println("Cannot read commande: Database connection is null");
                return null;
            }

            String sql = "SELECT * FROM commande WHERE id = ?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            rs = statement.executeQuery();

            if (rs.next()) {
                Commande commande = extractCommandeFromResultSet(rs);

                // Charger les produits associés à cette commande
                loadCommandeProduits(connection, commande);

                return commande;
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Error reading commande: " + e.getMessage());
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
                // Ne pas fermer la connexion ici
                // if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    private void loadCommandeProduits(Connection connection, Commande commande) throws SQLException {
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT p.* FROM produit p " +
                    "JOIN commande_produit cp ON p.id = cp.produit_id " +
                    "WHERE cp.commande_id = ?";

            statement = connection.prepareStatement(sql);
            statement.setInt(1, commande.getId());
            rs = statement.executeQuery();

            ProduitService produitService = new ProduitService();
            while (rs.next()) {
                Produit produit = new Produit();
                produit.setId(rs.getInt("id"));
                produit.setNom(rs.getString("nom"));
                produit.setQuantite(rs.getInt("quantite"));
                produit.setPrix(rs.getDouble("prix"));
                produit.setCategories(rs.getString("categories"));
                if (rs.getDate("date_creation_produit") != null) {
                    produit.setDate_creation_produit(rs.getDate("date_creation_produit"));
                }
                if (rs.getDate("date_modification_produit") != null) {
                    produit.setDate_modification_produit(rs.getDate("date_modification_produit"));
                }
                produit.setApproved(rs.getBoolean("approved"));
                produit.setDescription(rs.getString("description"));
                produit.setImage_file_name(rs.getString("image_file_name"));

                commande.addProduit(produit);
            }
        } finally {
            if (rs != null) rs.close();
            if (statement != null) statement.close();
        }
    }

    public List<Commande> readAll() {
        List<Commande> commandes = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            if (connection == null) {
                System.err.println("Cannot read all commandes: Database connection is null");
                return commandes;
            }

            String sql = "SELECT * FROM commande ORDER BY date_creation_commande DESC";
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);

            while (rs.next()) {
                Commande commande = extractCommandeFromResultSet(rs);
                loadCommandeProduits(connection, commande);
                commandes.add(commande);
            }
        } catch (SQLException e) {
            System.err.println("Error reading all commandes: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
                // Ne pas fermer la connexion ici
                // if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }

        return commandes;
    }

    public boolean update(Commande commande) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection();
            if (connection == null) {
                System.err.println("Cannot update commande: Database connection is null");
                return false;
            }

            // Vérifier dynamiquement les colonnes
            try {
                PreparedStatement checkStatement = connection.prepareStatement("DESCRIBE commande");
                ResultSet checkRs = checkStatement.executeQuery();

                StringBuilder setClause = new StringBuilder();
                List<String> columns = new ArrayList<>();

                while (checkRs.next()) {
                    String columnName = checkRs.getString("Field");
                    if (!columnName.equals("id") && !columnName.equals("date_creation_commande")) {
                        if (setClause.length() > 0) {
                            setClause.append(", ");
                        }
                        setClause.append(columnName).append(" = ?");
                        columns.add(columnName);
                    }
                }

                checkRs.close();
                checkStatement.close();

                String sql = "UPDATE commande SET " + setClause.toString() + " WHERE id = ?";
                System.out.println("Requête SQL update: " + sql);

                statement = connection.prepareStatement(sql);

                int paramIndex = 1;
                for (String column : columns) {
                    switch (column) {
                        case "quantite":
                            statement.setInt(paramIndex++, commande.getQuantite());
                            break;
                        case "prix":
                            statement.setDouble(paramIndex++, commande.getPrix());
                            break;
                        case "type_commande":
                            statement.setString(paramIndex++, commande.getType_commande());
                            break;
                        case "status":
                            statement.setString(paramIndex++, commande.getStatus());
                            break;
                        case "adress":
                            statement.setString(paramIndex++, commande.getAdress());
                            break;
                        case "paiment":
                            statement.setString(paramIndex++, commande.getPaiment());
                            break;
                        case "user_id":
                            // On ignore cette colonne dans notre modèle simplifié
                            statement.setInt(paramIndex++, 1); // Valeur par défaut pour user_id
                            break;
                    }
                }
                statement.setInt(paramIndex, commande.getId());

            } catch (SQLException e) {
                System.err.println("Erreur lors de la construction de la requête d'update: " + e.getMessage());

                // Fallback avec une requête fixe
                String sql = "UPDATE commande SET quantite = ?, prix = ?, type_commande = ?, " +
                        "status = ?, adress = ?, paiment = ? WHERE id = ?";

                statement = connection.prepareStatement(sql);
                statement.setInt(1, commande.getQuantite());
                statement.setDouble(2, commande.getPrix());
                statement.setString(3, commande.getType_commande());
                statement.setString(4, commande.getStatus());
                statement.setString(5, commande.getAdress());
                statement.setString(6, commande.getPaiment());
                statement.setInt(7, commande.getId());
            }

            int rowsAffected = statement.executeUpdate();
            statement.close();

            if (rowsAffected > 0) {
                // Mettre à jour les produits associés
                // D'abord, supprimer toutes les relations existantes
                String sql = "DELETE FROM commande_produit WHERE commande_id = ?";
                statement = connection.prepareStatement(sql);
                statement.setInt(1, commande.getId());
                statement.executeUpdate();
                statement.close();

                // Puis, insérer les nouvelles relations
                return insertCommandeProduits(connection, commande);
            }

            return false;
        } catch (SQLException e) {
            System.err.println("Error updating commande: " + e.getMessage());
            return false;
        } finally {
            try {
                if (statement != null) statement.close();
                // Ne pas fermer la connexion ici
                // if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    public boolean delete(int id) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection();
            if (connection == null) {
                System.err.println("Cannot delete commande: Database connection is null");
                return false;
            }

            // D'abord supprimer les relations dans la table de jointure
            String sql = "DELETE FROM commande_produit WHERE commande_id = ?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            statement.executeUpdate();
            statement.close();

            // Ensuite supprimer la commande
            sql = "DELETE FROM commande WHERE id = ?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting commande: " + e.getMessage());
            return false;
        } finally {
            try {
                if (statement != null) statement.close();
                // Ne pas fermer la connexion ici
                // if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    public List<Commande> searchByStatus(String status) {
        List<Commande> commandes = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            if (connection == null) {
                System.err.println("Cannot search commandes: Database connection is null");
                return commandes;
            }

            String sql = "SELECT * FROM commande WHERE status = ? ORDER BY date_creation_commande DESC";
            statement = connection.prepareStatement(sql);
            statement.setString(1, status);
            rs = statement.executeQuery();

            while (rs.next()) {
                Commande commande = extractCommandeFromResultSet(rs);
                loadCommandeProduits(connection, commande);
                commandes.add(commande);
            }
        } catch (SQLException e) {
            System.err.println("Error searching commandes by status: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
                // Ne pas fermer la connexion ici
                // if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }

        return commandes;
    }

    private Commande extractCommandeFromResultSet(ResultSet rs) throws SQLException {
        Commande commande = new Commande();
        commande.setId(rs.getInt("id"));

        try {
            commande.setQuantite(rs.getInt("quantite"));
        } catch (SQLException e) {
            // Si la colonne quantite n'existe pas, on utilise une valeur par défaut
            commande.setQuantite(1);
        }

        commande.setPrix(rs.getDouble("prix"));
        commande.setType_commande(rs.getString("type_commande"));
        commande.setStatus(rs.getString("status"));
        commande.setAdress(rs.getString("adress"));
        commande.setPaiment(rs.getString("paiment"));
        commande.setDate_creation_commande(rs.getDate("date_creation_commande"));
        commande.setUserId(rs.getInt("user_id"));

        return commande;
    }

    public List<Commande> getCommandesByUserId(int userId) {
        List<Commande> commandes = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            if (connection == null) {
                LOGGER.severe("Cannot get commandes: Database connection is null");
                return commandes;
            }

            String query = "SELECT * FROM commande WHERE user_id = ? ORDER BY date_creation_commande DESC";
            statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            rs = statement.executeQuery();

            while (rs.next()) {
                Commande commande = extractCommandeFromResultSet(rs);
                // Charger les produits associés à cette commande
                loadCommandeProduits(connection, commande);
                commandes.add(commande);

                LOGGER.info("Order ID: " + commande.getId() +
                        ", Status: " + commande.getStatus() +
                        ", Price: " + commande.getPrix() +
                        ", Number of products: " + commande.getProduits().size());
            }

            LOGGER.info("Found " + commandes.size() + " orders for user " + userId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving orders for user " + userId, e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing resources", e);
            }
        }

        return commandes;
    }

    public List<Commande> getCommandesByUser(int userId) {
        return getCommandesByUserId(userId);
    }

    public void generatePDF(Commande commande, Stage stage) {
        try {
            // Créer un nouveau document PDF
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);

            // Créer le contenu du PDF
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Configuration de la police et de la taille
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;
            float lineHeight = 20;

            // Titre
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Facture - AgriFarm");
            contentStream.endText();
            yPosition -= lineHeight * 2;

            // Informations de la commande
            contentStream.setFont(PDType1Font.HELVETICA, 12);

            // Date et numéro de commande
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("N° Commande: " + commande.getId());
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Date: " + commande.getDate_creation_commande());
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Statut: " + commande.getStatus());
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Mode de paiement: " + commande.getPaiment());
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Adresse de livraison: " + commande.getAdress());
            contentStream.endText();
            yPosition -= lineHeight * 2;

            // En-tête du tableau des produits
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Détails des produits:");
            contentStream.endText();
            yPosition -= lineHeight;

            // Colonnes du tableau
            float[] columnWidths = {200, 80, 80, 100};
            String[] headers = {"Produit", "Quantité", "Prix unit.", "Total"};
            float startX = margin;
            float tableWidth = 0;
            for (float width : columnWidths) {
                tableWidth += width;
            }

            // En-têtes des colonnes
            float currentX = startX;
            for (int i = 0; i < headers.length; i++) {
                contentStream.beginText();
                contentStream.newLineAtOffset(currentX, yPosition);
                contentStream.showText(headers[i]);
                contentStream.endText();
                currentX += columnWidths[i];
            }
            yPosition -= lineHeight;

            // Ligne de séparation
            contentStream.moveTo(startX, yPosition + 5);
            contentStream.lineTo(startX + tableWidth, yPosition + 5);
            contentStream.stroke();
            yPosition -= lineHeight;

            // Contenu du tableau
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            double sousTotal = 0;
            for (Produit produit : commande.getProduits()) {
                int quantite = commande.getQuantitesParProduit().get(produit.getId());
                double prixUnitaire = produit.getPrix();
                double totalProduit = prixUnitaire * quantite;
                sousTotal += totalProduit;

                currentX = startX;

                // Nom du produit
                contentStream.beginText();
                contentStream.newLineAtOffset(currentX, yPosition);
                contentStream.showText(produit.getNom());
                contentStream.endText();
                currentX += columnWidths[0];

                // Quantité
                contentStream.beginText();
                contentStream.newLineAtOffset(currentX, yPosition);
                contentStream.showText(String.valueOf(quantite));
                contentStream.endText();
                currentX += columnWidths[1];

                // Prix unitaire
                contentStream.beginText();
                contentStream.newLineAtOffset(currentX, yPosition);
                contentStream.showText(String.format("%.2f€", prixUnitaire));
                contentStream.endText();
                currentX += columnWidths[2];

                // Total du produit
                contentStream.beginText();
                contentStream.newLineAtOffset(currentX, yPosition);
                contentStream.showText(String.format("%.2f€", totalProduit));
                contentStream.endText();

                yPosition -= lineHeight;
            }

            // Ligne de séparation
            yPosition -= lineHeight;
            contentStream.moveTo(startX, yPosition + 5);
            contentStream.lineTo(startX + tableWidth, yPosition + 5);
            contentStream.stroke();
            yPosition -= lineHeight;

            // Sous-total
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(startX, yPosition);
            contentStream.showText("Sous-total: " + String.format("%.2f€", sousTotal));
            contentStream.endText();
            yPosition -= lineHeight;

            // Vérifier si une réduction a été appliquée
            if (sousTotal > commande.getPrix()) {
                double reduction = sousTotal - commande.getPrix();
                double pourcentageReduction = (reduction / sousTotal) * 100;

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, yPosition);
                contentStream.showText(String.format("Réduction (%.0f%%): -%.2f€", pourcentageReduction, reduction));
                contentStream.endText();
                yPosition -= lineHeight;
            }

            // Total final
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.beginText();
            contentStream.newLineAtOffset(startX, yPosition);
            contentStream.showText("Total: " + String.format("%.2f€", commande.getPrix()));
            contentStream.endText();

            // Fermer le flux de contenu
            contentStream.close();

            // Ouvrir une boîte de dialogue pour sauvegarder le fichier
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer la facture");
            fileChooser.setInitialFileName("facture_" + commande.getId() + ".pdf");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf")
            );

            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                document.save(file);
                document.close();

                // Afficher une confirmation
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("La facture a été générée avec succès !");
                alert.showAndWait();
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la génération du PDF", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue lors de la génération du PDF: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private User getUserById(int userId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            if (connection == null) {
                logger.log(Level.SEVERE, "Cannot get user: Database connection is null");
                return null;
            }

            String sql = "SELECT * FROM users WHERE id = ?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            rs = statement.executeQuery();

            if (rs.next()) {
                String email = rs.getString("email");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String password = rs.getString("password");
                List<String> roles = User.parseRolesFromJson(rs.getString("roles"));


            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting user by ID", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error closing resources", e);
            }
        }
        return null;
    }
}