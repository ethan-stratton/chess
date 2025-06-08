package websocket.commands;

import chess.ChessGame;
import chess.ChessMove;

public class MakeChessMove extends UserGameCommand {
    //int gameID;
    ChessMove move;

    public MakeChessMove(String authToken, int gameID, ChessMove move) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
    }

//    public int getGameID() {
//        return gameID;
//    }

    public ChessMove getMove() {
        return move;
    }
}
