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

    public DecisionResponseDTO() {}

    public DecisionResponseDTO(String roomId, String decision) {
        this.roomId = roomId;
        this.decision = decision;
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
}
