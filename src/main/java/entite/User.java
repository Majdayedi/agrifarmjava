package entite;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class User {
    private int id;
    private String email;
    private List<String> roles;
    private String password;
    private String lastName;
    private String firstName;
    private LocalDateTime createdAt;
    private String imageFileName; // New field for profile picture

    // Updated Constructor
    public User(int id, String email, List<String> roles, String password, String lastName, String firstName, LocalDateTime createdAt, String imageFileName) {
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.password = password;
        this.lastName = lastName;
        this.firstName = firstName;
        this.createdAt = createdAt;
        this.imageFileName = imageFileName;
    }

    // Overloaded constructor without imageFileName (optional use)
    public User(int id, String email, List<String> roles, String password, String lastName, String firstName, LocalDateTime createdAt) {
        this(id, email, roles, password, lastName, firstName, createdAt, null);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", createdAt=" + createdAt +
                ", imageFileName='" + imageFileName + '\'' +
                '}';
    }

    public static List<String> parseRolesFromJson(String json) {
        Gson gson = new Gson();
        try {
            // Try parsing as array
            return gson.fromJson(json, new TypeToken<List<String>>(){}.getType());
        } catch (JsonSyntaxException e) {
            // Fallback: treat it as a single string role
            return Collections.singletonList(json.replace("\"", "").trim());
        }
    }

}
