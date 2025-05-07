package utils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.UUID;

public class FacebookAuth {
    private static final String APP_ID = "1641418556562646";
    private static final String REDIRECT_URI = "http://localhost:8888/fb-callback";

    public static void launchLogin() throws IOException {
        String url = "https://www.facebook.com/v19.0/dialog/oauth?" +
                "client_id=" + APP_ID +
                "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8") +
                "&state=" + UUID.randomUUID().toString() +
                "&scope=email,public_profile";
        Desktop.getDesktop().browse(URI.create(url));
    }
}
