package websocket.commands;

import chess.ChessGame;

public class JoinPlayer extends UserGameCommand {
    ChessGame.TeamColor playerColor;

    public JoinPlayer(String authToken, int gameID, ChessGame.TeamColor playerColor) {
        super(CommandType.JOIN_PLAYER, authToken, gameID);
        this.playerColor = playerColor;
    }

    public String getColorString() {
        return playerColor == ChessGame.TeamColor.WHITE ? "white" : "black";
    }

}