package server.handlers;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import service.UserService;
import model.UserData;
import dataaccess.exceptions.DataAccessException;

public class UserHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public Object register(Request req, Response res) {
        try {
            UserData user = gson.fromJson(req.body(), UserData.class);
            AuthData auth = userService.register(user);
            res.status(200);
            return gson.toJson(auth);
        } catch (DataAccessException e) {
            if (e.getMessage().contains("already taken")) {
                res.status(403);
            } else {
                res.status(400);
            }
            return gson.toJson(Map.of("message", e.getMessage()));
        }
    }
}