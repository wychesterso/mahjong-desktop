package com.mahjong.mahjongdesktop.controllers;

import com.mahjong.mahjongdesktop.AppNavigator;
import com.mahjong.mahjongdesktop.AppState;
import com.mahjong.mahjongdesktop.domain.Tile;
import com.mahjong.mahjongdesktop.dto.prompt.DecisionOnDiscardPromptDTO;
import com.mahjong.mahjongdesktop.dto.prompt.DecisionOnDrawPromptDTO;
import com.mahjong.mahjongdesktop.dto.prompt.DiscardAfterDrawPromptDTO;
import com.mahjong.mahjongdesktop.dto.prompt.DiscardPromptDTO;
import com.mahjong.mahjongdesktop.dto.state.EndGameDTO;
import com.mahjong.mahjongdesktop.dto.state.GameStateDTO;
import com.mahjong.mahjongdesktop.dto.state.HandDTO;
import com.mahjong.mahjongdesktop.dto.state.ScoringContextDTO;
import com.mahjong.mahjongdesktop.network.GameMessageHandler;
import com.mahjong.mahjongdesktop.ui.TileNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

import java.util.*;
import java.util.function.Consumer;

public class GameController implements CleanupAware {

    // ============ player zones ============
    @FXML private Label northNameLabel, southNameLabel, eastNameLabel, westNameLabel;
    @FXML private Region northTurnIndicator, southTurnIndicator, eastTurnIndicator, westTurnIndicator;

    @FXML private HBox northContainer, northMelds, northHand, northFlowers;
    @FXML private HBox eastMelds, eastHand, eastFlowers;
    @FXML private VBox eastContainer;
    @FXML private HBox westMelds, westHand, westFlowers;
    @FXML private VBox westContainer;
    @FXML private HBox southMelds, southFlowers, southHand, southDrawnTile;
    @FXML private StackPane resultOverlay;
    @FXML private VBox resultContent;

    @FXML private GridPane discardPile;

    // ============ buttons ============
    @FXML private Button pongButton, sheungButton, kongButton, winButton, passButton;

    @FXML private HBox sheungSelector;
    @FXML private HBox sheungOptions;
    @FXML private Button cancelSheungBtn;

    @FXML private HBox nextGameDecisionBox;
    @FXML private Button nextGameButton, exitButton;

    // ============ state ============
    private GameMessageHandler handler;
    private TileNode selectedTile;
    private String selfSeat; // the current player seat

    private boolean allowDiscard = true;
    private boolean decisionDraw = false; // true if prompted for decisionOnDraw, false if prompted for decisionOnDiscard
    private List<String> availableDecisions = List.of();
    private List<List<String>> sheungCombos = List.of();
    private String lastDrawnTile = ""; // using something like this to fix?
    private String lastDiscardedTile = "";

    private final Map<String, Pane> handBoxes = new HashMap<>();
    private final Map<String, HBox> meldBoxes = new HashMap<>();
    private final Map<String, HBox> flowerBoxes = new HashMap<>();
    private final Map<String, Label> nameLabels = new HashMap<>();
    private final Map<String, Region> turnIndicators = new HashMap<>();

    private final Map<String, String> seatToUI = new HashMap<>();
    private Map<String, String> seatToName = null;

    // ============ listener references ============
    private Consumer<Void> startGameListener;
    private Consumer<GameStateDTO> stateListener;
    private Consumer<DecisionOnDrawPromptDTO> decisionOnDrawPromptListener;
    private Consumer<DecisionOnDiscardPromptDTO> decisionOnDiscardPromptListener;
    private Consumer<DiscardPromptDTO> discardPromptListener;
    private Consumer<DiscardAfterDrawPromptDTO> discardAfterDrawListener;
    private Consumer<EndGameDTO> endGameListener;
    private Consumer<Void> endGameDecisionListener;

    private boolean registeredWithHandlers = false;

    @FXML
    public void initialize() {
        // map seats to UI components (fixed)
        mapSeatsToUI();
        enforceFixedBoxSizes();
//        rotateSideHands();

        // subscribe to game state updates
        handler = AppState.getGameMessageHandler();
        if (handler != null && !registeredWithHandlers) {
            registeredWithHandlers = true;

            startGameListener = v -> onStartGame();
            stateListener = this::onGameState;
            decisionOnDrawPromptListener = this::onDecisionOnDrawPrompt;
            decisionOnDiscardPromptListener = this::onDecisionOnDiscardPrompt;
            discardPromptListener = this::onDiscardPrompt;
            discardAfterDrawListener = this::onDiscardAfterDrawPrompt;
            endGameListener = this::onGameEnd;
            endGameDecisionListener = v -> onEndGameDecisionPrompt();

            handler.addStartGameListener(startGameListener);
            handler.addStateListener(stateListener);
            handler.addDecisionOnDrawPromptListener(decisionOnDrawPromptListener);
            handler.addDecisionOnDiscardPromptListener(decisionOnDiscardPromptListener);
            handler.addDiscardPromptListener(discardPromptListener);
            handler.addDiscardAfterDrawPromptListener(discardAfterDrawListener);
            handler.addEndGameListener(endGameListener);
            handler.addEndGameDecisionPromptListener(endGameDecisionListener);

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
//        lockSideContainerSizes();
    }

    private void lockSideContainerSizes() {
        eastContainer.setPrefSize(50, 450);
        eastContainer.setMinSize(50, 450);
        eastContainer.setMaxSize(50, 450);

        westContainer.setPrefSize(50, 450);
        westContainer.setMinSize(50, 450);
        westContainer.setMaxSize(50, 450);
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
        discardPile.setPrefSize(450, 400);
        discardPile.setMinSize(450, 400);
        discardPile.setMaxSize(450, 400);

        // stop expansion
        westContainer.setMaxWidth(Region.USE_PREF_SIZE);
        eastContainer.setMaxWidth(Region.USE_PREF_SIZE);

        HBox.setHgrow(westContainer, Priority.NEVER);
        HBox.setHgrow(eastContainer, Priority.NEVER);
    }

    // ================== GAME STATE UPDATES ==================

    private void onStartGame() {
        Platform.runLater(() -> {
            toggleGameEndOverlay(false);
        });
    }

    private void onGameState(GameStateDTO state) {
        if (state == null) return;

        if (selfSeat == null && state.getTable() != null && state.getTable().getSelfSeat() != null) {
            selfSeat = state.getTable().getSelfSeat();
            mapServerSeatsToUI(selfSeat);
        }

        Platform.runLater(() -> {
            updatePlayerNames(state);
            updateTurnIndicators(state.getCurrentTurn());

            if (state.getTable() != null && state.getTable().getHands() != null) {
                updateAllHands(state);
                updateMelds(state);
            }

            if (state.getTable() != null && state.getTable().getDiscardPile() != null) {
                updateCenterDiscardPile(state.getTable().getDiscardPile());
            }

            hideSheungSelector(false);
        });
    }

    private void updatePlayerNames(GameStateDTO state) {
        if (state.getPlayerNames() == null) return;
        seatToName = state.getPlayerNames();

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
                if (hand.getDarkKongs().isEmpty()) {
                    // generate face down dark kongs for opponents
                    for (int i = 0; i < hand.getDarkKongCount(); i++) {
                        addDarkMeld(meldBox, 4);
                    }
                } else {
                    // generate own dark kongs
                    for (List<String> kong : hand.getDarkKongs()) {
                        addMeld(kong, meldBox);
                    }
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

    private void addDarkMeld(HBox meldBox, int numTiles) {
        HBox box = new HBox(2);
        for (int i = 0; i < numTiles; i++) {
            TileNode t = new TileNode(null);
            t.setClickable(false);
            t.setFaceUp(false);
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

    // ================== END GAME DATA ==================

    private void onGameEnd(EndGameDTO data) {
        Platform.runLater(() -> {
            if (data == null) return;

            resultContent.getChildren().clear();

            // header for draw
            if ("DRAW".equalsIgnoreCase(data.getResult())) {
                Label title = new Label();
                title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 26px; -fx-font-weight: bold;");
                title.setText("Draw!");
                resultContent.getChildren().add(title);
            }

            if ("WIN".equalsIgnoreCase(data.getResult()) && data.getWinners() != null && !data.getWinners().isEmpty()) {
                // display winners
                for (Map.Entry<String, ScoringContextDTO> entry : data.getWinners().entrySet()) {
                    String seat = entry.getKey();
                    ScoringContextDTO ctx = entry.getValue();

                    VBox winnerBox = buildWinnerBox(seat, ctx);
                    resultContent.getChildren().add(winnerBox);
                }

                // loser list
                if (data.getLoserSeats() != null) {
                    Label loserTitle = new Label("Loser(s):");
                    loserTitle.setStyle("-fx-text-fill: #ffaaaa; -fx-font-size: 16px; -fx-font-weight: bold;");
                    resultContent.getChildren().add(loserTitle);

                    List<String> losers;
                    if (data.getLoserSeats().isEmpty()) {
                        // self draw, every non-winner is loser
                        losers = new ArrayList<>();
                        for (Map.Entry<String, String> entry : seatToName.entrySet()) {
                            if (!data.getWinners().containsKey(entry.getKey())) {
                                losers.add(entry.getValue());
                            }
                        }
                    } else {
                        // lose on discard
                        losers = data.getLoserSeats().stream().map(s -> seatToName.get(s)).toList();
                    }

                    Label loserList = new Label(String.join(", ", losers)); // string these losers together
                    loserList.setStyle("-fx-text-fill: #ffcccc; -fx-font-size: 14px;");
                    resultContent.getChildren().add(loserList);
                }
            }

            // show overlay
            toggleGameEndOverlay(true);
        });
    }

    private VBox buildWinnerBox(String seat, ScoringContextDTO ctx) {
        VBox box = new VBox(6);
        box.setStyle("-fx-border-color: #666; -fx-border-width: 2; -fx-padding: 12; -fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 8;");
        box.setAlignment(Pos.CENTER);

        Label seatLabel = new Label("Winner: " + seat);
        seatLabel.setStyle("-fx-text-fill: #ffffaa; -fx-font-size: 18px; -fx-font-weight: bold;");
        box.getChildren().add(seatLabel);

        HBox hand = new HBox(0);
        hand.setAlignment(Pos.CENTER);

        // flowers
        if (ctx.getFlowers() != null && !ctx.getFlowers().isEmpty()) {
            HBox flowerBox = new HBox(2);
            for (String flower : ctx.getFlowers()) {
                TileNode tile = new TileNode(flower);
                tile.setClickable(false);
                enforceTileFixedSize(tile);
                flowerBox.getChildren().add(tile);
            }
            hand.getChildren().add(flowerBox);

            addSpacer(hand, 16);
            addSeparator(hand);
            addSpacer(hand, 16);
        }

        // revealed groups
        if (ctx.getRevealedGroups() != null && !ctx.getRevealedGroups().isEmpty()) {
            HBox revealedGroups = new HBox(4);
            for (List<String> group : ctx.getRevealedGroups()) {
                addMeld(group, revealedGroups);
            }
            hand.getChildren().add(revealedGroups);

            addSeparator(hand);
            addSpacer(hand, 16);
        }

        // concealed groups
        boolean foundWinningGroup = false;

        if (ctx.getConcealedGroups() != null && !ctx.getConcealedGroups().isEmpty()) {
            HBox concealedGroups = new HBox(4);

            for (List<String> group : ctx.getConcealedGroups()) {
                if (!foundWinningGroup && group.equals(ctx.getWinningGroup())) {
                    foundWinningGroup = true;
                    boolean foundWinningTile = false;

                    HBox winningGroupBox = new HBox(2);
                    for (String tile : group) {
                        TileNode t = new TileNode(tile);
                        t.setClickable(false);
                        if (!foundWinningTile && tile.equals(ctx.getWinningTile())) {
                            foundWinningTile = true;
                            t.setSelected(true);
                        }
                        enforceTileFixedSize(t);
                        winningGroupBox.getChildren().add(t);
                    }

                    concealedGroups.getChildren().add(winningGroupBox);
                    if (ctx.getConcealedGroups().getLast() != group) addSpacer(concealedGroups, 16);

                } else {
                    addMeld(group, concealedGroups);
                }
            }

            hand.getChildren().add(concealedGroups);
        }

        box.getChildren().add(hand);

        // scoring patterns
        if (ctx.getScoringPatterns() != null && !ctx.getScoringPatterns().isEmpty()) {
            Label patternLabel = new Label("SCORING PATTERNS");
            patternLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-underline: true;");
            box.getChildren().add(patternLabel);

            for (String pattern : ctx.getScoringPatterns()) {
                Label p = new Label("• " + pattern);
                p.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;");
                box.getChildren().add(p);
            }
        }

        return box;
    }

    private void toggleGameEndOverlay(boolean show) {
        resultOverlay.setVisible(show);
        resultOverlay.setManaged(show);
    }

    private void addSpacer(Pane pane, int width) {
        Region spacer = new Region();
        spacer.setPrefWidth(width);
        pane.getChildren().add(spacer);
    }

    private void addSeparator(Pane pane) {
        Separator separator = new Separator();
        separator.setOrientation(Orientation.VERTICAL);
        separator.setPrefHeight(50);
        pane.getChildren().add(separator);
    }

    // ================== END GAME PROMPT ==================

    private void onEndGameDecisionPrompt() {
        Platform.runLater(() -> {
            southDrawnTile.getChildren().clear();
            disableDiscard();

            nextGameDecisionBox.setVisible(true);
            nextGameDecisionBox.setManaged(true);
        });
    }

    private void sendEndGameDecision(String decision) {
        if (AppState.getGameSocketClient() != null) {
            AppState.getGameSocketClient().sendEndGameDecision(decision);
        }

        if (decision.equals("EXIT")) {
            Platform.runLater(() -> {
                AppNavigator.switchTo("room.fxml");
            });
        } else {
            Platform.runLater(() -> {
                nextGameDecisionBox.setVisible(false);
                nextGameDecisionBox.setManaged(false);
            });
        }
    }

    // ================== CLEANUP ==================

    @Override
    public void cleanup() {
        GameMessageHandler handler = AppState.getGameMessageHandler();
        if (handler != null) {
            handler.removeStartGameListener(startGameListener);
            handler.removeStateListener(stateListener);
            handler.removeDecisionOnDrawPromptListener(decisionOnDrawPromptListener);
            handler.removeDecisionOnDiscardPromptListener(decisionOnDiscardPromptListener);
            handler.removeDiscardPromptListener(discardPromptListener);
            handler.removeDiscardAfterDrawPromptListener(discardAfterDrawListener);
            handler.removeEndGameListener(endGameListener);
            handler.removeEndGameDecisionPromptListener(endGameDecisionListener);
        }
    }
}