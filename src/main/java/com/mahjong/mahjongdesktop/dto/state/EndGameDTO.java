package com.mahjong.mahjongdesktop.dto.state;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Set;

public class EndGameDTO {
    @JsonProperty("result")
    private String result;

    @JsonProperty("winners")
    private Map<String, ScoringContextDTO> winners;

    @JsonProperty("loserSeats")
    private Set<String> loserSeats;

    @JsonProperty("tableDTO")
    TableDTO tableDTO;

    public EndGameDTO() {}

    public EndGameDTO(String result,
                      Map<String, ScoringContextDTO> winners,
                      Set<String> loserSeats,
                      TableDTO tableDTO) {
        this.result = result;
        this.winners = winners;
        this.loserSeats = loserSeats;
        this.tableDTO = tableDTO;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Map<String, ScoringContextDTO> getWinners() {
        return winners;
    }

    public void setWinners(Map<String, ScoringContextDTO> winners) {
        this.winners = winners;
    }

    public Set<String> getLoserSeats() {
        return loserSeats;
    }

    public void setLoserSeats(Set<String> loserSeats) {
        this.loserSeats = loserSeats;
    }

    public TableDTO getTableDTO() {
        return tableDTO;
    }

    public void setTableDTO(TableDTO tableDTO) {
        this.tableDTO = tableDTO;
    }
}
