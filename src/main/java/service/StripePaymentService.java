package service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class StripePaymentService {
    
    private boolean isInitialized = false;
    private static final String CONFIG_FILE = "/config.properties";

    public StripePaymentService() {
        initializeStripe();
    }

    private void initializeStripe() {
        try {
            if (!isInitialized) {
                // Load configuration
                Properties props = new Properties();
                InputStream input = getClass().getResourceAsStream(CONFIG_FILE);
                if (input == null) {
                    showError("Erreur de configuration", "Le fichier de configuration est introuvable.");
                    return;
                }
                props.load(input);
                
                // Get API key from configuration
                String apiKey = props.getProperty("stripe.api.key");
                if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("YOUR_STRIPE_TEST_SECRET_KEY")) {
                    showError("Erreur de configuration", "La clé API Stripe n'est pas configurée.");
                    return;
                }

                // Validate API key format
                apiKey = apiKey.trim(); // Remove any whitespace
                if (!apiKey.startsWith("sk_test_") && !apiKey.startsWith("sk_live_")) {
                    showError("Erreur de configuration", "La clé API Stripe doit commencer par 'sk_test_' ou 'sk_live_'");
                    return;
                }
                
                // Initialize Stripe with the API key
                Stripe.apiKey = apiKey;
                isInitialized = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur d'initialisation", "Impossible d'initialiser Stripe: " + e.getMessage());
        }
    }

    public boolean processPayment(double amount, String currency) {
        if (!isInitialized) {
            showError("Erreur de configuration", "Le service de paiement n'est pas correctement configuré. Contactez l'administrateur.");
            return false;
        }

        try {
            // Create a payment intent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount((long) (amount * 100)) // Stripe uses cents
                .setCurrency(currency)
                .build();

            PaymentIntent intent = PaymentIntent.create(params);

            // Show payment dialog
            return showPaymentDialog(intent.getClientSecret());

        } catch (StripeException e) {
            e.printStackTrace();
            String errorMessage = "Une erreur est survenue lors du traitement du paiement: ";
            if (e.getMessage().contains("Invalid API Key")) {
                errorMessage += "Clé API invalide. Veuillez contacter l'administrateur.";
            } else {
                errorMessage += e.getMessage();
            }
            showError("Erreur de paiement", errorMessage);
            return false;
        }
    }

    private boolean showPaymentDialog(String clientSecret) {
        // Create custom dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Paiement sécurisé");
        dialog.setHeaderText(null);
        
        // Create the custom content
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: white;");

        // Header with Stripe logo and secure payment text
        HBox header = new HBox(10);
        Label securePaymentLabel = new Label("Paiement sécurisé via Stripe");
        securePaymentLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        securePaymentLabel.setTextFill(Color.web("#32325d"));
        header.getChildren().addAll(securePaymentLabel);

        // Card information section
        VBox cardSection = new VBox(10);
        cardSection.setStyle("-fx-background-color: #f7fafc; -fx-padding: 15; -fx-background-radius: 8;");

        // Card number field with custom styling
        TextField cardNumber = createStyledTextField("4242 4242 4242 4242");
        cardNumber.setPromptText("Numéro de carte");
        cardNumber.setStyle(getTextFieldStyle());

        // Expiry and CVC in the same row
        HBox cardDetailsBox = new HBox(10);
        TextField expDate = createStyledTextField("MM/YY");
        expDate.setPrefWidth(100);
        TextField cvc = createStyledTextField("123");
        cvc.setPrefWidth(80);

        cardDetailsBox.getChildren().addAll(
            createFieldWithLabel(expDate, "Date d'expiration"),
            createFieldWithLabel(cvc, "CVC")
        );

        // Add card type indicators
        HBox cardTypes = new HBox(10);
        cardTypes.setStyle("-fx-padding: 5 0 10 0;");
        Label acceptedCards = new Label("Cartes acceptées pour le test :");
        acceptedCards.setStyle("-fx-text-fill: #666;");
        cardTypes.getChildren().add(acceptedCards);

        // Add all elements to the card section
        cardSection.getChildren().addAll(
            createFieldWithLabel(cardNumber, "Numéro de carte"),
            cardDetailsBox
        );

        // Add help text
        Label helpText = new Label("Pour tester, utilisez le numéro 4242 4242 4242 4242");
        helpText.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

        // Add all sections to main container
        mainContainer.getChildren().addAll(header, cardSection, helpText);

        // Set the custom content
        dialog.getDialogPane().setContent(mainContainer);

        // Add buttons
        ButtonType payButton = new ButtonType("Payer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(payButton, cancelButton);

        // Style the pay button
        Button payButtonNode = (Button) dialog.getDialogPane().lookupButton(payButton);
        payButtonNode.setStyle("-fx-background-color: #5469d4; -fx-text-fill: white; -fx-font-weight: bold;");

        // Show the dialog and process the result
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == payButton) {
            // Remove spaces from card number
            String cleanCardNumber = cardNumber.getText().replaceAll("\\s+", "");
            
            // For testing purposes, accept any card number that matches the test pattern
            if (cleanCardNumber.matches("4242\\d{12}") || 
                cleanCardNumber.equals("4000056655665556") || 
                cleanCardNumber.equals("4000002760003184")) {
                
                // Validate expiration date format
                if (!expDate.getText().matches("\\d{2}/\\d{2}")) {
                    showError("Format invalide", "Le format de la date d'expiration doit être MM/YY");
                    return false;
                }
                
                // Validate CVC
                if (!cvc.getText().matches("\\d{3}")) {
                    showError("Format invalide", "Le CVC doit être composé de 3 chiffres");
                    return false;
                }

                // For test purposes, consider the payment successful if using the test card number
                if (cleanCardNumber.startsWith("4242")) {
                    showSuccess("Paiement réussi", "Votre paiement a été traité avec succès!");
                    return true;
                } else {
                    showError("Paiement refusé", "La carte a été refusée. Veuillez utiliser une autre carte.");
                    return false;
                }
            } else {
                showError("Carte invalide", "Veuillez utiliser une carte de test valide (ex: 4242 4242 4242 4242)");
                return false;
            }
        }
        return false;
    }

    private VBox createFieldWithLabel(TextField field, String labelText) {
        VBox container = new VBox(5);
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #32325d; -fx-font-size: 12;");
        container.getChildren().addAll(label, field);
        return container;
    }

    private TextField createStyledTextField(String placeholder) {
        TextField field = new TextField();
        field.setStyle(getTextFieldStyle());
        field.setPromptText(placeholder);
        return field;
    }

    private String getTextFieldStyle() {
        return "-fx-background-color: white;" +
               "-fx-border-color: #e6e6e6;" +
               "-fx-border-radius: 4;" +
               "-fx-padding: 8;" +
               "-fx-font-size: 14;";
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color: white;");
        ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setStyle(
            "-fx-background-color: #5469d4; -fx-text-fill: white; -fx-font-weight: bold;"
        );
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color: white;");
        ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setStyle(
            "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold;"
        );
        alert.showAndWait();
    }
} 