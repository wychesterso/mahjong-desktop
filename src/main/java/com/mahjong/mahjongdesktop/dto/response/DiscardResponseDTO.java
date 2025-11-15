package com.mahjong.mahjongdesktop.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response sent when a player discards a tile.
 */
public class DiscardResponseDTO {
    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("tile")
    private String tile;

    public DiscardResponseDTO() {}

    public DiscardResponseDTO(String roomId, String tile) {
        this.roomId = roomId;
        this.tile = tile;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getTile() {
        return tile;
    }

    public void setTile(String tile) {
        this.tile = tile;
    }
}
