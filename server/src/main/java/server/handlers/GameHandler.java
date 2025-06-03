package server.handlers;

import com.google.gson.Gson;
import service.GameService;
import dataaccess.UnauthorizedUserException;
import dataaccess.DataAccessException;
import model.GameData;
import model.GamesList;

import spark.Request;
import spark.Response;
import java.util.HashSet;

public class GameHandler {

    GameService gameService;
    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public Object listGames(Request req, Response resp) {
        try {
            String authToken = req.headers("authorization");
            //HashSet<GameData> games = gameService.listGames(authToken);
            GamesList games = new GamesList(gameService.listGames(authToken));

            resp.status(200);
            //return "{ \"games\": %s}".formatted(new Gson().toJson(games));
            return new Gson().toJson(games);

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
            return "{ \"message\": \"Error: " + e.getMessage() + "\" }";
        }
    }

    public Object createGame(Request req, Response resp) {
        try {
            String authToken = req.headers("authorization");
            record CreateGameData(String gameName) {}
            CreateGameData createData = new Gson().fromJson(req.body(), CreateGameData.class);

            if (createData.gameName() == null || createData.gameName().isEmpty()) {
                resp.status(400);
                return "{ \"message\": \"Error: Bad Request\" }";
            }

            int gameID = gameService.createGame(authToken, createData.gameName());
            resp.status(200);
            return "{ \"gameID\": %d }".formatted(gameID);
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
            return "{ \"message\": \"Error: " + e.getMessage() + "\" }";
        }
    }

    public Object joinGame(Request req, Response resp) {
        try {
            if (!req.body().contains("\"gameID\":")) {
                resp.status(400);
                return "{ \"message\": \"Error: Bad Request\" }";
            }

            String authToken = req.headers("authorization");
            record JoinGameData(String playerColor, int gameID) {}
            JoinGameData joinData = new Gson().fromJson(req.body(), JoinGameData.class);

            if (joinData.playerColor() != null &&
                    !joinData.playerColor().isEmpty() &&
                    !joinData.playerColor().equalsIgnoreCase("WHITE") &&
                    !joinData.playerColor().equalsIgnoreCase("BLACK")) {
                resp.status(400);
                return "{ \"message\": \"Error: Bad Request\" }";
            }

            int joinStatus = gameService.joinGame(authToken, joinData.gameID(), joinData.playerColor());

            switch (joinStatus) {
                case 0:
                    resp.status(200);
                    return "{}";
                case 1:
                    resp.status(400);
                    return "{ \"message\": \"Error: Bad Request\" }";
                case 2:
                    resp.status(403);
                    return "{ \"message\": \"Error: Game Already Taken\" }";
                default:
                    resp.status(500);
                    return "{ \"message\": \"Error: Unknown status code\" }";
            }
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
            return "{ \"message\": \"Error: " + e.getMessage() + "\" }";
        }
    }
}