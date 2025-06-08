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
            ws.session.close();
            ws = null;
        }
        catch (IOException e) {
            System.out.println("Failed to close connection with server");
        }
    }

    public void sendWSMessage(String message) {
        ws.sendMessage(message);
    }

    public void sendCommand(UserGameCommand command) {
        String message = new Gson().toJson(command);
        ws.sendMessage(message);
    }

    //need joinPlayer, joinObserver, makeChessMove, leaveGame, and resignation commands implemented
    public void joinPlayer() {
    }

    public void joinObserver() {
    }

    public void makeMove() {
    }

    public void leave() {
    }

    public void resign() {
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
        return http.joinGame(gameId, playerColor);
    }
}