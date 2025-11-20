package com.mahjong.mahjongdesktop.network;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import com.mahjong.mahjongdesktop.dto.prompt.DecisionOnDiscardPromptDTO;
import com.mahjong.mahjongdesktop.dto.prompt.DecisionOnDrawPromptDTO;
import com.mahjong.mahjongdesktop.dto.prompt.DiscardAfterDrawPromptDTO;
import com.mahjong.mahjongdesktop.dto.prompt.DiscardPromptDTO;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahjong.mahjongdesktop.dto.state.GameStateDTO;

/**
 * Handles incoming STOMP frames and converts them into typed GameStateDTO objects.
 * Provides a listener registration API for UI controllers to receive updates.
 */
public class GameMessageHandler implements StompFrameHandler {

    private final List<Consumer<GameStateDTO>> stateListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<DecisionOnDrawPromptDTO>> decisionOnDrawPromptListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<DecisionOnDiscardPromptDTO>> decisionOnDiscardPromptListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<DiscardPromptDTO>> discardPromptListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<DiscardAfterDrawPromptDTO>> discardAfterDrawPromptListeners = new CopyOnWriteArrayList<>();

    private final ObjectMapper mapper = new ObjectMapper();
    private volatile GameStateDTO latestState;

    public void addStateListener(Consumer<GameStateDTO> listener) {
        stateListeners.add(listener);
    }
    public void addDecisionOnDrawPromptListener(Consumer<DecisionOnDrawPromptDTO> listener) {
        decisionOnDrawPromptListeners.add(listener);
    }
    public void addDecisionOnDiscardPromptListener(Consumer<DecisionOnDiscardPromptDTO> listener) {
        decisionOnDiscardPromptListeners.add(listener);
    }
    public void addDiscardPromptListener(Consumer<DiscardPromptDTO> listener) {
        discardPromptListeners.add(listener);
    }
    public void addDiscardAfterDrawPromptListener(Consumer<DiscardAfterDrawPromptDTO> listener) {
        discardAfterDrawPromptListeners.add(listener);
    }

    public void removeStateListener(Consumer<GameStateDTO> listener) {
        stateListeners.remove(listener);
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

        if (!(payload instanceof Map<?, ?> map)) {
            System.err.println("[GameMessageHandler] Invalid payload, expected Map");
            return;
        }

        Object typeObj = map.get("type");
        Object dataObj = map.get("data");

        if (!(typeObj instanceof String type)) {
            System.err.println("[GameMessageHandler] Missing or invalid 'type' field");
            return;
        }

        try {
            switch (type) {
                case "update" -> handleTypedMessage(
                        dataObj,
                        GameStateDTO.class,
                        stateListeners,
                        dto -> latestState = dto,
                        "game state update"
                );

                case "prompt_draw_decision" -> handleTypedMessage(
                        dataObj, DecisionOnDrawPromptDTO.class, decisionOnDrawPromptListeners,
                        null, "prompt_draw_decision"
                );

                case "prompt_discard_decision" -> handleTypedMessage(
                        dataObj, DecisionOnDiscardPromptDTO.class, decisionOnDiscardPromptListeners,
                        null, "prompt_discard_decision"
                );

                case "prompt_discard" -> handleTypedMessage(
                        dataObj, DiscardPromptDTO.class, discardPromptListeners,
                        null, "prompt_discard"
                );

                case "prompt_discard_on_draw" -> handleTypedMessage(
                        dataObj, DiscardAfterDrawPromptDTO.class, discardAfterDrawPromptListeners,
                        null, "prompt_discard_on_draw"
                );

                // TODO: everything below here
                case "prompt_end_game_decision" -> System.out.println(
                        "[GameMessageHandler] Received end_game_decision: " + dataObj
                );

                case "log" -> System.out.println(
                        "[GameMessageHandler] Log: " + dataObj
                );

                case "game_start" -> System.out.println(
                        "[GameMessageHandler] Received game_start"
                );

                case "game_end" -> System.out.println(
                        "[GameMessageHandler] Received game_end"
                );

                case "session_ended" -> System.out.println(
                        "[GameMessageHandler] Received session_ended"
                );

                default -> System.out.println(
                        "[GameMessageHandler] Ignored message: " + type
                );
            }
        } catch (Exception e) {
            System.err.println("[GameMessageHandler] Failed to handle message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private <T> void handleTypedMessage(
            Object dataObj,
            Class<T> dtoClass,
            List<Consumer<T>> listeners,
            Consumer<T> preDispatch,
            String label
    ) {
        T dto = mapper.convertValue(dataObj, dtoClass);
        if (dto == null) {
            System.err.println("[GameMessageHandler] Failed to parse " + label);
            return;
        }

        System.out.println("[GameMessageHandler] Received " + label);

        if (preDispatch != null) preDispatch.accept(dto);

        for (Consumer<T> listener : listeners) {
            try {
                listener.accept(dto);
            } catch (Exception e) {
                System.err.println("[GameMessageHandler] Listener error (" + label + "): " + e.getMessage());
            }
        }
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return Map.class;
    }
}