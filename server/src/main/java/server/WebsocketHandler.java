package server;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
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

    //todo: add command types and implementation for websocket.commands
    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        System.out.printf("Received: %s\n", message);

        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case LEAVE:
                handleLeave(session, (LeaveGame) command);
                break;
            case JOIN_PLAYER:
                Server.gameSessions.replace(session, command.getGameID());
                handleJoinPlayer(session, (JoinPlayer) command);
                break;
            case JOIN_OBSERVER:
                Server.gameSessions.replace(session, command.getGameID());
                handleJoinObserver(session, (JoinObserver) command);
                break;
            case RESIGN:
                handleResignation(session, (Resignation) command);
                break;
            case MAKE_MOVE:
                handleMakeMove(session,(MakeChessMove) command);
                break;
        }
    }

    private void handleMakeMove(Session session, MakeChessMove command) throws IOException {
        try {
            AuthData auth = Server.userAuthService.getAuth(command.getAuthToken());
            GameData game = Server.gameService.getGameData(command.getAuthToken(), command.getGameID());
            ChessGame.TeamColor userColor = getTeamColor(auth.username(), game);
            if (userColor == null) {
                sendError(session, new Error("Error: You are observing this game"));
                return;
            }

            if (game.game().getTeamTurn().equals(userColor)) {
                game.game().makeMove(command.getMove());
                Server.gameService.updateGame(auth.authToken(), game);
                LoadGame load = new LoadGame(game.game());
                broadcastMessage(session, load, true);
            }
            else {
                sendError(session, new Error("Error: Not your turn"));
            }
        }
        catch (UnauthorizedUserException e) {
            sendError(session, new Error("Error: Not authorized"));
        } catch (BadRequestException e) {
            sendError(session, new Error("Error: invalid game"));
        } catch (InvalidMoveException e) {
            sendError(session, new Error("Error: invalid move"));
        }
    }

    private void handleResignation(Session session, Resignation command) throws IOException {
        try {
            AuthData auth = Server.userAuthService.getAuth(command.getAuthToken());
            GameData game = Server.gameService.getGameData(command.getAuthToken(), command.getGameID());
            ChessGame.TeamColor userColor = getTeamColor(auth.username(), game);

            String opponentUsername = userColor == ChessGame.TeamColor.WHITE ? game.blackUsername() : game.whiteUsername();

            if (userColor == null) {
                sendError(session, new Error("Error: You are observing this game"));
                return;
            }

            Notification notif = new Notification("%s has forfeited, %s wins!".formatted(auth.username(), opponentUsername));
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
            Notification notif = new Notification("%s has joined the game as %s".formatted(auth.username(), command.getColorString()));
            broadcastMessage(session, notif);
        }
        catch (UnauthorizedUserException e) {
            sendError(session, new Error("Error: Not authorized"));
        }
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
        session.getRemote().sendString(new Gson().toJson(error));
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
            if ((toSelf || !isSelf) && inAGame && sameGame) {
                session.getRemote().sendString(new Gson().toJson(message));
            }
        }
    }
}
