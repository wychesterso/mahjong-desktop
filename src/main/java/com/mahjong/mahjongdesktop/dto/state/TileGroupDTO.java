package com.mahjong.mahjongdesktop.dto.state;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a group of tiles (e.g., a pong, sheung, or kong).
 */
public class TileGroupDTO {
    @JsonProperty("tiles")
    private List<String> tiles;

    public TileGroupDTO() {}

    public TileGroupDTO(List<String> tiles) {
        this.tiles = tiles;
    }

    public List<String> getTiles() {
        return tiles;
    }

    public void setTiles(List<String> tiles) {
        this.tiles = tiles;
    }
}
