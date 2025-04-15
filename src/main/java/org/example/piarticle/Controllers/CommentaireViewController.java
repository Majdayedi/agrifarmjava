package org.example.piarticle.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.piarticle.Entities.Commentaire;
import org.example.piarticle.Services.CommentaireService;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.net.URL;

public class CommentaireViewController {

    @FXML
    private Label commentCountLabel;

    @FXML
    private Label averageRatingLabel;

    @FXML
    private VBox commentsContainer;

    private Integer articleId;
    private boolean isAdminMode;
    private final CommentaireService commentaireService;
    private final SimpleDateFormat dateFormat;

    public CommentaireViewController() throws SQLException {
        this.commentaireService = new CommentaireService();
        this.dateFormat = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a");
    }

    @FXML
    private void initialize() {
        // Initialization code if needed
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
        loadComments();
    }

    public void setAdminMode(boolean isAdmin) {
        this.isAdminMode = isAdmin;
    }

    public void loadComments() {
        if (articleId == null) return;

        try {
            List<Commentaire> comments = commentaireService.getAllByArticleId(articleId);
            double avgRating = commentaireService.getAverageRating(articleId);
            int commentCount = commentaireService.getCommentCount(articleId);

            // Update UI
            commentCountLabel.setText(String.format("%d Comments", commentCount));
            if (commentCount > 0) {
                averageRatingLabel.setText(String.format("%.1f ⭐", avgRating));
            } else {
                averageRatingLabel.setText("No ratings yet");
            }

            // Clear and reload comments
            commentsContainer.getChildren().clear();
            for (Commentaire comment : comments) {
                addCommentToUI(comment);
            }

        } catch (SQLException e) {
            showError("Failed to load comments: " + e.getMessage());
        }
    }

    private void addCommentToUI(Commentaire comment) {
        VBox commentBox = new VBox(5);
        commentBox.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 5;");

        Label ratingLabel = new Label("⭐ ".repeat(comment.getRate()));
        ratingLabel.setStyle("-fx-text-fill: #f39c12;");

        Label dateLabel = new Label(dateFormat.format(comment.getCreatedAt()));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        Label contentLabel = new Label(comment.getCommentaire());
        contentLabel.setWrapText(true);

        commentBox.getChildren().addAll(ratingLabel, contentLabel, dateLabel);
        commentsContainer.getChildren().add(commentBox);
    }

    @FXML
    private void handleAddComment() {
        try {
            URL resource = getClass().getResource("/org/example/piarticle/add-commentaire.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            
            AddCommentaireController commentController = loader.getController();
            commentController.setArticleId(articleId);

            Stage commentStage = new Stage();
            commentStage.setTitle("Add Comment");
            commentStage.setScene(new Scene(root));
            commentStage.initModality(Modality.APPLICATION_MODAL);
            commentStage.initOwner(commentsContainer.getScene().getWindow());
            
            // Reload comments after the window is closed
            commentStage.setOnHidden(e -> loadComments());
            
            commentStage.showAndWait();

        } catch (IOException e) {
            showError("Failed to open comment form: " + e.getMessage());
        }
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 