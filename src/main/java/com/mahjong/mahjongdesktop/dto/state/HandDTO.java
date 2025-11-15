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

    @JsonProperty("sheungs")
    private List<List<String>> sheungs;

    @JsonProperty("pongs")
    private List<List<String>> pongs;

    @JsonProperty("brightKongs")
    private List<List<String>> brightKongs;

    @JsonProperty("darkKongs")
    private List<List<String>> darkKongs;

    @JsonProperty("darkKongCount")
    private int darkKongCount;

    @JsonProperty("flowers")
    private Set<String> flowers;

    public HandDTO() {}

    public HandDTO(List<String> concealedTiles, int concealedTileCount,
                   List<List<String>> sheungs, List<List<String>> pongs,
                   List<List<String>> brightKongs, List<List<String>> darkKongs,
                   int darkKongCount, Set<String> flowers) {
        this.concealedTiles = concealedTiles;
        this.concealedTileCount = concealedTileCount;
        this.sheungs = sheungs;
        this.pongs = pongs;
        this.brightKongs = brightKongs;
        this.darkKongs = darkKongs;
        this.darkKongCount = darkKongCount;
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

    public List<List<String>> getSheungs() {
        return sheungs;
    }

    public void setSheungs(List<List<String>> sheungs) {
        this.sheungs = sheungs;
    }

    public List<List<String>> getPongs() {
        return pongs;
    }

    public void setPongs(List<List<String>> pongs) {
        this.pongs = pongs;
    }

    public List<List<String>> getBrightKongs() {
        return brightKongs;
    }

    public void setBrightKongs(List<List<String>> brightKongs) {
        this.brightKongs = brightKongs;
    }

    public List<List<String>> getDarkKongs() {
        return darkKongs;
    }

    public void setDarkKongs(List<List<String>> darkKongs) {
        this.darkKongs = darkKongs;
    }

    public int getDarkKongCount() {
        return darkKongCount;
    }

    public void setDarkKongCount(int darkKongCount) {
        this.darkKongCount = darkKongCount;
    }

    public Set<String> getFlowers() {
        return flowers;
    }

    public void setFlowers(Set<String> flowers) {
        this.flowers = flowers;
    }
}
