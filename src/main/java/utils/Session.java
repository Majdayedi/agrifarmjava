package utils;

import entite.User;
import java.util.prefs.Preferences;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.LocalDateTime;


public class Session {
    private static Session instance;
    private User loggedInUser;
    private static final String PREF_NODE = "JavaPiApp";
    private static final String USER_KEY = "logged_in_user";
    private static final Preferences prefs = Preferences.userNodeForPackage(Session.class);

    // Create a Gson instance with the custom adapter
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    private Session() {
        // Try to restore user session on initialization
        restoreSession();
    }

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }


    public void setUser(User user) {
        this.loggedInUser = user;
        if (user != null) {
            // Save user to preferences
            String userJson = gson.toJson(user);
            prefs.put(USER_KEY, userJson);
        } else {
            // Clear saved user
            prefs.remove(USER_KEY);
        }
    }

    public User getUser() {
        return loggedInUser;
    }

    public void clearSession() {
        loggedInUser = null;
        prefs.remove(USER_KEY);
    }

    private void restoreSession() {
        String userJson = prefs.get(USER_KEY, null);
        if (userJson != null) {
            try {
                loggedInUser = gson.fromJson(userJson, User.class);
            } catch (Exception e) {
                e.printStackTrace();
                clearSession();
            }
        }
    }


    public boolean isAdmin() {
        return loggedInUser != null && loggedInUser.getRoles().contains("ROLE_ADMIN");
    }

    public boolean isUser() {
        return loggedInUser != null && loggedInUser.getRoles().contains("ROLE_USER");
    }

    public boolean isLoggedIn() {
        return loggedInUser != null;
    }
}