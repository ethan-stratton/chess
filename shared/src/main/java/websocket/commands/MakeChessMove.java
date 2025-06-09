package websocket.commands;

import chess.ChessGame;
import chess.ChessMove;

public class MakeChessMove extends UserGameCommand {

    ChessMove move;

    public MakeChessMove(String authToken, int gameID, ChessMove move) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.move = move;
    }

    public ChessMove getMove() {
        return move;
    }
}
