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
        String message = new Gson().toJson(command);
        ws.sendMessage(message);
    }

    public void joinPlayer(int gameID, ChessGame.TeamColor color) {
        //sendCommand
    }

    public void joinObserver(int gameID) {
    }

    public void makeMove(int gameID, ChessMove move) {
    }

    public void leave(int gameID) {
        //sendCommand(new LeaveGame(authToken, gameID));
        if (ws != null && ws.session != null && ws.session.isOpen()) {
            sendCommand(new LeaveGame(authToken, gameID));
            closeWS();
        } else {
            System.out.println("Not connected to WebSocket");
        }
    }

    public void resign(int gameID) {
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
        //return http.joinGame(gameId, playerColor);
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
                // send CONNECT command after establishing connection
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