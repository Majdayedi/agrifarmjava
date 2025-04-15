package org.example.piarticle.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.piarticle.Entities.Article;
import org.example.piarticle.Entities.Commentaire;
import org.example.piarticle.Services.ArticleService;
import org.example.piarticle.Services.CommentaireService;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class AdminCommentsController {

    @FXML
    private VBox commentsContainer;

    @FXML
    private Label totalCommentsLabel;

    @FXML
    private Button backButton;

    private final CommentaireService commentaireService;
    private final ArticleService articleService;
    private final SimpleDateFormat dateFormat;

    public AdminCommentsController() throws SQLException {
        this.commentaireService = new CommentaireService();
        this.articleService = new ArticleService();
        this.dateFormat = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");
    }

    @FXML
    private void initialize() {
        loadAllComments();
    }

    private void loadAllComments() {
        try {
            commentsContainer.getChildren().clear();
            List<Commentaire> comments = commentaireService.getAll(); // We'll need to add this method to CommentaireService
            totalCommentsLabel.setText(String.format("Total Comments: %d", comments.size()));

            for (Commentaire comment : comments) {
                addCommentToUI(comment);
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load comments: " + e.getMessage());
        }
    }

    private void addCommentToUI(Commentaire comment) {
        try {
            VBox commentBox = new VBox(5);
            commentBox.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-margin: 5;");

            // Get the article title
            Article article = articleService.getById(comment.getArticleId());
            Label articleLabel = new Label("Article: " + article.getTitle());
            articleLabel.setStyle("-fx-font-weight: bold;");

            Label ratingLabel = new Label("Rating: " + "⭐".repeat(comment.getRate()));
            ratingLabel.setStyle("-fx-text-fill: #f39c12;");

            Label dateLabel = new Label("Posted on: " + dateFormat.format(comment.getCreatedAt()));
            dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

            // Create HBox for comment content and buttons
            HBox contentBox = new HBox(10);
            Label contentLabel = new Label(comment.getCommentaire());
            contentLabel.setWrapText(true);
            HBox.setHgrow(contentLabel, Priority.ALWAYS);

            // Buttons container
            HBox buttonsBox = new HBox(5);
            Button deleteButton = new Button("Delete");
            Button viewArticleButton = new Button("View Article");

            deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
            viewArticleButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");

            buttonsBox.getChildren().addAll(deleteButton, viewArticleButton);

            contentBox.getChildren().addAll(contentLabel, buttonsBox);

            // Add all elements to the comment box
            commentBox.getChildren().addAll(articleLabel, ratingLabel, contentBox, dateLabel);

            // Add separator
            Separator separator = new Separator();
            separator.setStyle("-fx-margin: 10;");

            commentsContainer.getChildren().addAll(commentBox, separator);

            // Button actions
            deleteButton.setOnAction(e -> handleDeleteComment(comment));
            viewArticleButton.setOnAction(e -> handleViewArticle(article));

        } catch (SQLException e) {
            System.err.println("Failed to load article for comment: " + e.getMessage());
        }
    }

    private void handleEditComment(Commentaire comment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/piarticle/add-commentaire.fxml"));
            Parent root = loader.load();
            
            AddCommentaireController commentController = loader.getController();
            commentController.setEditMode(comment);

            Stage commentStage = new Stage();
            commentStage.setTitle("Edit Comment");
            commentStage.setScene(new Scene(root));
            commentStage.initModality(Modality.APPLICATION_MODAL);
            commentStage.initOwner(backButton.getScene().getWindow());
            
            commentStage.setOnHidden(e -> loadAllComments());
            commentStage.showAndWait();

        } catch (IOException e) {
            showAlert("Error", "Failed to open edit form: " + e.getMessage());
        }
    }

    private void handleDeleteComment(Commentaire comment) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Are you sure you want to delete this comment?");

        if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                commentaireService.delete(comment.getId(), comment.getArticleId());
                loadAllComments();
                showAlert("Success", "Comment deleted successfully!");
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete comment: " + e.getMessage());
            }
        }
    }

    private void handleViewArticle(Article article) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/piarticle/article_details.fxml"));
            Parent root = loader.load();

            ArticleDetailsController controller = loader.getController();
            controller.setArticle(article);
            controller.setAdminMode(true);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to open article: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/piarticle/admin_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to navigate back: " + e.getMessage());
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