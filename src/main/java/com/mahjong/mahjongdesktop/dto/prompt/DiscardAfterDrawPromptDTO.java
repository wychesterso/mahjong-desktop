package com.mahjong.mahjongdesktop.dto.prompt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mahjong.mahjongdesktop.dto.state.TableDTO;

/**
 * Prompt received for discarding a tile after a draw.
 */
public class DiscardAfterDrawPromptDTO {
    @JsonProperty("table")
    private TableDTO table;

    @JsonProperty("drawnTile")
    private String drawnTile;

    public DiscardAfterDrawPromptDTO() {}

    public DiscardAfterDrawPromptDTO(TableDTO table, String drawnTile) {
        this.table = table;
        this.drawnTile = drawnTile;
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
}
