package utils;

import java.util.prefs.Preferences;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class CredentialManager {
    private static final String PREF_NODE = "JavaPiApp";
    private static final String EMAIL_KEY = "saved_email";
    private static final String PASSWORD_KEY = "saved_password";
    private static final String REMEMBER_ME_KEY = "remember_me";
    // Using a byte array for exact 16 bytes (AES-128)
    private static final byte[] ENCRYPTION_KEY = new byte[] {
            74, 97, 118, 97, 80, 105, 83, 101, 99, 114, 101, 116, 75, 101, 121, 49
    };

    private static final Preferences prefs = Preferences.userNodeForPackage(CredentialManager.class);

    public static void saveCredentials(String email, String password, boolean rememberMe) {
        if (rememberMe) {
            try {
                String encryptedEmail = encrypt(email);
                String encryptedPassword = encrypt(password);
                prefs.put(EMAIL_KEY, encryptedEmail);
                prefs.put(PASSWORD_KEY, encryptedPassword);
                prefs.putBoolean(REMEMBER_ME_KEY, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            clearCredentials();
        }
    }

    public static String getSavedEmail() {
        String encryptedEmail = prefs.get(EMAIL_KEY, "");
        if (encryptedEmail.isEmpty()) return "";
        try {
            return decrypt(encryptedEmail);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getSavedPassword() {
        String encryptedPassword = prefs.get(PASSWORD_KEY, "");
        if (encryptedPassword.isEmpty()) return "";
        try {
            return decrypt(encryptedPassword);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static boolean isRememberMeEnabled() {
        return prefs.getBoolean(REMEMBER_ME_KEY, false);
    }

    public static void clearCredentials() {
        prefs.remove(EMAIL_KEY);
        prefs.remove(PASSWORD_KEY);
        prefs.remove(REMEMBER_ME_KEY);
    }

    private static String encrypt(String value) throws Exception {
        SecretKeySpec key = new SecretKeySpec(ENCRYPTION_KEY, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(value.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private static String decrypt(String encrypted) throws Exception {
        SecretKeySpec key = new SecretKeySpec(ENCRYPTION_KEY, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encrypted));
        return new String(decryptedBytes);
    }
}