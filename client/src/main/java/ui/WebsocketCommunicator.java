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
                //this.session.addMessageHandler((MessageHandler.Whole<String>) this::handleMessage);
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

        //todo add notification and error message methods
        //add move made method
        public void handleMessage(String message) {
            System.out.println(message);
            //todo
            //notification
            //error
            //printgame
        }

        public void sendMessage(String message) {
            this.session.getAsyncRemote().sendText(message);
        }

        //todo: print executedMove method
    }