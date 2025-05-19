package server.handlers;

import dataAccess.GameDAO;
import services.GameService;

public class GameHandler {

    GameService gameService;
    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }
}