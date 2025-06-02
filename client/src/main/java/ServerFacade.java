import com.google.gson.Gson;

import java.io.*;
import java.net.*;
import java.util.Map;

public class ServerFacade {

    String baseURL = "http://localhost:8080";
    String authToken;

    private final Gson gson = new Gson();

    public boolean register(String username, String password, String email) {
        var body = Map.of("username", username, "password", password, "email", email);
        return post("/user", gson.toJson(body));
    }

    public boolean login(String username, String password) {
        var body = Map.of("username", username, "password", password);
        return post("/session", gson.toJson(body));
    }


    public boolean post(String endpoint, String body) {
        try {
            HttpURLConnection http = (HttpURLConnection) new URL(baseURL + endpoint).openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/json");

            // Send request
            try (OutputStream os = http.getOutputStream()) {
                os.write(body.getBytes());
            }

            // Check response code
            int status = http.getResponseCode();

            if (status == 200) {  // Success case
                try (InputStream is = http.getInputStream()) {
                    String response = new String(is.readAllBytes());
                    authToken = new Gson().fromJson(response, Map.class).get("authToken").toString();
                    return true;
                }
            } else {  // Error case
                try (InputStream is = http.getErrorStream()) {
                    String errorResponse = is != null ? new String(is.readAllBytes()) : "No error details";
                    System.err.println("Server error (" + status + "): " + errorResponse);
                }
                return false;
            }

        } catch (Exception e) {
            System.err.println("Request failed: " + e.getMessage());
            return false;
        }
    }

}