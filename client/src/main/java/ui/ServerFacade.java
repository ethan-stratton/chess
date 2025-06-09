package ui;

import java.util.*;

import chess.ChessGame;
import chess.ChessMove;

import com.google.gson.Gson;

import model.GameData;

import websocket.messages.*;
import websocket.commands.*;

import java.io.IOException;

public class ServerFacade {

    HttpCommunicator http;
    String baseURL;

    WebsocketCommunicator ws;
    String serverDomain;
    String authToken;

    public ServerFacade() throws Exception {
        this("localhost:8080");
    }

    public ServerFacade(String serverDomain) throws Exception {
        this.serverDomain= serverDomain;
        http = new HttpCommunicator(this, serverDomain);
        //ws = new WebsocketCommunicator(serverDomain);
    }

    protected String getAuth(){
        return authToken;
    }

    protected void setAuth(String authToken){
        this.authToken = authToken;
    }

    public boolean register(String username, String password, String email) {
        return http.register(username, password, email);
    }

    public void setServerPort(int port) {
        this.baseURL = "http://localhost:" + port;
    }

    public void connectWS() {
        try {
            ws = new WebsocketCommunicator(serverDomain);
        }
        catch (Exception e) {
            System.out.println("Failed to make connection with server");
        }
    }

    public void closeWS() {
        try {
            if (ws != null && ws.session != null && ws.session.isOpen()) {
                ws.session.close();
            }
        } catch (IOException e) {
            System.out.println("Failed to close WebSocket: " + e.getMessage());
        } finally {
            ws = null;
        }
    }

    public void sendWSMessage(String message) {
        ws.sendMessage(message);
    }

    public void sendCommand(UserGameCommand command) {
        try {
            if (ws == null || !ws.session.isOpen()) {
                initializeWebSocket(command.getGameID());
            }
            String message = new Gson().toJson(command);
            ws.sendMessage(message);
        } catch (Exception e) {
            System.out.println("Failed to send command: " + e.getMessage());
        }
    }

    public void joinPlayer(int gameID, ChessGame.TeamColor color) {
        initializeWebSocket(gameID);
        sendCommand(new JoinPlayer(authToken, gameID, color));
    }

    public void joinObserver(int gameID) {
        initializeWebSocket(gameID);
        sendCommand(new JoinObserver(authToken, gameID));
    }

    public void makeChessMove(int gameID, ChessMove move) {
        sendCommand(new MakeChessMove(authToken, gameID, move));
    }

    public void leave(int gameID) {
        if (ws == null || !ws.session.isOpen()) {
            initializeWebSocket(gameID); // Reconnect?
        }
        sendCommand(new LeaveGame(authToken, gameID));
        closeWS();
    }

    public void resign(int gameID) {
        try {
            if (ws == null || !ws.session.isOpen()) {
                initializeWebSocket(gameID);
            }
            sendCommand(new Resignation(authToken, gameID));
            Thread.sleep(200);
            closeWS();
        } catch (Exception e) {
            System.out.println("Resignation failed: " + e.getMessage());
        }
    }


    public boolean logout() {
        return http.logout();
    }

    public boolean login(String username, String password) {
        return http.login(username, password);
    }

    public int createGame(String gameName) {
        return http.createGame(gameName);
    }

    public HashSet<GameData> listGames() {
        return http.listGames();
    }

    public boolean joinGame(int gameId, String playerColor) {
        boolean success = http.joinGame(gameId, playerColor);
        if (success) {
            initializeWebSocket(gameId);
        }
        return success;
    }

    private void initializeWebSocket(int gameID) {
        try {
            if (ws == null || !ws.session.isOpen()) {
                ws = new WebsocketCommunicator(serverDomain);
                sendCommand(new UserGameCommand(
                        UserGameCommand.CommandType.CONNECT,
                        authToken,
                        gameID
                ));
            }
        } catch (Exception e) {
            System.out.println("Failed to initialize WebSocket: " + e.getMessage());
        }
    }
}