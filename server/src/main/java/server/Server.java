package server;

import com.google.gson.Gson;
import dataAccess.*;
import service.GameService;
import service.UserAuthService;
import server.handlers.*;
import spark.*;

import java.io.InputStream;

public class Server {

    UserDAO userDAO;
    AuthDAO authDAO;
    GameDAO gameDAO;

    UserAuthService userAuthService;
    GameService gameService;

    UserAuthHandler userAuthHandler;
    GameHandler gameHandler;

    public Server() {

        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();

        userAuthService = new UserAuthService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);

        userAuthHandler = new UserAuthHandler(userAuthService);
        gameHandler = new GameHandler(gameService);

    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        //Spark.staticFiles.location("/web");
        Spark.staticFiles.externalLocation("src/main/resources/web");
        Spark.staticFiles.expireTime(600);

        Spark.get("/", (req, res) -> {
            res.redirect("/index.html");
            return null;
        });

        // Register your endpoints and handle exceptions here.

        Spark.post("/user", userAuthHandler::register);
        Spark.post("/session", userAuthHandler::login);
        Spark.delete("/session", userAuthHandler::logout);
        Spark.get("/game", gameHandler::listGames);
        Spark.post("/game", gameHandler::createGame);
        Spark.put("/game", gameHandler::joinGame);
        Spark.delete("/db", this::clear);

        Spark.awaitInitialization();
        //System.out.println("Server started successfully on port " + desiredPort);

        //System.out.println("index.html path: " +
        //        getClass().getResource("/web/index.html"));

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
            return "{ \"message\": \"Error: %s\"}".formatted(new Gson().toJson(e.getMessage()));
        }


    }
}