package service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FacebookUserService {

    public static FacebookUser fetchUserInfo(String accessToken) throws IOException {
        String url = "https://graph.facebook.com/me?fields=id,name,email,first_name,last_name,picture.width(400).height(400)&access_token=" + accessToken;
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Failed to fetch user info from Facebook. HTTP code: " + responseCode);
        }

        try (InputStream in = connection.getInputStream()) {
            String json = new String(in.readAllBytes());
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            String email = obj.has("email") ? obj.get("email").getAsString() : null;
            String firstName = obj.has("first_name") ? obj.get("first_name").getAsString() : "";
            String lastName = obj.has("last_name") ? obj.get("last_name").getAsString() : "";
            String pictureUrl = obj.getAsJsonObject("picture")
                    .getAsJsonObject("data")
                    .get("url").getAsString();

            return new FacebookUser(email, firstName, lastName, pictureUrl);
        }
    }

    public static class FacebookUser {
        private final String email;
        private final String firstName;
        private final String lastName;
        private final String pictureUrl;

        public FacebookUser(String email, String firstName, String lastName, String pictureUrl) {
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.pictureUrl = pictureUrl;
        }

        public String getEmail() {
            return email;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getPictureUrl() {
            return pictureUrl;
        }
    }
}
