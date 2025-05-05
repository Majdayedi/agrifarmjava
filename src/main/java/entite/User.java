package entite;

import java.time.LocalDateTime;
import java.util.List;
import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private int id;

    @SerializedName("email")
    private String email;

    @SerializedName("roles")
    private List<String> roles;

    @SerializedName("password")
    private String password;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("createdAt")
    private LocalDateTime createdAt;

    @SerializedName("imageFileName")
    private String imageFileName;

    @SerializedName("isVerified")
    private boolean isVerified;

    // Updated Constructor
    public User(int id, String email, List<String> roles, String password, String lastName, String firstName, LocalDateTime createdAt, String imageFileName, boolean isVerified) {
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.password = password;
        this.lastName = lastName;
        this.firstName = firstName;
        this.createdAt = createdAt;
        this.imageFileName = imageFileName;
        this.isVerified = isVerified;
    }

    // Overloaded constructor without imageFileName (optional use)
    public User(int id, String email, List<String> roles, String password, String lastName, String firstName, LocalDateTime createdAt, boolean isVerified) {
        this(id, email, roles, password, lastName, firstName, createdAt, null, isVerified);
    }

    // Getters and setters
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

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        this.isVerified = verified;
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
                ", isVerified=" + isVerified +
                '}';
    }

    public static List<String> parseRolesFromJson(String rolesJson) {
        try {
            return new com.google.gson.Gson().fromJson(rolesJson, List.class);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of("ROLE_USER");
        }
    }
}
