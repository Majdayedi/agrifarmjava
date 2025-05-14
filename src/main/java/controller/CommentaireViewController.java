package controller;

import entite.Commentaire;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.CommentaireService;


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

        // Create buttons container
        HBox buttonsContainer = new HBox(5);
        buttonsContainer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        // Create edit button
        Button editButton = new Button("✏️ Edit");
        editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        editButton.setOnAction(e -> handleEditComment(comment));

        // Create delete button
        Button deleteButton = new Button("🗑️ Delete");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> handleDeleteComment(comment));

        // Add buttons to container
        buttonsContainer.getChildren().addAll(editButton, deleteButton);

        Label ratingLabel = new Label("⭐ ".repeat(comment.getRate()));
        ratingLabel.setStyle("-fx-text-fill: #f39c12;");

        String dateText = (comment.getCreatedAt() != null)
                ? dateFormat.format(comment.getCreatedAt())
                : "No date";
        Label dateLabel = new Label(dateText);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        Label contentLabel = new Label(comment.getCommentaire());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        commentBox.getChildren().addAll(buttonsContainer, ratingLabel, contentLabel, dateLabel);
        commentsContainer.getChildren().add(commentBox);
    }

    private void handleEditComment(Commentaire comment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/add-commentaire.fxml"));
            Parent root = loader.load();
            AddCommentaireController controller = loader.getController();
            controller.setEditMode(comment);
            controller.setArticleId(articleId);

            Stage stage = new Stage();
            stage.setTitle("Edit Comment");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Reload comments after editing
            loadComments();
        } catch (IOException e) {
            showError("Failed to open edit form: " + e.getMessage());
        }
    }

    private void handleDeleteComment(Commentaire comment) {
        try {
            commentaireService.delete(comment.getId(), comment.getArticleId());
            loadComments();
        } catch (SQLException e) {
            showError("Failed to delete comment: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddComment() {
        try {
            URL resource = getClass().getResource("/controller/add-commentaire.fxml");
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