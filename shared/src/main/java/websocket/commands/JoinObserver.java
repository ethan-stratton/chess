package websocket.commands;

import chess.ChessGame;

public class JoinObserver extends UserGameCommand {
    public JoinObserver(String authToken, int gameID) {
        super(CommandType.JOIN_OBSERVER, authToken, gameID);
    }
}