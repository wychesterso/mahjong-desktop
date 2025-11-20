package com.mahjong.mahjongdesktop.dto.prompt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mahjong.mahjongdesktop.dto.state.TableDTO;

/**
 * Prompt received for discarding a tile after a non-draw action (e.g. creating melds).
 */
public class DiscardPromptDTO {
    @JsonProperty("table")
    private TableDTO table;

    public DiscardPromptDTO() {}

    public DiscardPromptDTO(TableDTO table) {
        this.table = table;
    }

    public TableDTO getTable() {
        return table;
    }

    public void setTable(TableDTO table) {
        this.table = table;
    }
}
