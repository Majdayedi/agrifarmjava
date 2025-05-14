package controller;

import entite.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import utils.Session;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML
    private VBox marketplaceCard;

    @FXML
    private Button farmButton, marketplaceButton, articlesButton, profileButton, logoutButton;

    @FXML
    private Label welcomeLabel;

    @FXML
    private ImageView userProfileImage;

    private User currentUser;
    private Session session;

    private static final String SHARED_PROFILE_DIR = "C:\\shared-profile-pics\\";
    private static final String DEFAULT_IMAGE_RESOURCE = "/images/default.jpg"; // Embedded fallback

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        session = Session.getInstance();
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
        if (welcomeLabel != null) {
            setupUserInterface();
        }
    }

    private void setupUserInterface() {
        if (currentUser == null) return;

        System.out.println("Logged-in User ID: " + currentUser.getId());

        welcomeLabel.setText("Welcome, " + currentUser.getFirstName() + "!");

        loadProfileImage(currentUser);

        boolean isAdmin = session.isAdmin();
        if (isAdmin) {
            // Admin-specific logic here
        }
    }

    private void loadProfileImage(User user) {
        String imageFileName = user.getImageFileName();

        if (StringUtils.isNotBlank(imageFileName)) {
            File imageFile = new File(SHARED_PROFILE_DIR + imageFileName);
            System.out.println("Checking image in: " + imageFile.getAbsolutePath());

            if (imageFile.exists()) {
                String imageUrl = imageFile.toURI().toString();
                System.out.println("Loaded image from shared folder.");
                userProfileImage.setImage(new Image(imageUrl));
                return;
            }

            File legacyFile = new File("src/user_data/profile_pics/" + imageFileName);
            if (legacyFile.exists()) {
                try {
                    Files.copy(
                            legacyFile.toPath(),
                            Paths.get(SHARED_PROFILE_DIR + imageFileName),
                            StandardCopyOption.REPLACE_EXISTING
                    );
                    userProfileImage.setImage(new Image(legacyFile.toURI().toString()));
                    System.out.println("Migrated image from legacy folder.");
                    return;
                } catch (IOException e) {
                    System.err.println("Failed to migrate profile image: " + e.getMessage());
                }
            }

            System.out.println("Profile image not found for: " + imageFileName);
        }

        loadDefaultProfileImage();
    }

    private void loadDefaultProfileImage() {
        try {
            File defaultFile = new File(SHARED_PROFILE_DIR + "default.jpg");
            if (defaultFile.exists()) {
                userProfileImage.setImage(new Image(defaultFile.toURI().toString()));
                System.out.println("Loaded default image from shared folder.");
                return;
            }

            InputStream defaultStream = getClass().getResourceAsStream(DEFAULT_IMAGE_RESOURCE);
            if (defaultStream != null) {
                userProfileImage.setImage(new Image(defaultStream));
                System.out.println("Loaded default image from resources.");
            } else {
                System.err.println("Default image not found in resources: " + DEFAULT_IMAGE_RESOURCE);
                userProfileImage.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Error loading default profile image: " + e.getMessage());
            userProfileImage.setImage(null);
        }
    }

    @FXML
    public void navigateToFarm() {
        if (!ensureUserLoggedIn()) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/farmdisplay.fxml"));
            Parent farmView = loader.load();
            Stage stage = (Stage) farmButton.getScene().getWindow();
            stage.setTitle("Farm Management");
            stage.setScene(new Scene(farmView, 900, 600));
        } catch (IOException e) {
            showAlert("Error loading Farm view: " + e.getMessage());
        }
    }

    @FXML
    public void navigateToMarketplace() {
        if (!ensureUserLoggedIn()) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/produit.fxml"));
            Parent view = loader.load();
            Stage stage = (Stage) marketplaceButton.getScene().getWindow();
            stage.setTitle("Marketplace");
            stage.setScene(new Scene(view, 900, 600));
        } catch (IOException e) {
            showAlert("Error loading Marketplace view: " + e.getMessage());
        }
    }

    @FXML
    public void navigateToArticles() {
        if (!ensureUserLoggedIn()) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/article_form.fxml"));
            Parent view = loader.load();
            Stage stage = (Stage) articlesButton.getScene().getWindow();
            stage.setTitle("Articles");
            stage.setScene(new Scene(view, 900, 600));
        } catch (IOException e) {
            showAlert("Error loading Articles view: " + e.getMessage());
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

            Stage stage = (Stage) profileButton.getScene().getWindow();
            stage.setTitle("User Profile");
            stage.setScene(new Scene(profileView, 900, 600));
        } catch (IOException e) {
            showAlert("Error loading Profile view: " + e.getMessage());
        }
    }

    @FXML
    public void handleLogout() {
        session.clearSession();
        redirectToLogin();
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/login.fxml"));
            Parent loginView = loader.load();
            Stage stage = (Stage) farmButton.getScene().getWindow();
            stage.setTitle("Login");
            stage.setScene(new Scene(loginView, 900, 600));
        } catch (IOException e) {
            showAlert("Error loading Login view: " + e.getMessage());
        }
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