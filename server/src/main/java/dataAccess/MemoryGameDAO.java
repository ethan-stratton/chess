package dataAccess;

import chess.ChessGame;
import model.GameData;

import javax.swing.text.SimpleAttributeSet;
import java.util.HashSet;

public class MemoryGameDAO implements GameDAO {

    HashSet<GameData> db;

    public MemoryGameDAO() {
        db = HashSet.newHashSet(16);
    }

    @Override
    public HashSet<GameData> listGames(String username) {
        HashSet<GameData> games = HashSet.newHashSet(16);
        for (GameData game : db) {
            if (game.whiteUsername().equals(username) ||
                    game.blackUsername().equals(username)) {
                games.add(game);
            }
        }
        return games;
    }

    @Override
    public void createGame(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
        db.add(new GameData(gameID, whiteUsername, blackUsername, gameName, game));
    }

    @Override
    public void createGame(GameData game) {
        db.add(game);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        for (GameData game : db) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        throw new DataAccessException("Game not found, id: " +gameID);
    }

    @Override
    public void clear() {
        db = HashSet.newHashSet(16);
    }
}