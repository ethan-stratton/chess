package dataAccess;

import model.GameData;
import java.util.HashSet;

public interface GameDAO {
    HashSet<GameData> listGames();
    void createGame(GameData game);
    GameData getGame(int gameID);
    boolean gameExists(int gameID);
    void updateGame(GameData game);
    void clear();
}