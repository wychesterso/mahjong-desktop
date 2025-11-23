package com.mahjong.mahjongdesktop.dto.state;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Set;

public class ScoringContextDTO {
    @JsonProperty("scoringPatterns")
    private List<String> scoringPatterns;

    @JsonProperty("flowers")
    private Set<String> flowers;

    @JsonProperty("revealedGroups")
    private List<List<String>> revealedGroups;

    @JsonProperty("concealedGroups")
    private List<List<String>> concealedGroups;

    @JsonProperty("winningGroup")
    private List<String> winningGroup;

    @JsonProperty("winningTile")
    private String winningTile;

    public ScoringContextDTO() {}

    public ScoringContextDTO(List<String> scoringPatterns,
                             Set<String> flowers,
                             List<List<String>> revealedGroups,
                             List<List<String>> concealedGroups,
                             List<String> winningGroup,
                             String winningTile) {
        this.scoringPatterns = scoringPatterns;
        this.flowers = flowers;
        this.revealedGroups = revealedGroups;
        this.concealedGroups = concealedGroups;
        this.winningGroup = winningGroup;
        this.winningTile = winningTile;
    }

    public List<String> getScoringPatterns() {
        return scoringPatterns;
    }

    public void setScoringPatterns(List<String> scoringPatterns) {
        this.scoringPatterns = scoringPatterns;
    }

    public Set<String> getFlowers() {
        return flowers;
    }

    public void setFlowers(Set<String> flowers) {
        this.flowers = flowers;
    }

    public List<List<String>> getRevealedGroups() {
        return revealedGroups;
    }

    public void setRevealedGroups(List<List<String>> revealedGroups) {
        this.revealedGroups = revealedGroups;
    }

    public List<List<String>> getConcealedGroups() {
        return concealedGroups;
    }

    public void setConcealedGroups(List<List<String>> concealedGroups) {
        this.concealedGroups = concealedGroups;
    }

    public List<String> getWinningGroup() {
        return winningGroup;
    }

    public void setWinningGroup(List<String> winningGroup) {
        this.winningGroup = winningGroup;
    }

    public String getWinningTile() {
        return winningTile;
    }

    public void setWinningTile(String winningTile) {
        this.winningTile = winningTile;
    }
}
