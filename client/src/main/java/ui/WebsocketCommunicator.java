package ui;

import chess.ChessGame;
import com.google.gson.Gson;
import websocket.messages.LoadGame;
import websocket.messages.Notification;
import websocket.messages.Error;
import websocket.messages.ServerMessage;

import ui.BoardToString;
import ui.Gameplay;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static ui.EscapeSequences.ERASE_LINE;

public class WebsocketCommunicator extends Endpoint {

        Session session;

        public WebsocketCommunicator(String serverDomain) throws Exception {
            try {

                URI uri = new URI("ws://" + serverDomain + "/ws");
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                this.session = container.connectToServer(this, uri);

                this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                    @Override
                    public void onMessage(String message) {
                        handleMessage(message);
                    }
                });
            } catch (DeploymentException | IOException | URISyntaxException ex) {
                throw new Exception();
            }
        }

        @Override
        public void onOpen(Session session, EndpointConfig config) {
        }

        public void handleMessage(String message) {
            if (message.contains("\"serverMessageType\":\"LOAD_GAME\"")) {
                LoadGame loadGame = new Gson().fromJson(message, LoadGame.class);
                printMoveMade(loadGame.getGame());
            }
            else if (message.contains("\"serverMessageType\":\"NOTIFICATION\"")) {
                Notification notif = new Gson().fromJson(message, Notification.class);
                printNotification(notif.getMessage());
            }
            //add error class with error message
            //System.out.print(ERASE_LINE + "\r");
            //System.out.println(SET_TEXT_COLOR_RED + "[ERROR] " + message + RESET_TEXT_COLOR);
            else if (message.contains("\"serverMessageType\":\"ERROR\"")) {
                Error error = new Gson().fromJson(message, Error.class);
                printNotification(error.getMessage());
            }
        }

        private void printNotification(String message) {
            System.out.print(ERASE_LINE + '\r');
            System.out.println(message);
        }

        private void printMoveMade(ChessGame game) {
            System.out.print(ERASE_LINE + "\r");
            Gameplay.boardRepr.updateGame(game);
            Gameplay.boardRepr.printBoard(Gameplay.color, null);
            System.out.print("[IN-GAME] >>> ");
        }

        public void sendMessage(String message) {
            this.session.getAsyncRemote().sendText(message);
        }
    }