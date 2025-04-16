package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import entite.Article;
import entite.Commentaire;
import service.ArticleService;
import service.CommentaireService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class ArticleDetailsController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label contentLabel;

    @FXML
    private Label featuredTextLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private ImageView articleImage;

    @FXML
    private Button backButton;

    @FXML
    private Button editButton;

    @FXML
    private Label commentCountLabel;

    @FXML
    private Label averageRatingLabel;

    @FXML
    private VBox commentsContainer;

    @FXML
    private CommentaireViewController commentaireViewControllerController;

    private Article currentArticle;
    private final ArticleService articleService;
    private final CommentaireService commentaireService;
    private boolean isAdminMode = false;

    public ArticleDetailsController() throws SQLException {
        this.articleService = new ArticleService();
        this.commentaireService = new CommentaireService();
    }

    @FXML
    private void initialize() {
        // Any initialization code if needed
    }

    public void setArticle(Article article) {
        this.currentArticle = article;
        populateFields();
        if (commentaireViewControllerController != null) {
            commentaireViewControllerController.setArticleId(article.getId());
            commentaireViewControllerController.setAdminMode(isAdminMode);
            commentaireViewControllerController.loadComments();
        }
    }

    private void populateFields() {
        if (currentArticle != null) {
            titleLabel.setText(currentArticle.getTitle());
            contentLabel.setText(currentArticle.getContent());
            featuredTextLabel.setText(currentArticle.getFeaturedText());
            
            // Format and display the creation date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");
            dateLabel.setText("Created on: " + dateFormat.format(currentArticle.getCreatedAt()));

            // Load and display the image if available
            if (currentArticle.getImage() != null && !currentArticle.getImage().isEmpty()) {
                try {
                    String imagePath = "file:src/main/resources/uploads/" + currentArticle.getImage().replace("uploads/", "");
                    Image image = new Image(imagePath, true);
                    articleImage.setImage(image);
                    articleImage.setFitWidth(600); // Set a reasonable max width
                    articleImage.setPreserveRatio(true);
                    articleImage.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
                    articleImage.setVisible(true);
                } catch (Exception e) {
                    System.err.println("Failed to load image: " + e.getMessage());
                    articleImage.setVisible(false);
                }
            } else {
                // Hide the ImageView if no image is available
                articleImage.setVisible(false);
            }
        }
    }

    private void loadComments() {
        try {
            List<Commentaire> comments = commentaireService.getAllByArticleId(currentArticle.getId());
            double avgRating = commentaireService.getAverageRating(currentArticle.getId());
            int commentCount = commentaireService.getCommentCount(currentArticle.getId());

            // Update UI
            commentCountLabel.setText(String.format("%d Comments", commentCount));
            averageRatingLabel.setText(String.format("%.1f ⭐", avgRating));

            // Clear and reload comments
            commentsContainer.getChildren().clear();
            for (Commentaire comment : comments) {
                addCommentToUI(comment);
            }

        } catch (SQLException e) {
            showAlert("Error", "Failed to load comments: " + e.getMessage());
        }
    }

    private void addCommentToUI(Commentaire comment) {
        VBox commentBox = new VBox(5);
        commentBox.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 5;");

        Label ratingLabel = new Label("⭐ ".repeat(comment.getRate()));
        ratingLabel.setStyle("-fx-text-fill: #f39c12;");

        Label dateLabel = new Label(comment.getCreatedAt().toString());
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        Label contentLabel = new Label(comment.getCommentaire());
        contentLabel.setWrapText(true);

        // Create an HBox for the content and buttons
        HBox contentBox = new HBox(10); // 10 pixels spacing
        contentBox.getChildren().add(contentLabel);
        HBox.setHgrow(contentLabel, Priority.ALWAYS); // Make content label take up available space

        // Create buttons container
        HBox buttonsBox = new HBox(5); // 5 pixels spacing between buttons

        // Create edit button
        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        editButton.setOnAction(e -> handleEditComment(comment));

        // Create delete button
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> handleDeleteComment(comment));

        // Add buttons to the buttons container
        buttonsBox.getChildren().addAll(editButton, deleteButton);
        contentBox.getChildren().add(buttonsBox);

        commentBox.getChildren().addAll(ratingLabel, contentBox, dateLabel);
        commentsContainer.getChildren().add(commentBox);
    }

    private void handleEditComment(Commentaire comment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/add-commentaire.fxml"));
            Parent root = loader.load();
            
            AddCommentaireController commentController = loader.getController();
            commentController.setEditMode(comment);

            Stage commentStage = new Stage();
            commentStage.setTitle("Edit Comment");
            commentStage.setScene(new Scene(root));
            commentStage.initModality(Modality.APPLICATION_MODAL);
            commentStage.initOwner(backButton.getScene().getWindow());
            
            // Reload comments after the edit window is closed
            commentStage.setOnHidden(e -> {
                if (commentaireViewControllerController != null) {
                    commentaireViewControllerController.loadComments();
                } else {
                    loadComments();
                }
            });
            
            commentStage.showAndWait();

        } catch (IOException e) {
            showAlert("Error", "Failed to open edit comment form: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddComment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/add-commentaire.fxml"));
            Parent root = loader.load();
            
            AddCommentaireController commentController = loader.getController();
            commentController.setArticleId(currentArticle.getId());

            Stage commentStage = new Stage();
            commentStage.setTitle("Add Comment");
            commentStage.setScene(new Scene(root));
            commentStage.initModality(Modality.APPLICATION_MODAL);
            commentStage.initOwner(backButton.getScene().getWindow());
            
            // Reload comments after the comment window is closed
            commentStage.setOnHidden(e -> {
                if (commentaireViewControllerController != null) {
                    commentaireViewControllerController.loadComments();
                }
            });
            
            commentStage.showAndWait();

        } catch (IOException e) {
            showAlert("Error", "Failed to open comment form: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            URL resource = getClass().getResource("/controller/home.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to navigate back: " + e.getMessage());
        }
    }

    @FXML
    private void handleEdit() {
        try {
            URL resource = getClass().getResource("/controller/article_edit.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            EditArticleController controller = loader.getController();
            controller.setArticle(currentArticle);

            Stage stage = (Stage) editButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
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
                if (commentaireViewControllerController != null) {
                    commentaireViewControllerController.loadComments();
                } else {
                    loadComments();
                }
                showAlert("Success", "Comment deleted successfully!");
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete comment: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setAdminMode(boolean isAdmin) {
        this.isAdminMode = isAdmin;
        if (commentaireViewControllerController != null) {
            commentaireViewControllerController.setAdminMode(isAdmin);
        }
    }
} 