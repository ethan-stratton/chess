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

        Spark.staticFiles.location("/web");
        Spark.staticFiles.expireTime(600);

        Spark.get("/debug/files", (req, res) -> {
            res.type("text/plain");
            try {
                InputStream in = getClass().getResourceAsStream("/web/index.html");
                return in != null ? "File exists in /web" : "File missing from /web";
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        });

        Spark.get("/", (req, res) -> {
            res.redirect("/index.html");
            return null;
        });

        // Register your endpoints and handle exceptions here.
        Spark.init();

        Spark.post("/user", userAuthHandler::register);
        Spark.post("/session", userAuthHandler::login);
        Spark.delete("/session", userAuthHandler::logout);
        Spark.get("/game", gameHandler::listGames);
        Spark.post("/game", gameHandler::createGame);
        Spark.put("/game", gameHandler::joinGame);
        Spark.delete("/db", this::clear);

//        System.out.println("Static files configured from: " +
//                getClass().getResource("/web/index.html"));
//        System.out.println("Registered API endpoints:");
//        Spark.routes().forEach(route -> {
//            System.out.println("  " + route.getHttpMethod() + " " + route.getMatchUri());
//        });

        Spark.awaitInitialization();
        //System.out.println("Server started successfully on port " + desiredPort);

        // Verify paths
        System.out.println("index.html path: " +
                getClass().getResource("/web/index.html"));

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