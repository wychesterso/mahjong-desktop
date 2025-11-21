package com.mahjong.mahjongdesktop.controllers;

import com.mahjong.mahjongdesktop.AppNavigator;
import com.mahjong.mahjongdesktop.AppState;
import com.mahjong.mahjongdesktop.domain.Tile;
import com.mahjong.mahjongdesktop.dto.prompt.DecisionOnDiscardPromptDTO;
import com.mahjong.mahjongdesktop.dto.prompt.DecisionOnDrawPromptDTO;
import com.mahjong.mahjongdesktop.dto.prompt.DiscardAfterDrawPromptDTO;
import com.mahjong.mahjongdesktop.dto.prompt.DiscardPromptDTO;
import com.mahjong.mahjongdesktop.dto.state.GameStateDTO;
import com.mahjong.mahjongdesktop.dto.state.HandDTO;
import com.mahjong.mahjongdesktop.network.GameMessageHandler;
import com.mahjong.mahjongdesktop.ui.TileNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.*;

public class GameController {

    // ============ player zones ============
    @FXML private Label northNameLabel, southNameLabel, eastNameLabel, westNameLabel;
    @FXML private Region northTurnIndicator, southTurnIndicator, eastTurnIndicator, westTurnIndicator;

    @FXML private HBox northContainer, northMelds, northHand, northFlowers;
    @FXML private HBox eastMelds, eastHand, eastFlowers;
    @FXML private VBox eastContainer;
    @FXML private HBox westMelds, westHand, westFlowers;
    @FXML private VBox westContainer;
    @FXML private HBox southContainer, southMelds, southFlowers, southHand, southDrawnTile;

    @FXML private GridPane discardPile;

    // ============ buttons ============
    @FXML private HBox actionButtonBox;
    @FXML private Button pongButton, sheungButton, kongButton, winButton, passButton;

    @FXML private HBox sheungSelector;
    @FXML private HBox sheungOptions;
    @FXML private Button cancelSheungBtn;

    @FXML private HBox endGameDecisionBox;
    @FXML private Button nextGameButton, exitButton;

    // ============ state ============
    private GameMessageHandler handler;
    private TileNode selectedTile;
    private String selfSeat; // the current player seat

    private boolean allowDiscard = true;
    private boolean decisionDraw = false; // true if prompted for decisionOnDraw, false if prompted for decisionOnDiscard
    private List<String> availableDecisions = List.of();
    private List<List<String>> sheungCombos = List.of();
    private String lastDiscardedTile = "";

    private final Map<String, Pane> handBoxes = new HashMap<>();
    private final Map<String, HBox> meldBoxes = new HashMap<>();
    private final Map<String, HBox> flowerBoxes = new HashMap<>();
    private final Map<String, Label> nameLabels = new HashMap<>();
    private final Map<String, Region> turnIndicators = new HashMap<>();

    private final Map<String, String> seatToUI = new HashMap<>();

    private boolean registeredWithHandlers = false;

    @FXML
    public void initialize() {
        // map seats to UI components (fixed)
        mapSeatsToUI();
        enforceFixedBoxSizes();
        rotateSideHands();

        // subscribe to game state updates
        handler = AppState.getGameMessageHandler();
        if (handler != null && !registeredWithHandlers) {
            registeredWithHandlers = true;

            handler.addStateListener(this::onGameState);
            handler.addDecisionOnDrawPromptListener(this::onDecisionOnDrawPrompt);
            handler.addDecisionOnDiscardPromptListener(this::onDecisionOnDiscardPrompt);
            handler.addDiscardPromptListener(this::onDiscardPrompt);
            handler.addDiscardAfterDrawPromptListener(this::onDiscardAfterDrawPrompt);
            handler.addEndGameDecisionPromptListener(v -> onEndGameDecisionPrompt());

            GameStateDTO initial = handler.getLatestState();
            if (initial != null && initial.getTable() != null) {
                selfSeat = initial.getTable().getSelfSeat();
                mapServerSeatsToUI(selfSeat);
                onGameState(initial);
            }
        } else {
            System.err.println("GameMessageHandler not present when GameController initialized.");
        }

        // wire button handlers
        pongButton.setOnAction(e -> handleClaim("PONG"));
        sheungButton.setOnAction(e -> handleClaim("SHEUNG"));
        kongButton.setOnAction(e -> handleClaim("BRIGHT_KONG"));
        winButton.setOnAction(e -> handleClaim("WIN"));
        passButton.setOnAction(e -> handleClaim("PASS"));
        updateDecisionButtons(List.of());

        nextGameButton.setOnAction(e -> sendEndGameDecision("NEXT_GAME"));
        exitButton.setOnAction(e -> sendEndGameDecision("EXIT"));

        cancelSheungBtn.setOnAction(e -> hideSheungSelector(true));
    }

    private void rotateSideHands() {
        // EAST hand (right side) -> rotate counterclockwise 90°
        eastContainer.setRotate(-90);
//        eastContainer.setPrefSize(200, 400);
//        eastHand.setTranslateY(eastHand.getWidth() / 2.0);
        // NORTH hand (top side) -> rotate 180°
        northContainer.setRotate(180);
        // WEST hand (left side) -> rotate clockwise 90°
        westContainer.setRotate(90);
//        westHand.setTranslateY(-westHand.getWidth() / 2.0);
        setContainerSizes();
    }

    private void setContainerSizes() {
        eastContainer.setPrefSize(150, 450);
        eastContainer.setMinSize(150, 450);
        eastContainer.setMaxSize(150, 450);

        westContainer.setPrefSize(150, 450);
        westContainer.setMinSize(150, 450);
        westContainer.setMaxSize(150, 450);
    }

    // ================= SEAT MAPPING =================

    private void mapServerSeatsToUI(String selfSeat) {
        seatToUI.clear();
        switch (selfSeat) {
            case "SOUTH" -> { // self at bottom
                seatToUI.put("SOUTH", "SOUTH");
                seatToUI.put("WEST", "EAST");
                seatToUI.put("NORTH", "NORTH");
                seatToUI.put("EAST", "WEST");
            }
            case "EAST" -> {
                seatToUI.put("EAST", "SOUTH");
                seatToUI.put("SOUTH", "EAST");
                seatToUI.put("WEST", "NORTH");
                seatToUI.put("NORTH", "WEST");
            }
            case "NORTH" -> {
                seatToUI.put("NORTH", "SOUTH");
                seatToUI.put("EAST", "EAST");
                seatToUI.put("SOUTH", "NORTH");
                seatToUI.put("WEST", "WEST");
            }
            case "WEST" -> {
                seatToUI.put("WEST", "SOUTH");
                seatToUI.put("NORTH", "EAST");
                seatToUI.put("EAST", "NORTH");
                seatToUI.put("SOUTH", "WEST");
            }
        }
    }

    private void mapSeatsToUI() {
        handBoxes.put("NORTH", northHand);
        handBoxes.put("SOUTH", southHand);
        handBoxes.put("EAST", eastHand);
        handBoxes.put("WEST", westHand);

        meldBoxes.put("NORTH", northMelds);
        meldBoxes.put("SOUTH", southMelds);
        meldBoxes.put("EAST", eastMelds);
        meldBoxes.put("WEST", westMelds);

        flowerBoxes.put("NORTH", northFlowers);
        flowerBoxes.put("SOUTH", southFlowers);
        flowerBoxes.put("EAST", eastFlowers);
        flowerBoxes.put("WEST", westFlowers);

        nameLabels.put("NORTH", northNameLabel);
        nameLabels.put("SOUTH", southNameLabel);
        nameLabels.put("EAST", eastNameLabel);
        nameLabels.put("WEST", westNameLabel);

        turnIndicators.put("NORTH", northTurnIndicator);
        turnIndicators.put("SOUTH", southTurnIndicator);
        turnIndicators.put("EAST", eastTurnIndicator);
        turnIndicators.put("WEST", westTurnIndicator);
    }

    // ================== ENFORCE FIXED SIZES ==================

    private void enforceFixedBoxSizes() {
        // Hands
//        handBoxes.values().forEach(box -> {
//            box.setPrefSize(400, 70);
//            box.setMinSize(400, 70);
//            box.setMaxSize(400, 70);
//        });

        // Melds
        meldBoxes.values().forEach(box -> {
            box.setPrefHeight(60);
            box.setMinHeight(60);
            box.setMaxHeight(60);
        });

        // Flowers
        flowerBoxes.values().forEach(box -> {
            box.setPrefHeight(50);
            box.setMinHeight(50);
            box.setMaxHeight(50);
        });

        // Discard pile
        discardPile.setPrefSize(550, 400);
        discardPile.setMinSize(550, 400);
        discardPile.setMaxSize(550, 400);
    }

    // ================== GAME STATE UPDATES ==================

    private void onGameState(GameStateDTO state) {
        if (state == null) return;

        if (selfSeat == null && state.getTable() != null && state.getTable().getSelfSeat() != null) {
            selfSeat = state.getTable().getSelfSeat();
            mapServerSeatsToUI(selfSeat);
        }

        Platform.runLater(() -> {
            updatePlayerNames(state);
            updateTurnIndicators(state.getCurrentTurn());
            updateDecisionBox(true);

            if (state.getTable() != null && state.getTable().getHands() != null) {
                updateAllHands(state);
                updateMelds(state);
            }

            if (state.getTable() != null && state.getTable().getDiscardPile() != null) {
                updateCenterDiscardPile(state.getTable().getDiscardPile());
                updateCenterDiscardPile(state.getTable().getDiscardPile());
            }

            hideSheungSelector(false); // this line is sus
        });
    }

    private void updatePlayerNames(GameStateDTO state) {
        if (state.getPlayerNames() == null) return;

        for (String position : List.of("SOUTH", "NORTH", "EAST", "WEST")) {
            String uiSeat = seatToUI.get(position);
            Label label = nameLabels.get(uiSeat);
            if (label != null) {
                String name = state.getPlayerNames().get(position);
                label.setText(position + ": " + (name == null ? "(vacant)" : name));
            }
        }
    }

    private void updateAllHands(GameStateDTO state) {
        for (String seat : new String[]{"NORTH", "SOUTH", "EAST", "WEST"}) {
            HandDTO hand = state.getTable().getHands().get(seat);
            String uiSeat = seatToUI.get(seat);

            Pane handBox = handBoxes.get(uiSeat);
            HBox flowerBox = flowerBoxes.get(uiSeat);
            boolean isPlayerHand = "SOUTH".equals(uiSeat);

            handBox.getChildren().clear();
            flowerBox.getChildren().clear();

            // flowers
            for (String flower : hand.getFlowers()) {
                TileNode tile = new TileNode(flower);
                tile.setClickable(false);
                enforceTileFixedSize(tile);
                flowerBox.getChildren().add(tile);
            }

            // tiles
            List<String> displayTiles = new ArrayList<>(hand.getConcealedTiles());

            // sort tiles before rendering
            if (isPlayerHand) {
                // generate the newly-drawn tile separately
                southDrawnTile.getChildren().clear();

                if (state.getDrawnTile() != null && displayTiles.getLast().equals(state.getDrawnTile())) {
                    displayTiles.removeLast();

                    TileNode tile = new TileNode(state.getDrawnTile());
                    tile.setClickable(true);
                    tile.setOnTileClicked(this::onSouthHandTileClicked);

                    enforceTileFixedSize(tile);
                    southDrawnTile.getChildren().add(tile);
                }

                // generate other concealed tiles
                displayTiles.sort(this::compareTileNames);

                for (String tileName : displayTiles) {
                    TileNode tile = new TileNode(tileName);
                    tile.setClickable(true);
                    tile.setOnTileClicked(this::onSouthHandTileClicked);

                    enforceTileFixedSize(tile);
                    handBox.getChildren().add(tile);
                }
            } else {
                for (int i = 0; i < hand.getConcealedTileCount(); i++) {
                    TileNode backTile = new TileNode(null);
                    backTile.setFaceUp(false);

                    enforceTileFixedSize(backTile);
                    handBox.getChildren().add(backTile);
                }
            }
        }
    }

    private int compareTileNames(String t1, String t2) {
        return Tile.valueOf(t1).compareTo(Tile.valueOf(t2));
    }

    private void enforceTileFixedSize(TileNode tile) {
        tile.setPrefSize(30, 40);
        tile.setMinSize(30, 40);
        tile.setMaxSize(30, 40);
    }

    private void updateMelds(GameStateDTO state) {
        for (String seat : new String[]{"NORTH", "SOUTH", "EAST", "WEST"}) {
            HandDTO hand = state.getTable().getHands().get(seat);
            String uiSeat = seatToUI.get(seat);
            HBox meldBox = meldBoxes.get(uiSeat);
            if (meldBox == null || hand == null) continue;

            meldBox.getChildren().clear();

            // sheungs
            if (hand.getSheungs() != null) {
                for (List<String> sheung : hand.getSheungs()) {
                    addMeld(sheung, meldBox);
                }
            }

            // pongs
            if (hand.getPongs() != null) {
                for (List<String> pong : hand.getPongs()) {
                    addMeld(pong, meldBox);
                }
            }

            // kongs
            if (hand.getBrightKongs() != null) {
                for (List<String> kong : hand.getBrightKongs()) {
                    addMeld(kong, meldBox);
                }
            }

            if (hand.getDarkKongs() != null) {
                for (List<String> kong : hand.getDarkKongs()) {
                    addMeld(kong, meldBox);
                }
            }
        }
    }

    private void addMeld(List<String> group, HBox meldBox) {
        HBox box = new HBox(2);
        for (String tile : group) {
            TileNode t = new TileNode(tile);
            t.setClickable(false);
            enforceTileFixedSize(t);
            box.getChildren().add(t);
        }

        meldBox.getChildren().add(box);

        // add spacer after each meld
        Region spacer = new Region();
        spacer.setPrefWidth(16);
        meldBox.getChildren().add(spacer);
    }

    private void updateCenterDiscardPile(List<String> discards) {
        discardPile.getChildren().clear();
        if (discards == null) return;

        int row = 0, col = 0;
        for (String tileName : discards) {
            TileNode tile = new TileNode(tileName);
            tile.setClickable(false);
            enforceTileFixedSize(tile);
            discardPile.add(tile, col, row);

            col++;
            if (col >= 11) {
                col = 0;
                row++;
            }
        }
    }

    // ================== PROMPTS ==================

    private void onDecisionOnDrawPrompt(DecisionOnDrawPromptDTO data) {
        if (data == null) return;
        decisionDraw = true;
        availableDecisions = data.getAvailableOptions();

        Platform.runLater(() -> {
            updateDecisionButtons(data.getAvailableOptions());
        });
    }

    private void onDecisionOnDiscardPrompt(DecisionOnDiscardPromptDTO data) {
        if (data == null) return;
        decisionDraw = false;
        availableDecisions = data.getAvailableOptions();
        lastDiscardedTile = data.getDiscardedTile();
        sheungCombos = data.getSheungCombos();

        Platform.runLater(() -> {
            // TODO: Maybe highlight discarded tile (data.getDiscardedTile) and put some marker on discarder (data.getDiscarder)
            updateDecisionButtons(data.getAvailableOptions());
        });
    }

    private void onDiscardPrompt(DiscardPromptDTO data) {
        System.out.println("[GameController] onDiscardPrompt");
        if (data == null) return;
        System.out.println("[GameController] onDiscardPrompt run");

        Platform.runLater(this::enableDiscard);
    }

    private void onDiscardAfterDrawPrompt(DiscardAfterDrawPromptDTO data) {
        System.out.println("[GameController] onDiscardAfterDrawPrompt");
        if (data == null) return;
        System.out.println("[GameController] onDiscardAfterDrawPrompt run");

        Platform.runLater(this::enableDiscard);
    }

    private void enableDiscard() {
        allowDiscard = true;
        southHand.setOpacity(1.0);
    }

    private void disableDiscard() {
        allowDiscard = false;
        southHand.setOpacity(0.7);
        if (selectedTile != null) {
            selectedTile.clearSelection();
            selectedTile = null;
        }
    }

    // ================== TURNS & DECISIONS ==================

    private void updateTurnIndicators(String currentTurn) {
        for (Region indicator : turnIndicators.values()) {
            indicator.setStyle("-fx-border-color: transparent; -fx-border-radius: 5; -fx-padding: 4;");
        }

        String uiSeat = seatToUI.get(currentTurn);
        if (uiSeat == null) return;

        Region current = turnIndicators.get(uiSeat);
        if (current != null) {
            current.setStyle("-fx-border-color: gold; -fx-border-radius: 5; -fx-padding: 4; -fx-border-width: 2;");
        }
    }

    private void updateDecisionButtons(List<String> decisions) {
        System.out.println("[GameController] Decisions=" + decisions);

        if (decisions != null) {
            setButtonState(pongButton, decisions.contains("PONG"));
            setButtonState(sheungButton, decisions.contains("SHEUNG"));
            setButtonState(kongButton, decisions.contains("BRIGHT_KONG") || decisions.contains("DARK_KONG"));
            setButtonState(winButton, decisions.contains("WIN"));
            setButtonState(passButton, !decisions.isEmpty());
        }
    }

    private void setButtonState(Button btn, boolean enabled) {
        btn.setVisible(enabled);
        btn.setManaged(enabled);
    }

    private void onSouthHandTileClicked(TileNode clicked) {
        if (!allowDiscard) {
            System.out.println("[GameController] allowDiscard is false");
            return;
        }

        if (clicked.getLastClickCount() == 2) {
            if (AppState.getGameSocketClient() != null) {
                AppState.getGameSocketClient().sendDiscardResponse(clicked.getTileName());
                disableDiscard();
            }
            selectedTile = null;
            return;
        }

        if (selectedTile != null && selectedTile != clicked) {
            selectedTile.clearSelection();
        }
        selectedTile = clicked;
        selectedTile.setSelected(true);
    }

    private void handleClaim(String decision) {
        if (AppState.getGameSocketClient() != null) {
            if (decisionDraw) {
                AppState.getGameSocketClient().sendDrawDecision(decision);
            } else {
                if (!decision.equals("SHEUNG")) {
                    AppState.getGameSocketClient().sendDiscardClaim(decision, List.of());
                } else if (sheungCombos.size() == 1) {
                    AppState.getGameSocketClient().sendDiscardClaim(decision, sheungCombos.getFirst());
                } else {
                    // TODO: Show another prompt for picking sheung combo
                    showSheungSelector();
                }
            }
        }

        availableDecisions = List.of();

        Platform.runLater(() -> {
            // hide all buttons
            updateDecisionButtons(List.of());
        });
    }

    // ================== SHEUNG SELECTION ==================

    private void showSheungSelector() {
        sheungOptions.getChildren().clear();

        for (List<String> combo : sheungCombos) {
            List<String> newCombo = new ArrayList<>(combo);
            newCombo.add(lastDiscardedTile);
            Collections.sort(newCombo);

            HBox row = new HBox(5);
            row.setAlignment(Pos.CENTER);

            for (String tileName : newCombo) {
                TileNode tile = new TileNode(tileName);
                tile.setClickable(false);
                tile.setPrefSize(32, 50);
                row.getChildren().add(tile);
            }

            row.setOnMouseClicked(e -> {
                AppState.getGameSocketClient().sendDiscardClaim("SHEUNG", combo);
                hideSheungSelector(false);
            });

            sheungOptions.getChildren().add(row);

            // add spacer after each combo
            Region spacer = new Region();
            spacer.setPrefWidth(16);
            sheungOptions.getChildren().add(spacer);
        }

        sheungSelector.setVisible(true);
        sheungSelector.setManaged(true);
    }

    private void hideSheungSelector(boolean showDecisionButtons) {
        sheungSelector.setVisible(false);
        sheungSelector.setManaged(false);
        sheungOptions.getChildren().clear();

        if (showDecisionButtons) {
            updateDecisionButtons(availableDecisions);
        } else {
            updateDecisionButtons(List.of());
        }
    }

    // ================== END GAME & DECISION ==================

    private void onEndGameDecisionPrompt() {
        Platform.runLater(() -> {
            updateDecisionBox(false);
        });
    }

    private void updateDecisionBox(boolean gameOngoing) {
        actionButtonBox.setVisible(gameOngoing);
        actionButtonBox.setManaged(gameOngoing);

        endGameDecisionBox.setVisible(!gameOngoing);
        endGameDecisionBox.setManaged(!gameOngoing);
    }

    private void sendEndGameDecision(String decision) {
        if (AppState.getGameSocketClient() != null) {
            AppState.getGameSocketClient().sendEndGameDecision(decision);
        }

        if (decision.equals("EXIT")) {
            Platform.runLater(() -> {
                AppNavigator.switchTo("room.fxml");
            });
        }
    }
}