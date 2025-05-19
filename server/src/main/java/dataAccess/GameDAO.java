package dataAccess;

import model.GameData;
import java.util.HashSet;
import chess.ChessGame;

public interface GameDAO {
    HashSet<GameData> listGames(String username);
    void createGame(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game);
    GameData getGame(int gameID) throws DataAccessException;
    void createGame(GameData game);
    void clear();
}