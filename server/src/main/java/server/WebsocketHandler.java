package server;

import com.google.gson.Gson;
import dataaccess.UnauthorizedUserException;
import model.AuthData;
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
        //session.getRemote().sendString("WebSocket response: " + message);

        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case LEAVE:
                handleLeave(session, (LeaveGame) command);
                break;
            // Add cases
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
