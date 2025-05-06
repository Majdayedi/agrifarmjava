package service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailService {
    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    private static String API_KEY;
    private static final String API_URL = "https://api.brevo.com/v3/smtp/email";

    public EmailService() {
        loadConfig();
    }

    private void loadConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                LOGGER.severe("Impossible de trouver config.properties");
                return;
            }

            Properties prop = new Properties();
            prop.load(input);

            API_KEY = prop.getProperty("brevo.api.key");
            if (API_KEY == null || API_KEY.trim().isEmpty()) {
                LOGGER.severe("La clé API Brevo n'est pas configurée dans config.properties");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement de la configuration", e);
        }
    }

    public boolean sendConfirmationEmail(String toEmail, String toName, double totalAmount) {
        LOGGER.info("Début de l'envoi de l'email de confirmation à : " + toEmail);

        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("api-key", API_KEY);
            conn.setDoOutput(true);

            // Créer le contenu de l'email
            JSONObject emailData = new JSONObject();

            // Destinataire
            JSONArray to = new JSONArray();
            JSONObject recipient = new JSONObject();
            recipient.put("email", toEmail);
            recipient.put("name", toName);
            to.put(recipient);
            emailData.put("to", to);

            // Expéditeur (utiliser l'email vérifié de votre compte Brevo)
            JSONObject sender = new JSONObject();
            sender.put("name", "AgriFarm");
            sender.put("email", "hamzatouati425@gmail.com");
            emailData.put("sender", sender);

            // ReplyTo
            JSONObject replyTo = new JSONObject();
            replyTo.put("name", "Service Client AgriFarm");
            replyTo.put("email", "hamzatouati425@gmail.com");
            emailData.put("replyTo", replyTo);

            // Sujet et contenu
            emailData.put("subject", "🌟 Confirmation de votre commande - AgriFarm");

            // Contenu HTML avec style amélioré
            String htmlContent = String.format(
                    "<html><body style='font-family: Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 20px;'>" +
                            "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); padding: 30px;'>" +
                            "<div style='text-align: center; margin-bottom: 30px;'>" +
                            "<h1 style='color: #2c3e50; margin: 0;'>Merci pour votre commande !</h1>" +
                            "</div>" +
                            "<p style='color: #34495e; font-size: 16px;'>Cher(e) %s,</p>" +
                            "<p style='color: #34495e; font-size: 16px;'>Nous vous confirmons que votre commande a bien été enregistrée.</p>" +
                            "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 6px; margin: 25px 0;'>" +
                            "<h2 style='color: #2c3e50; margin: 0 0 15px 0; font-size: 18px;'>Détails de votre commande :</h2>" +
                            "<p style='color: #34495e; margin: 5px 0;'><strong>Montant total :</strong> %.2f €</p>" +
                            "<p style='color: #34495e; margin: 5px 0;'><strong>Date :</strong> %s</p>" +
                            "</div>" +
                            "<p style='color: #34495e; font-size: 16px;'>Notre équipe met tout en œuvre pour préparer votre commande avec le plus grand soin.</p>" +
                            "<div style='background-color: #e8f5e9; padding: 15px; border-radius: 6px; margin: 25px 0;'>" +
                            "<p style='color: #2e7d32; margin: 0;'>Vous recevrez bientôt un email de confirmation lorsque votre commande sera prête.</p>" +
                            "</div>" +
                            "<p style='color: #34495e; margin-top: 30px;'>Cordialement,<br>L'équipe AgriFarm</p>" +
                            "<div style='text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;'>" +
                            "<p style='color: #7f8c8d; font-size: 12px; margin: 0;'>Pour toute question, n'hésitez pas à nous contacter en répondant à cet email.</p>" +
                            "</div>" +
                            "</div>" +
                            "</body></html>",
                    toName,
                    totalAmount,
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );
            emailData.put("htmlContent", htmlContent);

            // Ajouter des tags pour le suivi
            JSONArray tags = new JSONArray();
            tags.put("commande");
            tags.put("confirmation");
            emailData.put("tags", tags);

            LOGGER.info("Préparation de l'email terminée, envoi en cours...");
            LOGGER.info("Contenu de la requête : " + emailData.toString());

            // Envoyer la requête
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = emailData.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Lire la réponse
            int responseCode = conn.getResponseCode();
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream(),
                            StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            LOGGER.info("Response Code : " + responseCode);
            LOGGER.info("Response Body : " + response.toString());

            if (responseCode >= 400) {
                LOGGER.severe("Erreur lors de l'envoi de l'email. Response: " + response.toString());
                return false;
            }

            // Extraire le messageId de la réponse
            try {
                JSONObject jsonResponse = new JSONObject(response.toString());
                String messageId = jsonResponse.getString("messageId");
                LOGGER.info("Email envoyé avec succès! MessageID: " + messageId);
            } catch (Exception e) {
                LOGGER.warning("Impossible d'extraire le messageId, mais l'email a été envoyé");
            }

            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'envoi de l'email", e);
            e.printStackTrace();
            return false;
        }
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    public String generateVerificationToken(int userId) {
        // Generate a random token
        String token = java.util.UUID.randomUUID().toString();
        System.out.println("Generated token: " + token);
        // TODO: Store the token in the database with the userId and expiration time
        return token;
    }

    public String generatePasswordResetToken(String email) {
        // Generate a random token
        String token = java.util.UUID.randomUUID().toString();
        // TODO: Store the token in the database with the email and expiration time
        return token;
    }

    public Integer verifyToken(String token) {
        // TODO: Verify the token against the database
        // Check if token exists and hasn't expired
        // Return the user ID associated with the token
        return 1; // Placeholder return, should return actual user ID
    }

    public String verifyPasswordResetToken(String token) {
        // TODO: Verify the password reset token against the database
        // Check if token exists and hasn't expired
        // Return the email associated with the token
        return "user@example.com"; // Placeholder return, should return actual user email
    }

    public boolean sendVerificationEmail(String toEmail, String token) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("api-key", API_KEY);
            conn.setDoOutput(true);

            JSONObject emailData = new JSONObject();

            JSONArray to = new JSONArray();
            JSONObject recipient = new JSONObject();
            recipient.put("email", toEmail);
            to.put(recipient);
            emailData.put("to", to);

            JSONObject sender = new JSONObject();
            sender.put("name", "AgriFarm");
            sender.put("email", "hamzatouati425@gmail.com");
            emailData.put("sender", sender);

            emailData.put("subject", "Vérifiez votre compte AgriFarm");

            String verificationLink = "http://localhost:8080/verify?token=" + token;
            String htmlContent = String.format(
                    "<html><body style='font-family: Arial, sans-serif;'>" +
                            "<h1>Vérification de votre compte AgriFarm</h1>" +
                            "<p>Cliquez sur le lien ci-dessous pour vérifier votre compte :</p>" +
                            "<a href='%s'>Vérifier mon compte</a>" +
                            "</body></html>",
                    verificationLink
            );
            emailData.put("htmlContent", htmlContent);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = emailData.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'envoi de l'email de vérification", e);
            return false;
        }
    }

    public boolean sendPasswordResetEmail(String toEmail, String token) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("api-key", API_KEY);
            conn.setDoOutput(true);

            JSONObject emailData = new JSONObject();

            JSONArray to = new JSONArray();
            JSONObject recipient = new JSONObject();
            recipient.put("email", toEmail);
            to.put(recipient);
            emailData.put("to", to);

            JSONObject sender = new JSONObject();
            sender.put("name", "AgriFarm");
            sender.put("email", "hamzatouati425@gmail.com");
            emailData.put("sender", sender);

            emailData.put("subject", "Réinitialisation de votre mot de passe AgriFarm");

            String resetLink = "http://localhost:8080/reset-password?token=" + token;
            String htmlContent = String.format(
                    "<html><body style='font-family: Arial, sans-serif;'>" +
                            "<h1>Réinitialisation de votre mot de passe</h1>" +
                            "<p>Cliquez sur le lien ci-dessous pour réinitialiser votre mot de passe :</p>" +
                            "<a href='%s'>Réinitialiser mon mot de passe</a>" +
                            "</body></html>",
                    resetLink
            );
            emailData.put("htmlContent", htmlContent);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = emailData.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'envoi de l'email de réinitialisation", e);
            return false;
        }
    }
}