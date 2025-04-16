package controller;

import entite.Article;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.ArticleService;
import service.CommentaireService;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class ArticleDashboardController {


        @FXML
        private TableView<Article> articlesTable;

        @FXML
        private TableColumn<Article, Integer> idColumn;

        @FXML
        private TableColumn<Article, String> titleColumn;

        @FXML
        private TableColumn<Article, String> contentColumn;

        @FXML
        private TableColumn<Article, String> dateColumn;

        @FXML
        private TableColumn<Article, Void> actionsColumn;

        @FXML
        private TableColumn<Article, Integer> commentCountColumn;

        private final ArticleService articleService;
        private final CommentaireService commentaireService;
        private final SimpleDateFormat dateFormat;

        public ArticleDashboardController() throws SQLException {
            this.articleService = new ArticleService();
            this.commentaireService = new CommentaireService();
            this.dateFormat = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a");
        }

    public ArticleDashboardController(ArticleService articleService, CommentaireService commentaireService, SimpleDateFormat dateFormat) {
        this.articleService = articleService;
        this.commentaireService = commentaireService;
        this.dateFormat = dateFormat;
    }

    @FXML
        private void initialize() {
            setupTableColumns();
            loadArticles();
        }

        private void setupTableColumns() {
            idColumn.setCellValueFactory(cellData ->
                    new SimpleIntegerProperty(cellData.getValue().getId()).asObject()
            );

            titleColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getTitle())
            );
            titleColumn.setCellFactory(tc -> {
                TableCell<Article, String> cell = new TableCell<>() {
                    private final Text text = new Text();
                    {
                        setGraphic(text);
                        text.wrappingWidthProperty().bind(titleColumn.widthProperty().subtract(10));
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            text.setText(null);
                            setGraphic(null);
                        } else {
                            text.setText(item);
                            setGraphic(text);
                        }
                    }
                };
                return cell;
            });

            contentColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getContent())
            );
            contentColumn.setCellFactory(tc -> {
                TableCell<Article, String> cell = new TableCell<>() {
                    private final Text text = new Text();
                    {
                        setGraphic(text);
                        text.wrappingWidthProperty().bind(contentColumn.widthProperty().subtract(10));
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            text.setText(null);
                            setGraphic(null);
                        } else {
                            String displayText = item.length() > 100 ? item.substring(0, 100) + "..." : item;
                            text.setText(displayText);
                            setGraphic(text);
                        }
                    }
                };
                return cell;
            });

            dateColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(
                            dateFormat.format(cellData.getValue().getCreatedAt())
                    )
            );

            commentCountColumn.setCellValueFactory(cellData -> {
                Article article = cellData.getValue();
                try {
                    int commentCount = commentaireService.getCommentCount(article.getId());
                    return new SimpleIntegerProperty(commentCount).asObject();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return new SimpleIntegerProperty(0).asObject();
                }
            });

            actionsColumn.setCellFactory(param -> new TableCell<>() {
                private final Button deleteBtn = new Button("Delete");
                private final Button viewCommentsBtn = new Button("View");

                {
                    deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                    viewCommentsBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

                    deleteBtn.setOnAction(event -> {
                        Article article = getTableView().getItems().get(getIndex());
                        handleDelete(article);
                    });

                    viewCommentsBtn.setOnAction(event -> {
                        Article article = getTableView().getItems().get(getIndex());
                        handleViewComments(article);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        HBox buttons = new HBox(5);
                        buttons.getChildren().addAll(deleteBtn, viewCommentsBtn);
                        setGraphic(buttons);
                    }
                }
            });
        }

        private void loadArticles() {
            try {
                List<Article> articles = articleService.getAll();
                articlesTable.setItems(FXCollections.observableArrayList(articles));
            } catch (SQLException e) {
                showAlert("Error", "Failed to load articles: " + e.getMessage());
            }
        }

        @FXML
        private void handleNewArticle() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/article_form.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) articlesTable.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                showAlert("Error", "Failed to open article form: " + e.getMessage());
            }
        }

        private void handleDelete(Article article) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirm Delete");
            confirmDialog.setHeaderText(null);
            confirmDialog.setContentText("Are you sure you want to delete this article?");

            if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try {
                    articleService.delete(article.getId());
                    loadArticles(); // Refresh the table
                    showAlert("Success", "Article deleted successfully!");
                } catch (SQLException e) {
                    showAlert("Error", "Failed to delete article: " + e.getMessage());
                }
            }
        }

        private void handleViewComments(Article article) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/article_details.fxml"));
                Parent root = loader.load();

                ArticleDetailsController controller = loader.getController();
                controller.setArticle(article);
                controller.setAdminMode(true);

                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Comments for Article: " + article.getTitle());
                stage.setScene(new Scene(root));
                stage.showAndWait();

                // Refresh the table to update comment counts
                loadArticles();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Could not open comments view: " + e.getMessage());
            }
        }

        @FXML
        private void handleBack() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/home.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) articlesTable.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                showAlert("Error", "Failed to navigate back: " + e.getMessage());
            }
        }

        @FXML
        private void handleManageComments() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/admin_comments.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) articlesTable.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                showAlert("Error", "Failed to open comments management: " + e.getMessage());
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

