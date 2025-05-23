package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import entite.Article;
import service.ArticleService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class ArticleFormController {

    @FXML
    private TextField titleField;

    @FXML
    private TextArea contentArea;

    @FXML
    private TextArea featuredTextArea;

    @FXML
    private TextField imageField;

    @FXML
    private ImageView imagePreview;

    @FXML
    private Button submitButton;

    private final ArticleService articleService;
    private File selectedImageFile;
    private static final String UPLOAD_DIR = "src/main/resources/org/example/piarticle/uploads";

    public ArticleFormController() throws SQLException {
        this.articleService = new ArticleService();
        // Create uploads directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(imageField.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            // Update preview
            Image image = new Image(file.toURI().toString());
            imagePreview.setImage(image);
            // Update image field with file name
            imageField.setText(file.getName());
        }
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        String featuredText = featuredTextArea.getText().trim();
        String imageUrl = imageField.getText().trim();

        if (title.isEmpty() || content.isEmpty() || featuredText.isEmpty()) {
            showAlert("Error", "Please fill in all required fields (title, content, and featured text).");
            return;
        }

        try {
            String finalImagePath = null;
            
            // Handle image file if selected
            if (selectedImageFile != null) {
                String fileName = System.currentTimeMillis() + "_" + selectedImageFile.getName();
                Path targetPath = Paths.get(UPLOAD_DIR, fileName);
                Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                finalImagePath = "uploads/" + fileName;
            } else if (!imageUrl.isEmpty()) {
                // Use provided URL if no file is selected
                finalImagePath = imageUrl;
            }

            Article article = new Article();
            article.setTitle(title);
            article.setContent(content);
            article.setFeaturedText(featuredText);
            article.setImage(finalImagePath);

            // We don't set the slug manually, letting the service generate a unique one

            articleService.add(article);
            showAlert("Success", "Article added successfully!");
            
            // Navigate to article details page instead of home page
            goToArticleDetails(article.getId());

        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Duplicate entry") && errorMessage.contains("UNIQ_")) {
                showAlert("Error", "An article with a similar title already exists. Please use a different title.");
            } else {
                showAlert("Error", "Failed to add article: " + errorMessage);
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to save image or navigate to article details page: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        try {
            goToHomePage();
        } catch (IOException e) {
            showAlert("Error", "Failed to navigate to home page: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void goToHomePage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/home.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    // Add a new method to navigate to article details
    private void goToArticleDetails(int articleId) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/article_details.fxml"));
        Parent root = loader.load();
        
        // Get the controller and pass the article ID
        ArticleDetailsController detailsController = loader.getController();
        detailsController.initData(articleId);
        
        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Article Details");
        stage.show();
    }
}
