package com.mahjong.mahjongdesktop.dto.state;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Represents the game table state visible to a player.
 */
public class TableDTO {
    @JsonProperty("discardPile")
    private List<String> discardPile;

    @JsonProperty("drawPileSize")
    private int drawPileSize;

    @JsonProperty("selfSeat")
    private String selfSeat;

    @JsonProperty("hands")
    private Map<String, HandDTO> hands;

    public TableDTO() {}

    public TableDTO(List<String> discardPile, int drawPileSize, String selfSeat, Map<String, HandDTO> hands) {
        this.discardPile = discardPile;
        this.drawPileSize = drawPileSize;
        this.selfSeat = selfSeat;
        this.hands = hands;
    }

    public List<String> getDiscardPile() {
        return discardPile;
    }

    public void setDiscardPile(List<String> discardPile) {
        this.discardPile = discardPile;
    }

    public int getDrawPileSize() {
        return drawPileSize;
    }

    public void setDrawPileSize(int drawPileSize) {
        this.drawPileSize = drawPileSize;
    }

    public String getSelfSeat() {
        return selfSeat;
    }

    public void setSelfSeat(String selfSeat) {
        this.selfSeat = selfSeat;
    }

    public Map<String, HandDTO> getHands() {
        return hands;
    }

    public void setHands(Map<String, HandDTO> hands) {
        this.hands = hands;
    }
}
