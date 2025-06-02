
import com.google.gson.Gson;

import java.io.*;
import java.net.*;
import java.util.*;

import model.GameData;

public class ServerFacade {

    String baseURL = "http://localhost:8080";
    private String authToken;
    private final Gson gson = new Gson();



    public boolean register(String username, String password, String email) {
        var body = Map.of("username", username, "password", password, "email", email);
        Map<String, Object> resp = request("POST", "/user", gson.toJson(body));
        if (resp.containsKey("Error")) {
            return false;
        }
        authToken = (String) resp.get("authToken");
        return true;
    }

    public boolean login(String username, String password) {
        var body = Map.of("username", username, "password", password);
        Map<String, Object> resp = request("POST", "/session", gson.toJson(body));
        if (resp.containsKey("Error")) {
            return false;
        }
        authToken = (String) resp.get("authToken");
        return true;
    }

    public int createGame(String gameName) {
        var body = Map.of("gameName", gameName);
        Map<String, Object> resp = request("POST", "/game", gson.toJson(body));
        //return !resp.containsKey("Error");
        double gameID = (double) resp.get("gameID");
        return (int) gameID;
    }

    public List<GameData> listGames() {
        Map<String, Object> resp = request("GET", "/game");
        if (resp.containsKey("Error")) {
            return new ArrayList<>();
        }
        Object games = resp.get("games");
        if (games instanceof List) {
            return (List<GameData>) games;
        }
        return new ArrayList<>();
    }

    public boolean joinGame(int gameId, String playerColor) {
        var body = Map.of("gameID", gameId, "playerColor", playerColor);
        Map<String, Object> resp = request("PUT", "/game", gson.toJson(body));
        return !resp.containsKey("Error");
    }

    public Map<String, Object> request(String method, String endpoint) {
        return request(method, endpoint, null);
    }

    public Map<String, Object> request(String method, String endpoint, String body) {
        try {
            URI uri = new URI(baseURL + endpoint);
            HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
            http.setRequestMethod(method);

            if (authToken != null) {
                http.addRequestProperty("authorization", authToken);
            }
            if (body != null && !body.isEmpty()) {
                http.setDoOutput(true);
                http.addRequestProperty("Content-Type", "application/json");
                try (var outputStream = http.getOutputStream()) {
                    outputStream.write(body.getBytes());
                }
            }

            http.connect();

            if (http.getResponseCode() == 401) {
                return Map.of("Error", "Unauthorized");
            }

            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(respBody);
                return gson.fromJson(inputStreamReader, Map.class);
            }
        } catch (URISyntaxException | IOException e) {
            return Map.of("Error", e.getMessage());
        }
    }
}