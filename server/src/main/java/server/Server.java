package server;

import com.google.gson.Gson;
import dataaccess.*;
import service.GameService;
import service.UserAuthService;
import server.handlers.*;
import spark.*;

import java.util.Map;

public class Server {

    UserDAO userDAO;
    AuthDAO authDAO;
    GameDAO gameDAO;

    UserAuthService userAuthService;
    GameService gameService;

    UserAuthHandler userAuthHandler;
    GameHandler gameHandler;

    public Server() {

        //userDAO = new MemoryUserDAO();
        //authDAO = new MemoryAuthDAO();
        //gameDAO = new MemoryGameDAO();

        try {
            DatabaseManager.initializeDatabase();
            userDAO = new SQLUserDAO();
            gameDAO = new SQLGameDAO();
            authDAO = new SQLAuthDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }


        userAuthService = new UserAuthService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);

        userAuthHandler = new UserAuthHandler(userAuthService);
        gameHandler = new GameHandler(gameService);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        //Spark.staticFiles.externalLocation("src/main/resources/web");
        Spark.staticFiles.location("web");
        //Spark.staticFiles.expireTime(600);

        Spark.get("/", (req, res) -> {
            res.redirect("/index.html");
            return null;
        });

        Spark.post("/user", userAuthHandler::register);
        Spark.post("/session", userAuthHandler::login);
        Spark.delete("/session", userAuthHandler::logout);
        Spark.get("/game", gameHandler::listGames);
        Spark.post("/game", gameHandler::createGame);
        Spark.put("/game", gameHandler::joinGame);
        Spark.delete("/db", this::clear);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object clear(Request req, Response resp) {

        try {
            userAuthService.clear();
            gameService.clear();
            resp.status(200);
            return "{}";
        }
        catch (Exception e) {
            resp.status(500);
            //return "{ \"message\": \"Error: %s\"}".formatted(new Gson().toJson(e.getMessage()));
            return new Gson().toJson(Map.of("message", "Error: " + e.getMessage()));

        }
    }
}