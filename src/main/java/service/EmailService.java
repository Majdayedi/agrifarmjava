package service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EmailService {
    private static final String SMTP_HOST = "sandbox.smtp.mailtrap.io";
    private static final int SMTP_PORT = 587;
    private static final String SMTP_USERNAME = "ebe046dfc007d1";
    private static final String SMTP_PASSWORD = "b743f675cb9877";

    // In-memory token storage
    private static final Map<String, TokenInfo> tokenStorage = new ConcurrentHashMap<>();
    private static final Map<String, PasswordResetInfo> passwordResetStorage = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Token expiration time in minutes
    private static final int TOKEN_EXPIRATION_MINUTES = 10;

    // Static initializer to start the token cleanup task
    static {
        // Run cleanup every minute to remove expired tokens
        scheduler.scheduleAtFixedRate(
                EmailService::cleanupExpiredTokens,
                1, 1, TimeUnit.MINUTES
        );
    }

    // Inner class to store token information
    private static class TokenInfo {
        private final String token;
        private final int userId;
        private final LocalDateTime createdAt;

        public TokenInfo(String token, int userId) {
            this.token = token;
            this.userId = userId;
            this.createdAt = LocalDateTime.now();
        }

        public boolean isExpired() {
            return ChronoUnit.MINUTES.between(createdAt, LocalDateTime.now()) > TOKEN_EXPIRATION_MINUTES;
        }
    }

    // Inner class to store password reset information
    private static class PasswordResetInfo {
        private final String token;
        private final String email;
        private final LocalDateTime createdAt;

        public PasswordResetInfo(String token, String email) {
            this.token = token;
            this.email = email;
            this.createdAt = LocalDateTime.now();
        }

        public boolean isExpired() {
            return ChronoUnit.MINUTES.between(createdAt, LocalDateTime.now()) > TOKEN_EXPIRATION_MINUTES;
        }
    }

    // Cleanup method to remove expired tokens
    private static void cleanupExpiredTokens() {
        tokenStorage.entrySet().removeIf(entry -> entry.getValue().isExpired());
        passwordResetStorage.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    // Generate and store a verification token
    public String generateVerificationToken(int userId) {
        String token = UUID.randomUUID().toString();
        tokenStorage.put(token, new TokenInfo(token, userId));
        return token;
    }

    // Verify a token and return the associated userId if valid
    public Integer verifyToken(String token) {
        TokenInfo tokenInfo = tokenStorage.get(token);
        if (tokenInfo != null && !tokenInfo.isExpired()) {
            // Remove the token after successful verification
            tokenStorage.remove(token);
            return tokenInfo.userId;
        }
        return null;
    }

    // Generate and store a password reset token
    public String generatePasswordResetToken(String email) {
        String token = UUID.randomUUID().toString();
        passwordResetStorage.put(token, new PasswordResetInfo(token, email));
        return token;
    }

    // Verify a password reset token and return the associated email if valid
    public String verifyPasswordResetToken(String token) {
        PasswordResetInfo resetInfo = passwordResetStorage.get(token);
        if (resetInfo != null && !resetInfo.isExpired()) {
            // Remove the token after successful verification
            passwordResetStorage.remove(token);
            return resetInfo.email;
        }
        return null;
    }

    public void sendVerificationEmail(String toEmail, String verificationCode) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Email Verification");

            String htmlContent = String.format(
                    "<html><body>" +
                            "<h2>Email Verification</h2>" +
                            "<p>Your verification code is: <strong>%s</strong></p>" +
                            "<p>This code will expire in %d minutes.</p>" +
                            "</body></html>",
                    verificationCode, TOKEN_EXPIRATION_MINUTES
            );

            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Password Reset Request");

            String htmlContent = String.format(
                    "<html><body>" +
                            "<h2>Password Reset Request</h2>" +
                            "<p>You have requested to reset your password. Use the following token to reset your password:</p>" +
                            "<p><strong>%s</strong></p>" +
                            "<p>This token will expire in %d minutes.</p>" +
                            "<p>If you did not request this reset, please ignore this email.</p>" +
                            "</body></html>",
                    resetToken, TOKEN_EXPIRATION_MINUTES
            );

            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}