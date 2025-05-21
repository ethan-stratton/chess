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
            // Attempt to parse the JSON - if it fails, it's a bad request
            UserData userData;
            try {
                userData = new Gson().fromJson(req.body(), UserData.class);
            } catch (JsonSyntaxException e) {
                throw new BadRequestException("Invalid JSON format");
            }

            // Validate that required fields are present
            if (userData == null || userData.username() == null || userData.password() == null || userData.email() == null) {
                throw new BadRequestException("Missing required fields");
            }

            AuthData authData = userAuthService.createUser(userData);
            resp.status(200);
            return new Gson().toJson(authData);
        } catch (BadRequestException e) {
            System.out.println("Bad request in register: " + e.getMessage());
            resp.status(400);
            return "{ \"message\": \"Error: Bad Request\" }";
        } catch (DataAccessException e) {
            System.out.println("Username already taken: " + e.getMessage());
            resp.status(403);
            return "{ \"message\": \"Error: Username Already Taken\" }";
        } catch (Exception e) {
            System.out.println("Internal error in register: " + e.getMessage());
            resp.status(500);
            return "{ \"message\": \"Error: %s\" }".formatted(e.getMessage());
        }
    }

    public Object login(Request req, Response resp) {
        try {
            // Attempt to parse the JSON - if it fails, it's a bad request
            UserData userData;
            try {
                userData = new Gson().fromJson(req.body(), UserData.class);
            } catch (JsonSyntaxException e) {
                throw new BadRequestException("Invalid JSON format");
            }

            // Validate that required fields are present
            if (userData == null || userData.username() == null || userData.password() == null) {
                throw new BadRequestException("Missing required fields");
            }

            try {
                AuthData authData = userAuthService.loginUser(userData);
                resp.status(200);
                return new Gson().toJson(authData);
            } catch (DataAccessException e) {
                throw new UnauthorizedUserException("Invalid credentials");
            }
        } catch (BadRequestException e) {
            System.out.println("Bad request in login: " + e.getMessage());
            resp.status(400);
            return "{ \"message\": \"Error: Bad Request\" }";
        } catch (UnauthorizedUserException e) {
            System.out.println("Unauthorized in login: " + e.getMessage());
            resp.status(401);
            return "{ \"message\": \"Error: Unauthorized\" }";
        } catch (Exception e) {
            System.out.println("Internal error in login: " + e.getMessage());
            resp.status(500);
            return "{ \"message\": \"Error: %s\" }".formatted(e.getMessage());
        }
    }

    public Object logout(Request req, Response resp) {
        try {
            String authToken = req.headers("authorization");
            System.out.println("Attempting logout with token: " + authToken);

            if (authToken == null || authToken.isBlank()) {
                throw new UnauthorizedUserException("Missing authorization header");
            }

            try {
                userAuthService.logoutUser(authToken);
                resp.status(200);
                return "{}";
            } catch (DataAccessException e) {
                throw new UnauthorizedUserException("Invalid authentication token");
            }
        } catch (UnauthorizedUserException e) {
            System.out.println("Unauthorized in logout: " + e.getMessage());
            resp.status(401);
            return "{ \"message\": \"Error: Unauthorized\" }";
        } catch (Exception e) {
            System.out.println("Internal error in logout: " + e.getMessage());
            resp.status(500);
            return "{ \"message\": \"Error: Internal server error\" }";
        }
    }
}