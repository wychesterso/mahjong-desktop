package com.mahjong.mahjongdesktop.dto.prompt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mahjong.mahjongdesktop.dto.state.GameStateDTO;
import com.mahjong.mahjongdesktop.dto.state.TableDTO;

/**
 * Prompt received for discarding a tile after a draw.
 */
public class DiscardAfterDrawPromptDTO {
    @JsonProperty("state")
    private GameStateDTO state;

    @JsonProperty("drawnTile")
    private String drawnTile;

    public DiscardAfterDrawPromptDTO() {}

    public DiscardAfterDrawPromptDTO(GameStateDTO state, String drawnTile) {
        this.state = state;
        this.drawnTile = drawnTile;
    }

    public GameStateDTO getState() {
        return state;
    }

    public void setState(GameStateDTO state) {
        this.state = state;
    }

    public String getDrawnTile() {
        return drawnTile;
    }

    public void setDrawnTile(String drawnTile) {
        this.drawnTile = drawnTile;
    }
}
