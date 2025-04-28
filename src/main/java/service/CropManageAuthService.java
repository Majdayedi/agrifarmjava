package service;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CropManageAuthService {
    private static final String AUTH_URL = "https://api.cropmanage.ucanr.edu/Token";
    private String accessToken;

    public String authenticate() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(AUTH_URL);

            // Set headers
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

            // Set request body
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("username", "janmedali3@gmail.com"));
            params.add(new BasicNameValuePair("password", "b!V2PjkQNftgCe6"));
            params.add(new BasicNameValuePair("grant_type", "password"));

            httpPost.setEntity(new UrlEncodedFormEntity(params));

            // Execute request
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> result = mapper.readValue(entity.getContent(), Map.class);

                this.accessToken = (String) result.get("access_token");
                return this.accessToken;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getAccessToken() {
        return accessToken;
    }
}