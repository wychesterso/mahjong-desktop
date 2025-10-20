package com.mahjong.mahjongdesktop.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class GameController {

    @FXML private HBox southHand, northHand, eastHand, westHand;
    @FXML private HBox southMelds, northMelds, eastMelds, westMelds;
    @FXML private HBox southDiscards, northDiscards, eastDiscards, westDiscards;
    @FXML private Label southNameLabel, northNameLabel, eastNameLabel, westNameLabel;
    @FXML private StackPane tableCenter;

    @FXML
    public void initialize() {
        for (int i = 0; i < 13; i++) {
            Label tile = new Label("🀄");
            tile.setStyle("-fx-font-size: 24px;");
            southHand.getChildren().add(tile);
        }
    }

    @FXML private void handleDraw() { System.out.println("Draw clicked"); }
    @FXML private void handleDiscard() { System.out.println("Discard clicked"); }
    @FXML private void handleChi() { System.out.println("Chi clicked"); }
    @FXML private void handlePong() { System.out.println("Pong clicked"); }
    @FXML private void handleKong() { System.out.println("Kong clicked"); }
    @FXML private void handlePass() { System.out.println("Pass clicked"); }
}