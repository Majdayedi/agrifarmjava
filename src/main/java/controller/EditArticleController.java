package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import entite.Article;
import service.ArticleService;

import java.io.IOException;
import java.sql.SQLException;

public class EditArticleController {

    @FXML
    private TextField titleField;

    @FXML
    private TextArea contentArea;

    @FXML
    private TextArea featuredTextArea;

    @FXML
    private TextField imageField;

    @FXML
    private Button saveButton;

    private Article currentArticle;
    private final ArticleService articleService;

    public EditArticleController() throws SQLException {
        this.articleService = new ArticleService();
    }

    public void setArticle(Article article) {
        this.currentArticle = article;
        populateFields();
    }

    private void populateFields() {
        if (currentArticle != null) {
            titleField.setText(currentArticle.getTitle());
            contentArea.setText(currentArticle.getContent());
            featuredTextArea.setText(currentArticle.getFeaturedText());
            imageField.setText(currentArticle.getImage() != null ? currentArticle.getImage() : "");
        }
    }

    @FXML
    private void handleSave() {
        if (currentArticle == null) {
            showAlert("Error", "No article loaded for editing.");
            return;
        }

        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        String featuredText = featuredTextArea.getText().trim();
        String image = imageField.getText().trim();

        if (title.isEmpty() || content.isEmpty() || featuredText.isEmpty()) {
            showAlert("Error", "Please fill in all required fields (title, content, and featured text).");
            return;
        }

        try {
            currentArticle.setTitle(title);
            currentArticle.setContent(content);
            currentArticle.setFeaturedText(featuredText);
            currentArticle.setImage(image.isEmpty() ? null : image);

            articleService.update(currentArticle);
            showAlert("Success", "Article updated successfully!");
            goToArticleDetails();

        } catch (SQLException e) {
            showAlert("Error", "Failed to update article: " + e.getMessage());
        } catch (IOException e) {
            showAlert("Error", "Failed to navigate to article details page: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        try {
            goToArticleDetails();
        } catch (IOException e) {
            showAlert("Error", "Failed to navigate to article details page: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddComment() {
        if (currentArticle == null || currentArticle.getId() <= 0) {
            showAlert("Error", "Please save the article before adding comments.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/add-commentaire.fxml"));
            Parent root = loader.load();
            
            AddCommentaireController commentController = loader.getController();
            commentController.setArticleId(currentArticle.getId());

            Stage commentStage = new Stage();
            commentStage.setTitle("Add Comment");
            commentStage.setScene(new Scene(root));
            commentStage.initModality(Modality.APPLICATION_MODAL);
            commentStage.initOwner(saveButton.getScene().getWindow());
            commentStage.showAndWait();

        } catch (IOException e) {
            showAlert("Error", "Failed to open comment form: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Navigate to the article details page for the current article
     */
    private void goToArticleDetails() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/article_details.fxml"));
        Parent root = loader.load();
        
        // Get the controller and pass the article
        ArticleDetailsController detailsController = loader.getController();
        detailsController.setArticle(currentArticle);
        
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Article Details");
        stage.show();
    }
}
