package websocket.commands;

public class Resignation extends UserGameCommand {

    public Resignation(String authToken, int gameID) {
        super(CommandType.RESIGN, authToken, gameID);
    }
}