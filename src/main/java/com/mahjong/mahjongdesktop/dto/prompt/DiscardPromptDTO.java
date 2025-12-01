package com.mahjong.mahjongdesktop.dto.prompt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mahjong.mahjongdesktop.dto.state.GameStateDTO;
import com.mahjong.mahjongdesktop.dto.state.TableDTO;

/**
 * Prompt received for discarding a tile after a non-draw action (e.g. creating melds).
 */
public class DiscardPromptDTO {
    @JsonProperty("state")
    private GameStateDTO state;

    public DiscardPromptDTO() {}

    public DiscardPromptDTO(GameStateDTO state) {
        this.state = state;
    }

    public GameStateDTO getState() {
        return state;
    }

    public void setState(GameStateDTO state) {
        this.state = state;
    }
}
