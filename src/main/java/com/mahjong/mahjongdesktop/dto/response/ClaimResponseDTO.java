package com.mahjong.mahjongdesktop.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response sent when a player claims a tile with a specific combination (e.g., SHEUNG).
 */
public class ClaimResponseDTO {
    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("decision")
    private String decision;

    @JsonProperty("sheungCombo")
    private List<String> sheungCombo;

    public ClaimResponseDTO() {}

    public ClaimResponseDTO(String roomId, String decision, List<String> sheungCombo) {
        this.roomId = roomId;
        this.decision = decision;
        this.sheungCombo = sheungCombo;
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

    public List<String> getSheungCombo() {
        return sheungCombo;
    }

    public void setSheungCombo(List<String> sheungCombo) {
        this.sheungCombo = sheungCombo;
    }
}
