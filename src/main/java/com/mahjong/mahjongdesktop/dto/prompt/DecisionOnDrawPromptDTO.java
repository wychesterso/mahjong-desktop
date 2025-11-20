package com.mahjong.mahjongdesktop.dto.prompt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mahjong.mahjongdesktop.dto.state.TableDTO;

import java.util.List;

/**
 * Prompt received when a decision needs to be made after drawing a tile (WIN / KONG / PASS).
 */
public class DecisionOnDrawPromptDTO {
    @JsonProperty("table")
    private TableDTO table;

    @JsonProperty("drawnTile")
    private String drawnTile;

    @JsonProperty("availableOptions")
    private List<String> availableOptions;

    public DecisionOnDrawPromptDTO() {}

    public DecisionOnDrawPromptDTO(TableDTO table, String drawnTile, List<String> availableOptions) {
        this.table = table;
        this.drawnTile = drawnTile;
        this.availableOptions = availableOptions;
    }

    public TableDTO getTable() {
        return table;
    }

    public void setTable(TableDTO table) {
        this.table = table;
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
}
