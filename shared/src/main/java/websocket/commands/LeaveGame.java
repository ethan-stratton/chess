package websocket.commands;

public class LeaveGame extends UserGameCommand {

    public LeaveGame(String authToken, int gameID) {
        super(CommandType.LEAVE, authToken, gameID);
    }
}
