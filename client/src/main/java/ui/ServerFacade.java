package ui;

import com.google.gson.Gson;

import java.io.*;
import java.net.*;
import java.util.*;

import model.GameData;
import model.GamesList;

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

    public void setServerPort(int port) {
        this.baseURL = "http://localhost:" + port;
    }

    public boolean logout() {
        Map resp = request("DELETE", "/session");
        if (resp.containsKey("Error")) {
            return false;
        }
        authToken = null;
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
        if (resp.containsKey("Error")) {
            return -1;
        }
        double gameID = (double) resp.get("gameID");
        return (int) gameID;
    }

    public HashSet<GameData> listGames() {
        String resp = requestString("GET", "/game");
        if (resp.contains("Error")) {
            return HashSet.newHashSet(8);
        }
        GamesList games = new Gson().fromJson(resp, GamesList.class);

        return games.games();
    }


    public boolean joinGame(int gameId, String playerColor) {
        //var body = Map.of("gameID", gameId, "playerColor", playerColor);
        Map body;
        if (playerColor != null) {
            body = Map.of("gameID", gameId, "playerColor", playerColor);
        } else {
            body = Map.of("gameID", gameId);
        }
        String jsonBody = gson.toJson(body);
        //System.out.println("Sending join request: " + jsonBody); // Debug log
        Map<String, Object> resp = request("PUT", "/game", jsonBody);
        //System.out.println("Received response: " + resp); // Debug log
        return !resp.containsKey("Error");

        //Map<String, Object> resp = request("PUT", "/game", gson.toJson(body));
        //return !resp.containsKey("Error");
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

    private String requestString(String method, String endpoint) {
        return requestString(method, endpoint, null);
    }

    private String requestString(String method, String endpoint, String body) {
        String resp;
        try {
            URI uri = new URI(baseURL + endpoint);
            HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
            http.setRequestMethod(method);

            if (authToken != null) {
                http.addRequestProperty("authorization", authToken);
            }

            if (!Objects.equals(body, null)) {
                http.setDoOutput(true);
                http.addRequestProperty("Content-Type", "application/json");
                try (var outputStream = http.getOutputStream()) {
                    outputStream.write(body.getBytes());
                }
            }

            http.connect();

            try {
                if (http.getResponseCode() == 401) {
                    return "Error: 401";
                }
            } catch (IOException e) {
                return "Error: 401";
            }


            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(respBody);
                resp = readerToString(inputStreamReader);
            }

        } catch (URISyntaxException | IOException e) {
            return String.format("Error: %s", e.getMessage());
        }

        return resp;
    }

    private Map mapOf(String string) {
        System.out.println(string);
        return new Gson().fromJson(string, Map.class);
    }

    private String readerToString(InputStreamReader reader) {
        StringBuilder sb = new StringBuilder();
        try {
            for (int ch; (ch = reader.read()) != -1; ) {
                sb.append((char) ch);
            }
            return sb.toString();
        } catch (IOException e) {
            return "";
        }

    }


}