package controller;

import entite.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.Session;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    public ImageView backgroundImage;
    public VBox marketplaceCard;

    @FXML
    private Button farmButton;

    @FXML
    private Button marketplaceButton;

    @FXML
    private Button articlesButton;

    @FXML
    private Button profileButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Label welcomeLabel;

    @FXML
    private ImageView userProfileImage;

    private User currentUser;
    private Session session;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        session = Session.getInstance();

        // Only setup interface if user is already set or session is active
        if (currentUser != null || session.isLoggedIn()) {
            if (currentUser == null) {
                currentUser = session.getUser();
            }
            setupUserInterface();
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        Session.getInstance().setUser(user);

        // Safe check: only update interface if FXML fields are already initialized
        if (welcomeLabel != null) {
            setupUserInterface();
        }
    }

    private void setupUserInterface() {
        if (currentUser == null) return;

        // ✅ Print the logged-in user's ID to the console
        System.out.println("Logged-in User ID: " + currentUser.getId());

        welcomeLabel.setText("Welcome, " + currentUser.getFirstName() + "!");

        if (currentUser.getImageFileName() != null && !currentUser.getImageFileName().isEmpty()) {
            String imagePath = "src/user_data/profile_pics/" + currentUser.getImageFileName();
            File imageFile = new File(imagePath);

            if (imageFile.exists()) {
                userProfileImage.setImage(new Image(imageFile.toURI().toString()));
            } else {
                loadDefaultProfileImage();
            }
        } else {
            loadDefaultProfileImage();
        }

        boolean isAdmin = session.isAdmin();

        if (isAdmin) {
            // Admin-specific UI logic here
        }
    }


    private void loadDefaultProfileImage() {
        try {
            URL defaultImageUrl = getClass().getResource("/profile_pics/default.jpg");
            if (defaultImageUrl != null) {
                userProfileImage.setImage(new Image(defaultImageUrl.toExternalForm()));
            }
        } catch (Exception e) {
            System.err.println("Error loading default profile image: " + e.getMessage());
        }
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/login.fxml"));
            Parent loginView = loader.load();

            Scene currentScene = farmButton.getScene();
            if (currentScene != null) {
                Stage primaryStage = (Stage) currentScene.getWindow();
                primaryStage.setTitle("Login");
                primaryStage.setScene(new Scene(loginView, 900, 600));
            }
        } catch (IOException e) {
            showAlert("Error loading Login view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void navigateToFarm() {
        if (!ensureUserLoggedIn()) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/farmdisplay.fxml"));
            Parent farmView = loader.load();

            Stage primaryStage = (Stage) farmButton.getScene().getWindow();
            primaryStage.setTitle("Farm Management");
            primaryStage.setScene(new Scene(farmView, 900, 600));
        } catch (IOException e) {
            showAlert("Error loading Farm view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void navigateToMarketplace() {
        if (!ensureUserLoggedIn()) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/produit.fxml"));
            Parent marketplaceView = loader.load();

            Stage primaryStage = (Stage) marketplaceButton.getScene().getWindow();
            primaryStage.setTitle("Marketplace");
            primaryStage.setScene(new Scene(marketplaceView, 900, 600));
        } catch (IOException e) {
            showAlert("Error loading Marketplace view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void navigateToArticles() {
        if (!ensureUserLoggedIn()) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/article_form.fxml"));
            Parent articlesView = loader.load();

            Stage primaryStage = (Stage) articlesButton.getScene().getWindow();
            primaryStage.setTitle("Articles");
            primaryStage.setScene(new Scene(articlesView, 900, 600));
        } catch (IOException e) {
            showAlert("Error loading Articles view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void navigateToProfile() {
        if (!ensureUserLoggedIn()) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/view_profile.fxml"));
            Parent profileView = loader.load();

            ViewProfileController controller = loader.getController();
            controller.setUserDetail(currentUser);

            Stage primaryStage = (Stage) profileButton.getScene().getWindow();
            primaryStage.setTitle("User Profile");
            primaryStage.setScene(new Scene(profileView, 900, 600));
        } catch (IOException e) {
            showAlert("Error loading Profile view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout() {
        session.clearSession();
        redirectToLogin();
    }

    private boolean ensureUserLoggedIn() {
        if (!session.isLoggedIn()) {
            showAlert("Please log in to access this feature.");
            redirectToLogin();
            return false;
        }
        return true;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
