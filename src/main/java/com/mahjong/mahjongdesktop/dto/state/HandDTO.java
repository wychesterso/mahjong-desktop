package com.mahjong.mahjongdesktop.dto.state;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Set;

/**
 * Represents a player's hand in the game.
 */
public class HandDTO {
    @JsonProperty("concealedTiles")
    private List<String> concealedTiles;

    @JsonProperty("concealedTileCount")
    private int concealedTileCount;

    @JsonProperty("revealedMelds")
    private List<List<String>> revealedMelds;

    @JsonProperty("flowers")
    private Set<String> flowers;

    public HandDTO() {}

    public HandDTO(List<String> concealedTiles, int concealedTileCount,
                   List<List<String>> revealedMelds, Set<String> flowers) {
        this.concealedTiles = concealedTiles;
        this.concealedTileCount = concealedTileCount;
        this.revealedMelds = revealedMelds;
        this.flowers = flowers;
    }

    public List<String> getConcealedTiles() {
        return concealedTiles;
    }

    public void setConcealedTiles(List<String> concealedTiles) {
        this.concealedTiles = concealedTiles;
    }

    public int getConcealedTileCount() {
        return concealedTileCount;
    }

    public void setConcealedTileCount(int concealedTileCount) {
        this.concealedTileCount = concealedTileCount;
    }

    public List<List<String>> getRevealedMelds() {
        return revealedMelds;
    }

    public void setRevealedMelds(List<List<String>> revealedMelds) {
        this.revealedMelds = revealedMelds;
    }

    public Set<String> getFlowers() {
        return flowers;
    }

    public void setFlowers(Set<String> flowers) {
        this.flowers = flowers;
    }
}
