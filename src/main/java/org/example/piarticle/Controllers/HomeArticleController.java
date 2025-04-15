package org.example.piarticle.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.piarticle.Entities.Article;
import org.example.piarticle.Services.ArticleService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

public class HomeArticleController {

    @FXML
    private VBox articleContainer;

    private final ArticleService articleService;

    public HomeArticleController() throws SQLException {
        this.articleService = new ArticleService();
    }

    // Load articles when the controller is initialized
    @FXML
    public void initialize() {
        try {
            List<Article> articles = articleService.getAll();
            displayArticles(articles);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load articles: " + e.getMessage());
        }
    }

    private void displayArticles(List<Article> articles) {
        articleContainer.getChildren().clear();
        
        if (articles.isEmpty()) {
            Label noArticlesLabel = new Label("No articles found. Create your first article!");
            noArticlesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
            articleContainer.getChildren().add(noArticlesLabel);
            return;
        }

        for (Article article : articles) {
            VBox articleBox = new VBox(10);
            articleBox.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

            Label titleLabel = new Label(article.getTitle());
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            Label featuredTextLabel = new Label(article.getFeaturedText());
            featuredTextLabel.setStyle("-fx-font-size: 14px;");
            featuredTextLabel.setWrapText(true);

            ImageView imageView = new ImageView();
            try {
                if (article.getImage() != null && !article.getImage().isEmpty()) {
                    // Load the article's image from the uploads directory on disk
                    String imagePath = "file:src/main/resources/org/example/piarticle/uploads/" + article.getImage();
                    Image image = new Image(imagePath, true);
                    imageView.setImage(image);
                } else {
                    // Load default agriculture image from uploads directory as resource
                    String defaultImagePath = getClass().getResource("/org/example/piarticle/uploads/agriculture.png").toExternalForm();
                    Image defaultImage = new Image(defaultImagePath);
                    imageView.setImage(defaultImage);
                }
                imageView.setFitHeight(200);
                imageView.setFitWidth(300);
                imageView.setPreserveRatio(true);
            } catch (Exception e) {
                System.out.println("Failed to load image for article: " + article.getId() + ", Error: " + e.getMessage());
                // Load default agriculture image on error
                try {
                    String defaultImagePath = getClass().getResource("/org/example/piarticle/uploads/agriculture.png").toExternalForm();
                    Image defaultImage = new Image(defaultImagePath);
                    imageView.setImage(defaultImage);
                } catch (Exception ex) {
                    System.out.println("Failed to load default image: " + ex.getMessage());
                }
            }

            // Add action buttons
            HBox actionButtons = new HBox(10);
            actionButtons.setStyle("-fx-padding: 10 0 0 0;");

            Button viewButton = new Button("👁️ View");
            viewButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            viewButton.setOnAction(e -> handleViewArticle(article));

            Button editButton = new Button("✏️ Edit");
            editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            editButton.setOnAction(e -> handleEditArticle(article));

            Button deleteButton = new Button("🗑️ Delete");
            deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            deleteButton.setOnAction(e -> handleDeleteArticle(article));

            actionButtons.getChildren().addAll(viewButton, editButton, deleteButton);

            articleBox.getChildren().addAll(titleLabel, imageView, featuredTextLabel, actionButtons);
            articleContainer.getChildren().add(articleBox);
        }
    }

    private void handleViewArticle(Article article) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/piarticle/article_details.fxml"));
            Parent root = loader.load();

            ArticleDetailsController controller = loader.getController();
            controller.setArticle(article);

            Stage stage = (Stage) articleContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to open article details: " + e.getMessage());
        }
    }

    private void handleEditArticle(Article article) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/piarticle/article_edit.fxml"));
            Parent root = loader.load();

            EditArticleController controller = loader.getController();
            controller.setArticle(article);

            Stage stage = (Stage) articleContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to open edit form: " + e.getMessage());
        }
    }

    private void handleDeleteArticle(Article article) {
        try {
            articleService.delete(article.getId());
            showAlert("Success", "Article deleted successfully!");
            initialize(); // Refresh the list
        } catch (SQLException e) {
            showAlert("Error", "Failed to delete article: " + e.getMessage());
        }
    }

    @FXML
    private void handleNewArticle() {
        try {
            URL fxmlUrl = getClass().getResource("/org/example/piarticle/article_form.fxml");
            if (fxmlUrl == null) {
                showAlert("Error", "Could not find article form FXML file!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = (Stage) articleContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to open article form: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/piarticle/admin_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) articleContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to open admin dashboard: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
