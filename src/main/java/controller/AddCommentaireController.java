package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import entite.Commentaire;
import service.CommentaireService;

import java.sql.SQLException;

public class AddCommentaireController {

    @FXML
    private TextArea commentArea;

    @FXML
    private Slider ratingSlider;

    @FXML
    private Label ratingLabel;

    @FXML
    private Button submitButton;

    private Integer articleId;
    private Commentaire editingComment;
    private final CommentaireService commentaireService;
    private boolean isEditMode = false;

    public AddCommentaireController() throws SQLException {
        this.commentaireService = new CommentaireService();
    }

    @FXML
    private void initialize() {
        // Update rating label when slider value changes
        ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int rating = newVal.intValue();
            ratingLabel.setText("Rating: " + "⭐".repeat(rating));
        });

        // Set initial rating
        ratingSlider.setValue(5);
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }

    public void setEditMode(Commentaire comment) {
        this.isEditMode = true;
        this.editingComment = comment;
        this.articleId = comment.getArticleId();
        
        // Populate fields with existing comment data
        commentArea.setText(comment.getCommentaire());
        ratingSlider.setValue(comment.getRate());
        submitButton.setText("Update Comment");
    }

    @FXML
    private void handleSubmit() {
        String commentText = commentArea.getText().trim();
        int rating = (int) ratingSlider.getValue();

        if (commentText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a comment.");
            return;
        }

        try {
            if (isEditMode) {
                // Update existing comment
                editingComment.setCommentaire(commentText);
                editingComment.setRate(rating);
                commentaireService.update(editingComment);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Comment updated successfully!");
            } else {
                // Create new comment
                Commentaire newComment = new Commentaire(articleId, rating, commentText);
                commentaireService.add(newComment);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Comment added successfully!");
            }

            // Close the window
            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                     isEditMode ? "Failed to update comment: " : "Failed to add comment: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 