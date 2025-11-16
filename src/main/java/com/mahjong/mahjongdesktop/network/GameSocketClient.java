package com.mahjong.mahjongdesktop.network;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.mahjong.mahjongdesktop.dto.response.ClaimResponseDTO;
import com.mahjong.mahjongdesktop.dto.response.DecisionResponseDTO;
import com.mahjong.mahjongdesktop.dto.response.DiscardResponseDTO;
import com.mahjong.mahjongdesktop.dto.response.EndGameDecisionDTO;

public class GameSocketClient {

    private static final String WS_URL = "ws://localhost:8080/ws?token=%s";
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

        // Pass JWT as query param, not header
        String wsUrlWithToken = String.format(WS_URL, jwt);
        client.connect(wsUrlWithToken, new StompSessionHandlerAdapter() {
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

    private boolean isConnected() {
        return stompSession != null && stompSession.isConnected();
    }

    public void sendDiscardResponse(String tile) {
        if (!isConnected()) {
            System.err.println("Could not send discard response!");
            return;
        }
        DiscardResponseDTO dto = new DiscardResponseDTO(roomId, tile);
        stompSession.send("/app/game/respondDiscard", dto);
    }

    public void sendDrawDecision(String decision) {
        if (!isConnected()) {
            System.err.println("Could not send draw decision!");
            return;
        }
        DecisionResponseDTO dto = new DecisionResponseDTO(roomId, decision);
        stompSession.send("/app/game/respondDrawDecision", dto);
    }

    public void sendDiscardClaim(String decision, java.util.List<String> sheungCombo) {
        if (!isConnected()) {
            System.err.println("Could not send discard claim!");
            return;
        }
        ClaimResponseDTO dto = new ClaimResponseDTO(roomId, decision, sheungCombo);
        stompSession.send("/app/game/respondDiscardDecision", dto);
    }

    public void sendEndGameDecision(String decision) {
        if (!isConnected()) {
            System.err.println("Could not send end-game decision!");
            return;
        }
        EndGameDecisionDTO dto = new EndGameDecisionDTO(roomId, decision);
        stompSession.send("/app/game/endGameDecision", dto);
    }
}