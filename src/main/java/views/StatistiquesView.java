package views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.chart.XYChart;
import entite.Commande;
import service.CommandeService;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import java.util.logging.Logger;
import java.util.logging.Level;

public class StatistiquesView {
    private static final Logger LOGGER = Logger.getLogger(StatistiquesView.class.getName());
    private final CommandeService commandeService;
    private final int userId;

    public StatistiquesView(int userId) {
        this.commandeService = new CommandeService();
        this.userId = userId;
    }

    public void show() {
        Stage stage = new Stage();
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Titre
        Label titleLabel = new Label("Statistiques de vos achats");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        // Récupérer les commandes de l'utilisateur
        List<Commande> commandes = commandeService.getCommandesByUserId(userId);
        LOGGER.info("Récupération de " + commandes.size() + " commandes pour l'utilisateur " + userId);

        // Statistiques générales
        VBox statsBox = createGeneralStats(commandes);

        // Graphique des dépenses mensuelles
        BarChart<String, Number> depensesChart = createMonthlyExpensesChart(commandes);
        
        // Graphique de répartition des statuts
        PieChart statusChart = createStatusDistributionChart(commandes);

        // Layout pour les graphiques
        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.getChildren().addAll(depensesChart, statusChart);

        root.getChildren().addAll(titleLabel, statsBox, chartsBox);

        Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("Statistiques des achats");
        stage.setScene(scene);
        stage.show();
    }

    private VBox createGeneralStats(List<Commande> commandes) {
        VBox statsBox = new VBox(10);
        statsBox.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 5;");

        double totalDepense = commandes.stream()
                .mapToDouble(Commande::getPrix)
                .sum();

        int nombreCommandes = commandes.size();

        double moyenneParCommande = nombreCommandes > 0 ? totalDepense / nombreCommandes : 0;

        Label totalLabel = createStatsLabel("Total des dépenses", String.format("%.2f €", totalDepense));
        Label nombreLabel = createStatsLabel("Nombre de commandes", String.valueOf(nombreCommandes));
        Label moyenneLabel = createStatsLabel("Moyenne par commande", String.format("%.2f €", moyenneParCommande));

        statsBox.getChildren().addAll(totalLabel, nombreLabel, moyenneLabel);
        return statsBox;
    }

    private Label createStatsLabel(String title, String value) {
        Label label = new Label(title + ": " + value);
        label.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        label.setTextFill(Color.web("#34495e"));
        return label;
    }

    private BarChart<String, Number> createMonthlyExpensesChart(List<Commande> commandes) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        
        barChart.setTitle("Dépenses mensuelles");
        xAxis.setLabel("Mois");
        yAxis.setLabel("Montant (€)");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Dépenses");

        // Créer un Map pour stocker les dépenses par mois
        Map<String, Double> depensesParMois = new TreeMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

        for (Commande commande : commandes) {
            String mois = new java.sql.Timestamp(commande.getDate_creation_commande().getTime())
                .toLocalDateTime().format(formatter);
            depensesParMois.merge(mois, commande.getPrix(), Double::sum);
        }

        // Ajouter les données au graphique
        for (Map.Entry<String, Double> entry : depensesParMois.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barChart.getData().add(series);
        barChart.setMaxWidth(500);
        return barChart;
    }

    private PieChart createStatusDistributionChart(List<Commande> commandes) {
        // Compter le nombre de commandes par statut
        Map<String, Integer> statusCount = new HashMap<>();
        for (Commande commande : commandes) {
            statusCount.merge(commande.getStatus(), 1, Integer::sum);
        }

        // Créer le graphique
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Répartition des statuts de commandes");

        // Ajouter les données
        for (Map.Entry<String, Integer> entry : statusCount.entrySet()) {
            pieChart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        pieChart.setMaxWidth(400);
        return pieChart;
    }
} 