package server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedUserException;
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
            UserData userData;
            try {
                userData = new Gson().fromJson(req.body(), UserData.class);
            } catch (JsonSyntaxException e) {
                throw new BadRequestException("Invalid JSON format");
            }

            if (userData == null || userData.username() == null || userData.password() == null || userData.email() == null) {
                throw new BadRequestException("Missing required fields");
            }

            try {
                AuthData authData = userAuthService.createUser(userData);
                resp.status(200);
                return new Gson().toJson(authData);
            } catch (DataAccessException e) {
                if (e.getMessage().contains("failed to get connection") ||
                        e.getMessage().contains("Database connection failed")) {
                    resp.status(500);
                    return "{ \"message\": \"Error: Internal server error\" }";
                } else if (e.getMessage().contains("Username already taken")) {
                    resp.status(403);
                    return "{ \"message\": \"Error: Username already taken\" }";
                } else {
                    resp.status(500);
                    return "{ \"message\": \"Error: " + e.getMessage() + "\" }";
                }
            }
        } catch (BadRequestException e) {
            resp.status(400);
            return "{ \"message\": \"Error: Bad Request\" }";
        } catch (Exception e) {
            resp.status(500);
            return "{ \"message\": \"Error: Internal server error\" }";
        }
    }

    public Object login(Request req, Response resp) {
        try {
            UserData userData = new Gson().fromJson(req.body(), UserData.class);
            if (userData == null || userData.username() == null || userData.password() == null) {
                resp.status(400);
                return "{ \"message\": \"Error: Missing required fields\" }";
            }

            AuthData authData = userAuthService.loginUser(userData);
            resp.status(200);
            return new Gson().toJson(authData);
        } catch (JsonSyntaxException e) {
            resp.status(400);
            return "{ \"message\": \"Error: Invalid request format\" }";
        } catch (DataAccessException e) {
            if (e.getMessage().contains("Invalid credentials") || e.getMessage().contains("User not found")) {
                resp.status(401);
                return "{ \"message\": \"Error: Unauthorized\" }";
            } else {
                resp.status(500);
                return "{ \"message\": \"Error: " + e.getMessage() + "\" }";
            }
        } catch (Exception e) {
            resp.status(500);
            return "{ \"message\": \"Error: Internal server error\" }";
        }
    }

    public Object logout(Request req, Response resp) {
        try {
            String authToken = req.headers("authorization");

            if (authToken == null || authToken.isBlank()) {
                resp.status(400);
                return "{ \"message\": \"Error: Missing authorization header\" }";
            }

            userAuthService.logoutUser(authToken);
            resp.status(200);
            return "{}";
        } catch (DataAccessException e) {
            if (e.getMessage().contains("Invalid authentication token") ||
                    e.getMessage().contains("Token not found")) {
                resp.status(401);
                return "{ \"message\": \"Error: Unauthorized\" }";
            } else {
                resp.status(500);
                return "{ \"message\": \"Error: " + e.getMessage() + "\" }";
            }
        } catch (Exception e) {
            resp.status(500);
            return "{ \"message\": \"Error: Internal server error\" }";
        }
    }
}