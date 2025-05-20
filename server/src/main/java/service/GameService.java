package service;

import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.GameDAO;
import dataAccess.UnauthorizedUserException;
import model.AuthData;
import model.GameData;

import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
public class GameService {

    GameDAO gameDAO;
    AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public HashSet<GameData> listGames(String authToken) throws DataAccessException {
        authDAO.getAuth(authToken);
        return gameDAO.listGames();
    }

    public int createGame(String authToken) throws DataAccessException {
        authDAO.getAuth(authToken);
        int gameID;
        do {
            gameID = ThreadLocalRandom.current().nextInt(1, 10000); // in case the game ID is in use, will find another one
        } while (gameDAO.checkGameExists(gameID));

        gameDAO.createGame(new GameData(gameID, null, null, null, null));
        return gameID;
    }

    public int joinGame(String authToken, int gameID, String color) throws UnauthorizedUserException, DataAccessException {
        AuthData authData;
        GameData gameData;
        try {
            authData = authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedUserException("Authentication Token Incorrect");
        }

        if (gameDAO.checkGameExists(gameID)) {
            gameData = gameDAO.getGame(gameID);
        } else {
            return 1;
        }

        String whiteUser = gameData.whiteUsername();
        String blackUser = gameData.blackUsername();

        //the logic here sets the players up in order that the colors aren't taken
        if (Objects.equals(color, "WHITE")) {
            if (whiteUser != null) {
                return 2;
            } else {
                whiteUser = authData.username();
            }
        } else if (Objects.equals(color, "BLACK")) {
            if (blackUser != null) {
                return 2;
            } else blackUser = authData.username();
        } else if (color != null) {
            return 1; // error
        }
        gameDAO.updateGame(new GameData(gameID, whiteUser, blackUser, gameData.gameName(), gameData.game()));
        return 0;
    }

        public void clear() {
            gameDAO.clear();
        }
    }