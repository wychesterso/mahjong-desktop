package com.mahjong.mahjongdesktop.network;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.lang.reflect.Type;
import java.util.function.Consumer;

public class GameMessageHandler implements StompFrameHandler {

    private static final String WS_URL = "ws://localhost:8080/ws";
    private StompSession session;
    private final List<Consumer<Map<String, Object>>> listeners = new ArrayList<>();

    public void addListener(Consumer<Map<String, Object>> listener) {
        listeners.add(listener);
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        handleMessage(payload);
    }

    public void handleMessage(Object payload) {
        Map<String, Object> map = (Map<String, Object>) payload;
        for (Consumer<Map<String, Object>> listener : listeners) {
            listener.accept(map);
        }
    }

    @Override
    public Type getPayloadType(StompHeaders headers) { return Object.class; }

    public void connect() {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        try {
            session = stompClient
                    .connect(WS_URL, new GameSessionHandler())
                    .get();
            System.out.println("Connected to WebSocket server at " + WS_URL);
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Failed to connect to WebSocket: " + e.getMessage());
        }
    }

    private static class GameSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("WebSocket connected!");
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            System.err.println("WebSocket transport error: " + exception.getMessage());
        }
    }
}