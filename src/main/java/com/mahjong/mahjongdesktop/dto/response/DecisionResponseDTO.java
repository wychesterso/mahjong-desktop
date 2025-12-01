package com.mahjong.mahjongdesktop.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response sent when a player makes a simple decision (e.g., PASS, PONG, WIN).
 */
public class DecisionResponseDTO {
    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("decision")
    private String decision;

    @JsonProperty("kongTile")
    private String kongTile;

    public DecisionResponseDTO() {}

    public DecisionResponseDTO(String roomId, String decision, String kongTile) {
        this.roomId = roomId;
        this.decision = decision;
        this.kongTile = kongTile;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getKongTile() {
        return kongTile;
    }

    public void setKongTile(String kongTile) {
        this.kongTile = kongTile;
    }
}
