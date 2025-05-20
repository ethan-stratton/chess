package server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataAccess.DataAccessException;
import model.AuthData;
import model.UserData;
import service.UserAuthService;
import spark.Request;
import spark.Response;

public class UserAuthHandler {

    UserAuthService userAuthService;

    public UserAuthHandler(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    public Object register(Request req, Response resp) {

        try {
            UserData userData = new Gson().fromJson(req.body(), UserData.class);
            AuthData authData = userAuthService.createUser(userData);
            resp.status(200);
            return new Gson().toJson(authData);
        } catch (DataAccessException e) {
            resp.status(403);
            return "{ \"message\": \"Error: Username Already Taken\" }";
        } catch (JsonSyntaxException e) {
            resp.status(400);
            return "{ \"message\": \"Error: Bad Request\" }";
        } catch (Exception e) {
            resp.status(500);
            return "{ \"message\": \"Error: %s\" }".formatted(e.getMessage());
        }

    }

    public Object login(Request req, Response resp) {
        try {
            UserData userData = new Gson().fromJson(req.body(), UserData.class);
            AuthData authData = userAuthService.loginUser(userData);
            resp.status(200);
            return new Gson().toJson(authData);
        } catch (DataAccessException e) {
            resp.status(401);
            return "{ \"message\": \"Error: Unauthorized\" }";
        } catch (Exception e) {
            resp.status(500);
            return "{ \"message\": \"Error: %s\" }".formatted(e.getMessage());
        }
    }

    public Object logout(Request req, Response resp) {
        try {
            String authToken = req.headers("authorization");

            // Debug
            System.out.println("Attempting logout with token: " + authToken);

            if (authToken == null || authToken.isBlank()) {
                resp.status(401);
                return "{ \"message\": \"Error: Authorization header missing\" }";
            }

            userAuthService.logoutUser(authToken);
            resp.status(200);
            return "{}";

        } catch (DataAccessException e) {
            System.out.println("Logout failed: " + e.getMessage());
            resp.status(401);
            return "{ \"message\": \"Error: Invalid authentication token\" }";
        } catch (Exception e) {
            resp.status(500);
            return "{ \"message\": \"Error: Internal server error\" }";
        }
    }

}