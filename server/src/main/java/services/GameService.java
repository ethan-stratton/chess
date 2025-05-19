package services;

import dataAccess.GameDAO;

public class GameService {

    GameDAO gameDAO;

    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public void clear() {
        gameDAO.clear();
    }
}