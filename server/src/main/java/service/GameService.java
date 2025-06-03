package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UnauthorizedUserException;
import dataaccess.BadRequestException;
import model.AuthData;
import model.GameData;

import chess.ChessBoard;
import chess.ChessGame;

import java.util.HashSet;
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

    public int createGame(String authToken, String gameName) throws DataAccessException {
        authDAO.getAuth(authToken);
        int gameID;
        do {
            gameID = ThreadLocalRandom.current().nextInt(1, 10000); // in case the game ID is in use, will find another one
        } while (gameDAO.checkGameExists(gameID));

        //gameDAO.createGame(new GameData(gameID, null, null, gameName, null));
        ChessGame game = new ChessGame();
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        game.setBoard(board);
        gameDAO.createGame(new GameData(gameID, null, null, gameName, game));

        return gameID;
    }

    public int joinGame(String authToken, int gameID, String color) throws UnauthorizedUserException, DataAccessException, BadRequestException {

        if (color == null){
            return 1;
        }

        if (!color.isEmpty() &&
                !color.equalsIgnoreCase("WHITE") &&
                !color.equalsIgnoreCase("BLACK")) {
            //throw new BadRequestException("Invalid team color");
            return 1;
        }

        AuthData authData = authDAO.getAuth(authToken);

        if (!gameDAO.checkGameExists(gameID)) {
            return 1;
        }

        if (color.isEmpty()) {
            return 1;
        }
        if (!color.equalsIgnoreCase("WHITE") && !color.equalsIgnoreCase("BLACK")) {
            return 1;
        }

        GameData gameData = gameDAO.getGame(gameID);
        String whiteUser = gameData.whiteUsername();
        String blackUser = gameData.blackUsername();


        if (color.equalsIgnoreCase("WHITE")) {
            if (whiteUser != null) {
                return 2;
            }
            whiteUser = authData.username();
        } else if (color.equalsIgnoreCase("BLACK")) {
            if (blackUser != null) {
                return 2;
            }
            blackUser = authData.username();
        }

        gameDAO.updateGame(new GameData(gameID, whiteUser, blackUser, gameData.gameName(), gameData.game()));
        return 0;
    }

        public void clear() {
            gameDAO.clear();
        }
    }