package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import entite.Commentaire;
import service.CommentaireService;

import java.io.IOException;
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

            // Navigate to commentaire_view.fxml instead of just closing
            navigateToCommentaireView();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                     isEditMode ? "Failed to update comment: " : "Failed to add comment: " + e.getMessage());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to navigate to comments view: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        try {
            // Navigate to commentaire_view.fxml instead of just closing
            navigateToCommentaireView();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to navigate to comments view: " + e.getMessage());
            // If navigation fails, fall back to just closing the window
            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Navigate to the commentaire_view.fxml page
     */
    private void navigateToCommentaireView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/commentaire_view.fxml"));
        Parent root = loader.load();
        
        // Get the controller and pass the article ID
        CommentaireViewController viewController = loader.getController();
        viewController.setArticleId(articleId);
        
        // Get the current stage and set the new scene
        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Comments");
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 