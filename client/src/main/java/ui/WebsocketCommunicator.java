package ui;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

    public class WebsocketCommunicator extends Endpoint {

        Session session;

        public WebsocketCommunicator(String serverDomain) throws Exception {
            try {
                URI uri = new URI("ws://" + serverDomain + "/ws");
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                this.session = container.connectToServer(this, uri);
                this.session.addMessageHandler((MessageHandler.Whole<String>) this::handleMessage);
            } catch (DeploymentException | IOException | URISyntaxException ex) {
                throw new Exception();
            }
        }

        @Override
        public void onOpen(Session session, EndpointConfig config) {
        }

        //todo add notification and error message methods
        //add move made method
        public void handleMessage(String message) {
            System.out.println(message);
        }

        public void sendMessage(String message) {
            this.session.getAsyncRemote().sendText(message);
        }

        //debug script
//        public static void main(String[] args) {
//            try {
//                WebsocketCommunicator client = new WebsocketCommunicator("localhost:8080");
//
//                client.sendMessage("Hello Server!\n");
//                client.sendMessage("Another test message");
//
//                // keep connection open briefly to get the response
//                Thread.sleep(5000);
//
//                client.session.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }