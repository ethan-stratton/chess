package server;

import chess.ChessGame;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.*;
import dataaccess.BadRequestException;
import dataaccess.UnauthorizedUserException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import websocket.commands.*;
import websocket.messages.*;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.lang.Error;
import java.util.Objects;

@WebSocket
public class WebsocketHandler {

    @OnWebSocketConnect
    public void onConnect(Session session) throws Exception {
        Server.gameSessions.put(session, 0);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        Server.gameSessions.remove(session);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket Error: " + error.getMessage());
        if (session.isOpen()) {
            session.close();
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        System.out.printf("Received: %s\n", message);
        try {
            if (message.contains("\"commandType\":\"JOIN_PLAYER\"")) {
                JoinPlayer command = new Gson().fromJson(message, JoinPlayer.class);
                Server.gameSessions.replace(session, command.getGameID());
                handleJoinPlayer(session, command);
            }
            else if (message.contains("\"commandType\":\"JOIN_OBSERVER\"")) {
                JoinObserver command = new Gson().fromJson(message, JoinObserver.class);
                Server.gameSessions.replace(session, command.getGameID());
                handleJoinObserver(session, command);
            }
            else if (message.contains("\"commandType\":\"MAKE_MOVE\"")) {
                MakeChessMove command = new Gson().fromJson(message, MakeChessMove.class);
                handleMakeMove(session, command);
            }
            else if (message.contains("\"commandType\":\"LEAVE\"")) {
                LeaveGame command = new Gson().fromJson(message, LeaveGame.class);
                handleLeave(session, command);
            }
            else if (message.contains("\"commandType\":\"RESIGN\"")) {
                Resignation command = new Gson().fromJson(message, Resignation.class);
                handleResignation(session, command);
            }
            //lot of this method SHOULD have been handled in serverfacade and elsewhere, for some reason it broke somewhere
            // not a lot of time to figure it out before its due
            else if (message.contains("\"commandType\":\"CONNECT\"")) {
                UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);

                try {
                    AuthData auth = Server.userAuthService.getAuth(command.getAuthToken());
                    GameData game = Server.gameService.getGameData(command.getAuthToken(), command.getGameID());

                    Server.gameSessions.replace(session, command.getGameID());
                    LoadGame load = new LoadGame(game.game());
                    sendMessage(session, load);

                    Notification notif = new Notification(auth.username() + " has connected to the game");
                    broadcastMessage(session, notif, false);

                } catch (UnauthorizedUserException e) {
                    sendError(session, new Error("Error: Not authorized"));
                } catch (BadRequestException e) {
                    sendError(session, new Error("Error: Invalid game ID"));
                } catch (Exception e) {
                    sendError(session, new Error("Error: " + e.getMessage()));
                }
            }
            else {
                System.err.println("Unknown commandType in message: " + message);
                sendError(session, new Error("Invalid command type"));
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Failed to parse WebSocket message: " + e.getMessage());
            sendError(session, new Error("Malformed JSON"));
        }
    }

    private void handleMakeMove(Session session, MakeChessMove command) throws IOException {
        try {
            AuthData auth = Server.userAuthService.getAuth(command.getAuthToken());
            GameData game = Server.gameService.getGameData(command.getAuthToken(), command.getGameID());

            // Check if game is over first (avoid two errors)
            if (game.game().getGameOver()) {
                sendError(session, new Error("Error: The game is already over!"));
                return;
            }

            ChessGame.TeamColor userColor = getTeamColor(auth.username(), game);
            if (userColor == null) {
                sendError(session, new Error("Error: You are observing this game"));
                return;
            }

            if (game.game().getTeamTurn().equals(userColor)) {
                ChessPosition start = command.getMove().getStartPosition();
                ChessPosition end = command.getMove().getEndPosition();

                game.game().makeMove(command.getMove());
                Server.gameService.updateGame(auth.authToken(), game);

                LoadGame load = new LoadGame(game.game());
                broadcastMessage(session, load, true);

                String moveDesc = String.format("%s moved from %s to %s",
                        auth.username(),
                        positionToString(start),
                        positionToString(end));

                Notification notif = new Notification(moveDesc);

                broadcastMessage(session, notif, false);

            } else {
                sendError(session, new Error("Error: Not your turn"));
            }
        } catch (UnauthorizedUserException e) {
            sendError(session, new Error("Error: Not authorized"));
        } catch (BadRequestException e) {
            sendError(session, new Error("Error: invalid game"));
        } catch (InvalidMoveException e) {
            // Combine both error messages into one (game over was getting weird double error message)
            String errorMsg = "Error: Invalid move - " + e.getMessage();
            if (e.getMessage().contains("promotion")) {
                errorMsg += " (You may need to specify a promotion piece)";
            }
            sendError(session, new Error(errorMsg));
        }
    }




    private String positionToString(ChessPosition position) {
        char col = (char) ('a' + position.getColumn() - 1);
        return "" + col + position.getRow();
    }

    private void handleResignation(Session session, Resignation command) throws IOException {
        try {
            AuthData auth = Server.userAuthService.getAuth(command.getAuthToken());
            GameData game = Server.gameService.getGameData(command.getAuthToken(), command.getGameID());
            ChessGame.TeamColor userColor = getTeamColor(auth.username(), game);
            String winner = game.whiteUsername().equals(auth.username()) ?
                    game.blackUsername() : game.whiteUsername();
            String opponentUsername = userColor == ChessGame.TeamColor.WHITE ? game.blackUsername() : game.whiteUsername();

            if (userColor == null) {
                sendError(session, new Error("Error: You are observing this game"));
                return;
            }

            if (game.game().getGameOver()) {
                sendError(session, new Error("Error: The game is already over!"));
                return;
            }

            game.game().setGameOver(true);
            Server.gameService.updateGame(auth.authToken(), game);

            Notification notif = new Notification(String.format(
                    "%s has resigned. %s wins!",
                    auth.username(),
                    winner
            ));
            broadcastMessage(session, notif, true);

        } catch (UnauthorizedUserException e) {
            sendError(session, new Error("Error: Not authorized"));
        } catch (BadRequestException e) {
            sendError(session, new Error("Error: invalid game"));
        }
    }

    private ChessGame.TeamColor getTeamColor(String username, GameData game) {
        if (username.equals(game.whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        }
        else if (username.equals(game.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        }
        else return null;
    }

    private void handleJoinPlayer(Session session, JoinPlayer command) throws IOException {
        try {
            AuthData auth = Server.userAuthService.getAuth(command.getAuthToken());
            GameData game = Server.gameService.getGameData(command.getAuthToken(), command.getGameID());

            ChessGame.TeamColor joiningColor = command.getColorString().equalsIgnoreCase("white") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;

            boolean correctColor = false;
            if (joiningColor == ChessGame.TeamColor.WHITE) {
                correctColor = Objects.equals(game.whiteUsername(), auth.username());
            }
            else {
                correctColor = Objects.equals(game.blackUsername(), auth.username());
            }

            if (!correctColor) {
                Error error = new Error("Error: attempting to join with wrong color");
                sendError(session, error);
                return;
            }

            Notification notif = new Notification("%s has joined the game as %s".formatted(auth.username(), command.getColorString()));
            broadcastMessage(session, notif);
            LoadGame load = new LoadGame(game.game());
            sendMessage(session, load);
        }
        catch (UnauthorizedUserException e) {
            sendError(session, new Error("Error: Not authorized"));
        } catch (BadRequestException e) {
            sendError(session, new Error("Error: Not a valid game"));
        }
    }

    public void sendMessage(Session session, ServerMessage message) throws IOException {
        session.getRemote().sendString(new Gson().toJson(message));
    }

    private void handleJoinObserver(Session session, JoinObserver command) throws IOException {
        try {
            AuthData auth = Server.userAuthService.getAuth(command.getAuthToken());
            Notification notif = new Notification("%s has joined the game as an observer".formatted(auth.username()));
            broadcastMessage(session, notif);
        }
        catch (UnauthorizedUserException e) {
            sendError(session, new Error("Error: Not authorized"));
        }
    }

    private void handleLeave(Session session, LeaveGame command) throws IOException {
        try {
            AuthData auth = Server.userAuthService.getAuth(command.getAuthToken());

            Notification notif = new Notification("%s has left the game".formatted(auth.username()));
            broadcastMessage(session, notif);

            session.close();
            Server.gameSessions.remove(session);

        } catch (UnauthorizedUserException e) {
            sendError(session, new Error("Error: Not authorized"));
        }
    }

    private void sendError(Session session, Error error) throws IOException {
        try {
            JsonObject errorObj = new JsonObject();
            errorObj.addProperty("errorMessage", error.getMessage());
            errorObj.addProperty("serverMessageType", "ERROR");

            if (session.isOpen()) {
                session.getRemote().sendString(new Gson().toJson(errorObj));
            }
        } catch (Exception e) {
            System.err.println("Failed to send error message: " + e.getMessage());
        }
    }

    public void broadcastMessage (Session currSession, ServerMessage message) throws IOException {
        broadcastMessage(currSession, message, false);
    }

    public void broadcastMessage (Session currSession, ServerMessage message,boolean toSelf) throws IOException {
        System.out.printf("Broadcasting (toSelf: %s): %s%n", toSelf, new Gson().toJson(message));
        for (Session session : Server.gameSessions.keySet()) {
            boolean inAGame = Server.gameSessions.get(session) != 0;
            boolean sameGame = Server.gameSessions.get(session).equals(Server.gameSessions.get(currSession));
            boolean isSelf = session == currSession;
            if ((toSelf || !isSelf) && inAGame && sameGame && session.isOpen()) {
                sendMessage(session, message);
            }
        }
    }
}


