package com.mahjong.mahjongdesktop.dto.prompt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mahjong.mahjongdesktop.dto.state.GameStateDTO;
import com.mahjong.mahjongdesktop.dto.state.TableDTO;

import java.util.List;

/**
 * Prompt received when a decision needs to be made after drawing a tile (WIN / KONG / PASS).
 */
public class DecisionOnDrawPromptDTO {
    @JsonProperty("state")
    private GameStateDTO state;

    @JsonProperty("drawnTile")
    private String drawnTile;

    @JsonProperty("availableOptions")
    private List<String> availableOptions;

    @JsonProperty("availableBrightKongs")
    private List<String> availableBrightKongs;

    @JsonProperty("availableDarkKongs")
    private List<String> availableDarkKongs;

    public DecisionOnDrawPromptDTO() {}

    public DecisionOnDrawPromptDTO(GameStateDTO state, String drawnTile, List<String> availableOptions,
                                   List<String> availableBrightKongs, List<String> availableDarkKongs) {
        this.state = state;
        this.drawnTile = drawnTile;
        this.availableOptions = availableOptions;
        this.availableBrightKongs = availableBrightKongs;
        this.availableDarkKongs = availableDarkKongs;
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

    public List<String> getAvailableOptions() {
        return availableOptions;
    }

    public void setAvailableOptions(List<String> availableOptions) {
        this.availableOptions = availableOptions;
    }

    public List<String> getAvailableBrightKongs() {
        return availableBrightKongs;
    }

    public void setAvailableBrightKongs(List<String> availableBrightKongs) {
        this.availableBrightKongs = availableBrightKongs;
    }

    public List<String> getAvailableDarkKongs() {
        return availableDarkKongs;
    }

    public void setAvailableDarkKongs(List<String> availableDarkKongs) {
        this.availableDarkKongs = availableDarkKongs;
    }
}
