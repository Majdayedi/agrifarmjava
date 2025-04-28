package utils;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;

import java.io.*;
import java.security.SecureRandom;
import java.util.*;

public class GoogleAuthConfig {
    private static final String CLIENT_SECRETS_FILE = "client_secrets.json";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile"
    );

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport(); // Reuse instance
    private static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

    private final GoogleClientSecrets clientSecrets;
    private final GoogleAuthorizationCodeFlow flow;
    private final String state;

    public GoogleAuthConfig() throws IOException {
        // Load client secrets from resources
        InputStream in = getClass().getResourceAsStream("/" + CLIENT_SECRETS_FILE);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CLIENT_SECRETS_FILE);
        }
        clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Set up authorization flow
        flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                clientSecrets,
                SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        // Generate secure state string
        state = generateState();
    }

    public String getAuthorizationUrl() {
        return flow.newAuthorizationUrl()
                .setRedirectUri(REDIRECT_URI)
                .setState(state)
                .build();
    }

    public Userinfo getUserInfo(String code) throws IOException {
        GoogleTokenResponse response = flow.newTokenRequest(code)
                .setRedirectUri(REDIRECT_URI)
                .execute();

        flow.createAndStoreCredential(response, "user"); // Can replace "user" with dynamic ID if needed

        Oauth2 oauth2 = new Oauth2.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                flow.loadCredential("user"))
                .setApplicationName("Java1 Application")
                .build();

        return oauth2.userinfo().get().execute();
    }

    private String generateState() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String getState() {
        return state;
    }
}