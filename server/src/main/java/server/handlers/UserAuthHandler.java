package server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.UserDAO;
import model.AuthData;
import model.UserData;
import services.UserAuthService;
import spark.Request;
import spark.Response;

import java.util.UUID;

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
            return "{ \"message\": \"Error: already taken\" }";
        } catch (JsonSyntaxException e) {
            resp.status(400);
            return "{ \"message\": \"Error: bad request\" }";
        } catch (Exception e) {
            resp.status(500);
            return "{ \"message\": \"Error: %s\" }".formatted(e.getMessage());
        }

    }

    public Object login(Request req, Response resp) {
        return null;
    }

}