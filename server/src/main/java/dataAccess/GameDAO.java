package dataAccess;

import model.GameData;
import java.util.HashSet;

public interface GameDAO {
    HashSet<GameData> listGames();
    void createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    boolean gameExists(int gameID);
    void updateGame(GameData game) throws DataAccessException;
    void clear();
}