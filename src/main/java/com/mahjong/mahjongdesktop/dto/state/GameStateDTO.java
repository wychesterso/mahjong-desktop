package com.mahjong.mahjongdesktop.dto.state;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Represents the complete game state sent from the server.
 */
public class GameStateDTO {
    @JsonProperty("table")
    private TableDTO table;

    @JsonProperty("currentTurn")
    private String currentTurn;

    @JsonProperty("windSeat")
    private String windSeat;

    @JsonProperty("zhongSeat")
    private String zhongSeat;

    @JsonProperty("playerNames")
    private Map<String, String> playerNames;

    @JsonProperty("expectedClaimants")
    private Map<String, List<String>> expectedClaimants;

    @JsonProperty("lastDiscardedTile")
    private String lastDiscardedTile;

    @JsonProperty("lastDiscarder")
    private String lastDiscarder;

    @JsonProperty("availableDecisions")
    private List<String> availableDecisions;

    @JsonProperty("drawnTile")
    private String drawnTile;

    @JsonProperty("gameActive")
    private boolean gameActive;

    @JsonProperty("winnerSeats")
    private List<String> winnerSeats;

    @JsonProperty("numDraws")
    private int numDraws;

    public GameStateDTO() {}

    public GameStateDTO(TableDTO table, String currentTurn, String windSeat, String zhongSeat,
                       Map<String, String> playerNames, Map<String, List<String>> expectedClaimants,
                       String lastDiscardedTile, String lastDiscarder, List<String> availableDecisions,
                       String drawnTile, boolean gameActive, List<String> winnerSeats, int numDraws) {
        this.table = table;
        this.currentTurn = currentTurn;
        this.windSeat = windSeat;
        this.zhongSeat = zhongSeat;
        this.playerNames = playerNames;
        this.expectedClaimants = expectedClaimants;
        this.lastDiscardedTile = lastDiscardedTile;
        this.lastDiscarder = lastDiscarder;
        this.availableDecisions = availableDecisions;
        this.drawnTile = drawnTile;
        this.gameActive = gameActive;
        this.winnerSeats = winnerSeats;
        this.numDraws = numDraws;
    }

    public TableDTO getTable() {
        return table;
    }

    public void setTable(TableDTO table) {
        this.table = table;
    }

    public String getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(String currentTurn) {
        this.currentTurn = currentTurn;
    }

    public String getWindSeat() {
        return windSeat;
    }

    public void setWindSeat(String windSeat) {
        this.windSeat = windSeat;
    }

    public String getZhongSeat() {
        return zhongSeat;
    }

    public void setZhongSeat(String zhongSeat) {
        this.zhongSeat = zhongSeat;
    }

    public Map<String, String> getPlayerNames() {
        return playerNames;
    }

    public void setPlayerNames(Map<String, String> playerNames) {
        this.playerNames = playerNames;
    }

    public Map<String, List<String>> getExpectedClaimants() {
        return expectedClaimants;
    }

    public void setExpectedClaimants(Map<String, List<String>> expectedClaimants) {
        this.expectedClaimants = expectedClaimants;
    }

    public String getLastDiscardedTile() {
        return lastDiscardedTile;
    }

    public void setLastDiscardedTile(String lastDiscardedTile) {
        this.lastDiscardedTile = lastDiscardedTile;
    }

    public String getLastDiscarder() {
        return lastDiscarder;
    }

    public void setLastDiscarder(String lastDiscarder) {
        this.lastDiscarder = lastDiscarder;
    }

    public List<String> getAvailableDecisions() {
        return availableDecisions;
    }

    public void setAvailableDecisions(List<String> availableDecisions) {
        this.availableDecisions = availableDecisions;
    }

    public String getDrawnTile() {
        return drawnTile;
    }

    public void setDrawnTile(String drawnTile) {
        this.drawnTile = drawnTile;
    }

    public boolean isGameActive() {
        return gameActive;
    }

    public void setGameActive(boolean gameActive) {
        this.gameActive = gameActive;
    }

    public List<String> getWinnerSeats() {
        return winnerSeats;
    }

    public void setWinnerSeats(List<String> winnerSeats) {
        this.winnerSeats = winnerSeats;
    }

    public int getNumDraws() {
        return numDraws;
    }

    public void setNumDraws(int numDraws) {
        this.numDraws = numDraws;
    }
}
