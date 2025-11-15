package com.mahjong.mahjongdesktop.network;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

public class GameSocketClient {

    private static final String WS_URL = "ws://localhost:8080/ws";
    private StompSession stompSession;
    private final GameMessageHandler handler;
    private final String jwt;
    private final String roomId;

    public GameSocketClient(String jwt, String roomId, GameMessageHandler handler) {
        this.jwt = jwt;
        this.roomId = roomId;
        this.handler = handler;
    }

    public void connect() {
        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new MappingJackson2MessageConverter());

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Authorization", "Bearer " + jwt);

        client.connect(WS_URL, headers, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                stompSession = session;
                subscribeToTopics();
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                handler.handleMessage(payload);
            }
        });
    }

    public void disconnect() {
        if (stompSession != null && stompSession.isConnected()) {
            try {
                stompSession.disconnect();
                System.out.println("WebSocket disconnected.");
            } catch (Exception e) {
                System.err.println("Error while disconnecting WebSocket: " + e.getMessage());
            } finally {
                stompSession = null;
            }
        }
    }

    private void subscribeToTopics() {
        stompSession.subscribe("/topic/room/" + roomId, handler);
        stompSession.subscribe("/user/queue/game", handler);
    }

//    public void sendDiscardResponse(DiscardResponseDTO dto) {
//        stompSession.send("/app/game/respondDiscard", dto);
//    }
}