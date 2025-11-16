package com.mahjong.mahjongdesktop.network;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahjong.mahjongdesktop.dto.state.GameStateDTO;

/**
 * Handles incoming STOMP frames and converts them into typed GameStateDTO objects.
 * Provides a listener registration API for UI controllers to receive updates.
 */
public class GameMessageHandler implements StompFrameHandler {

    private final List<Consumer<GameStateDTO>> listeners = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private volatile GameStateDTO latestState;

    public void addListener(Consumer<GameStateDTO> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<GameStateDTO> listener) {
        listeners.remove(listener);
    }

    public GameStateDTO getLatestState() {
        return latestState;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        handleMessage(payload);
    }

    public void handleMessage(Object payload) {
        System.out.println(payload.toString());

        try {
            GameStateDTO state = null;

            if (payload instanceof Map<?, ?> map) {
                // expect { "type": "update", "data": GameStateDTO }
                Object typeObj = map.get("type");
                Object dataObj = map.get("data");

                if ("update".equals(typeObj) && dataObj != null) {
                    // extract GameStateDTO from data field
                    state = mapper.convertValue(dataObj, GameStateDTO.class);
                    System.out.println("Received wrapped game state update");
                } else {
                    // ignore non-update messages
                    System.out.println("Ignoring non-update message from server: " + typeObj);
                    return;
                }
            }
//            else if (payload instanceof String) {
//                // try to parse JSON string
//                state = mapper.readValue((String) payload, GameStateDTO.class);
//            }

            if (state != null) {
                latestState = state;
                System.out.println("Game state updated: currentTurn=" + state.getCurrentTurn() + 
                                 ", gameActive=" + state.isGameActive());
                for (Consumer<GameStateDTO> listener : listeners) {
                    try {
                        listener.accept(state);
                    } catch (Exception e) {
                        System.err.println("Listener error: " + e.getMessage());
                    }
                }
            } else {
                System.err.println("Received unknown payload type: " + (payload == null ? "null" : payload.getClass()));
            }
        } catch (Exception e) {
            System.err.println("Failed to handle message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return Map.class;
    }
}