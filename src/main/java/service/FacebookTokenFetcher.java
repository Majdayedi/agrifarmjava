package service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class FacebookTokenFetcher {
    private static final String APP_ID = "";
    private static final String APP_SECRET = "";
    private static final String REDIRECT_URI = "http://localhost:8888/fb-callback";

    public static String getAccessToken(String code) throws IOException {
        String url = "https://graph.facebook.com/v19.0/oauth/access_token?" +
                "client_id=" + APP_ID +
                "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8") +
                "&client_secret=" + APP_SECRET +
                "&code=" + code;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        try (InputStream in = connection.getInputStream()) {
            String json = new String(in.readAllBytes());
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            return obj.get("access_token").getAsString();
        }
    }
}
