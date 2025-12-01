package com.mahjong.mahjongdesktop.dto.prompt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mahjong.mahjongdesktop.dto.state.GameStateDTO;
import com.mahjong.mahjongdesktop.dto.state.TableDTO;

import java.util.List;

/**
 * Prompt received when a decision needs to be made after another player discards a tile (WIN / KONG / PONG / SHEUNG / PASS).
 */
public class DecisionOnDiscardPromptDTO {
    @JsonProperty("state")
    private GameStateDTO state;

    @JsonProperty("discardedTile")
    private String discardedTile;

    @JsonProperty("discarder")
    private String discarder;

    @JsonProperty("availableOptions")
    private List<String> availableOptions;

    @JsonProperty("sheungCombos")
    private List<List<String>> sheungCombos;

    public DecisionOnDiscardPromptDTO() {}

    public DecisionOnDiscardPromptDTO(GameStateDTO state, String discardedTile, String discarder, List<String> availableOptions, List<List<String>> sheungCombos) {
        this.state = state;
        this.discardedTile = discardedTile;
        this.discarder = discarder;
        this.availableOptions = availableOptions;
        this.sheungCombos = sheungCombos;
    }

    public GameStateDTO getState() {
        return state;
    }

    public void setState(GameStateDTO state) {
        this.state = state;
    }

    public String getDiscardedTile() {
        return discardedTile;
    }

    public void setDiscardedTile(String discardedTile) {
        this.discardedTile = discardedTile;
    }

    public String getDiscarder() {
        return discarder;
    }

    public void setDiscarder(String discarder) {
        this.discarder = discarder;
    }

    public List<String> getAvailableOptions() {
        return availableOptions;
    }

    public void setAvailableOptions(List<String> availableOptions) {
        this.availableOptions = availableOptions;
    }

    public List<List<String>> getSheungCombos() {
        return sheungCombos;
    }

    public void setSheungCombos(List<List<String>> sheungCombos) {
        this.sheungCombos = sheungCombos;
    }
}

