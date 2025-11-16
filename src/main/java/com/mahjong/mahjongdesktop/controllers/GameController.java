package com.mahjong.mahjongdesktop.controllers;

import com.mahjong.mahjongdesktop.AppState;
import com.mahjong.mahjongdesktop.dto.state.GameStateDTO;
import com.mahjong.mahjongdesktop.dto.state.HandDTO;
import com.mahjong.mahjongdesktop.network.GameMessageHandler;
import com.mahjong.mahjongdesktop.ui.TileNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameController {

    // ============ root & board ============
    @FXML private GridPane root;
    
    // ============ player zones (abs pos) ============
    @FXML private Label northNameLabel, southNameLabel, eastNameLabel, westNameLabel;
    @FXML private Region northTurnIndicator, southTurnIndicator, eastTurnIndicator, westTurnIndicator;
    
    @FXML private HBox northMelds, northDiscards, northHand, northFlowers;
    @FXML private HBox eastMelds, eastDiscards, eastFlowers;
    @FXML private VBox eastHand;
    @FXML private HBox westMelds, westDiscards, westFlowers;
    @FXML private VBox westHand;
    @FXML private HBox southMelds, southFlowers, southHand;
    
    @FXML private GridPane discardPile;

    // ============ buttons ============
    @FXML private Button pongButton, sheungButton, kongButton, winButton, passButton;

    private GameMessageHandler handler;
    private TileNode selectedTile;
    private String selfSeat; // your own seat
    private final Map<String, Pane> handBoxes = new HashMap<>();
    private final Map<String, HBox> meldBoxes = new HashMap<>();
    private final Map<String, HBox> flowerBoxes = new HashMap<>();
    private final Map<String, Label> nameLabels = new HashMap<>();
    private final Map<String, Region> turnIndicators = new HashMap<>();

    @FXML
    public void initialize() {
        // subscribe to game state updates
        handler = AppState.getGameMessageHandler();
        if (handler != null) {
            handler.addListener(this::onGameState);
            // load initial state if available
            GameStateDTO initial = handler.getLatestState();
            if (initial != null) {
                // capture self seat from initial state and rotate board
                if (initial.getTable() != null) {
                    selfSeat = initial.getTable().getSelfSeat();
                    rotateBoardForSeat(selfSeat);
                }
                onGameState(initial);
            }
        } else {
            System.err.println("GameMessageHandler not present when GameController initialized.");
        }

        // map seats to UI components
        mapSeatsToUI();

        // wire button handlers
        pongButton.setOnAction(e -> handleClaim("PONG"));
        sheungButton.setOnAction(e -> handleClaim("SHEUNG"));
        kongButton.setOnAction(e -> handleClaim("BRIGHT_KONG")); // TODO: differentiate kong types
        winButton.setOnAction(e -> handleClaim("WIN"));
        passButton.setOnAction(e -> handleClaim("PASS"));
    }

    /**
     * Rotate the board so that selfSeat always faces down (bottom).
     * Rotation is applied once at initialization, never changes during game.
     */
    private void rotateBoardForSeat(String seat) {
        if (seat == null || root == null) return;
        
        int rotation = switch (seat) {
            case "SOUTH" -> 0;    // self at bottom, no rotation
            case "EAST" -> 270;   // self at right, rotate left 90° to put you at bottom
            case "NORTH" -> 180;  // self at top, rotate 180°
            case "WEST" -> 90;    // self at left, rotate right 90°
            default -> 0;
        };
        
        root.setRotate(rotation);
        System.out.println("Board rotated " + rotation + "° for seat " + seat);
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

    private void onGameState(GameStateDTO state) {
        if (state == null) return;
        
        // capture selfSeat from state updates if not already set
        if (selfSeat == null && state.getTable() != null && state.getTable().getSelfSeat() != null) {
            selfSeat = state.getTable().getSelfSeat();
            rotateBoardForSeat(selfSeat);
        }

        Platform.runLater(() -> {
            // update player names
            if (state.getPlayerNames() != null) {
                for (String seat : new String[]{"NORTH", "SOUTH", "EAST", "WEST"}) {
                    String name = state.getPlayerNames().get(seat);
                    Label label = nameLabels.get(seat);
                    if (label != null) {
                        label.setText(name == null ? "(vacant)" : name);
                    }
                }
            }

            // highlight current turn
            if (state.getCurrentTurn() != null) {
                updateTurnIndicators(state.getCurrentTurn());
            }

            // update hands and melds
            if (state.getTable() != null && state.getTable().getHands() != null) {
                updateAllHands(state);
                updateMelds(state);
            }

            // update center discard pile
            if (state.getTable() != null && state.getTable().getDiscardPile() != null) {
                updateCenterDiscardPile(state.getTable().getDiscardPile());
            }

            // update available decision buttons
            updateDecisionButtons(state.getAvailableDecisions());

            System.out.println("Game State Updated: Turn=" + state.getCurrentTurn() + 
                             ", Decisions=" + state.getAvailableDecisions());
        });
    }

    private void updateAllHands(GameStateDTO state) {
        for (String seat : new String[]{"NORTH", "SOUTH", "EAST", "WEST"}) {
            HandDTO hand = state.getTable().getHands().get(seat);
            Pane handBox = handBoxes.get(seat);
            HBox flowerBox = flowerBoxes.get(seat);
            boolean isPlayerHand = "SOUTH".equals(seat);
            
            handBox.getChildren().clear();
            flowerBox.getChildren().clear();
            
            // render flowers (separate box)
            for (String flower : hand.getFlowers()) {
                TileNode tile = new TileNode(flower);
                tile.setClickable(false);
                flowerBox.getChildren().add(tile);
            }
            
            // render drawn tile first (for South) or sorted hand
            List<String> displayTiles = new ArrayList<>(hand.getConcealedTiles());
            if (isPlayerHand && state.getDrawnTile() != null) {
                displayTiles.add(0, state.getDrawnTile()); // drawn tile goes to the front
            }
            
            for (String tileName : displayTiles) {
                TileNode tile = new TileNode(tileName);
                if (isPlayerHand) {
                    tile.setClickable(true);
                    tile.setOnTileClicked(clicked -> onSouthHandTileClicked(clicked));
                } else {
                    tile.setFaceUp(false); // show back (🀫) for opponents
                }
                handBox.getChildren().add(tile);
            }
        }
    }

    private void updateMelds(GameStateDTO state) {
        for (String seat : new String[]{"NORTH", "SOUTH", "EAST", "WEST"}) {
            HandDTO hand = state.getTable().getHands().get(seat);
            HBox meldBox = meldBoxes.get(seat);

            if (meldBox == null || hand == null) continue;

            meldBox.getChildren().clear();

            // flowers (leftmost)
            if (hand.getFlowers() != null && !hand.getFlowers().isEmpty()) {
                for (String flower : hand.getFlowers()) {
                    TileNode tile = new TileNode(flower);
                    tile.setClickable(false);
                    meldBox.getChildren().add(tile);
                }
            }

            // sheungs
            if (hand.getSheungs() != null) {
                for (List<String> sheung : hand.getSheungs()) {
                    HBox group = new HBox(2);
                    for (String tile : sheung) {
                        TileNode t = new TileNode(tile);
                        t.setClickable(false);
                        group.getChildren().add(t);
                    }
                    meldBox.getChildren().add(group);
                }
            }

            // pongs
            if (hand.getPongs() != null) {
                for (List<String> pong : hand.getPongs()) {
                    HBox group = new HBox(2);
                    for (String tile : pong) {
                        TileNode t = new TileNode(tile);
                        t.setClickable(false);
                        group.getChildren().add(t);
                    }
                    meldBox.getChildren().add(group);
                }
            }

            // bright kongs
            if (hand.getBrightKongs() != null) {
                for (List<String> kong : hand.getBrightKongs()) {
                    HBox group = new HBox(2);
                    for (String tile : kong) {
                        TileNode t = new TileNode(tile);
                        t.setClickable(false);
                        group.getChildren().add(t);
                    }
                    meldBox.getChildren().add(group);
                }
            }

            // dark kongs (face down for other players)
            if (hand.getDarkKongs() != null && "SOUTH".equals(seat)) {
                for (List<String> kong : hand.getDarkKongs()) {
                    HBox group = new HBox(2);
                    for (String tile : kong) {
                        TileNode t = new TileNode(tile);
                        t.setFaceUp(false); // show back
                        t.setClickable(false);
                        group.getChildren().add(t);
                    }
                    meldBox.getChildren().add(group);
                }
            }
        }
    }

    private void updateCenterDiscardPile(List<String> discards) {
        discardPile.getChildren().clear();
        
        if (discards == null || discards.isEmpty()) return;

        // display in 4x3 grid (up to 13 tiles)
        int row = 0, col = 0;
        for (String tileName : discards) {
            TileNode tile = new TileNode(tileName);
            tile.setClickable(false);
            discardPile.add(tile, col, row);
            
            col++;
            if (col >= 4) {
                col = 0;
                row++;
            }
        }
    }

    private void updateTurnIndicators(String currentTurn) {
        // reset all
        for (Region indicator : turnIndicators.values()) {
            indicator.setStyle("-fx-border-color: transparent; -fx-border-radius: 5; -fx-padding: 4;");
        }
        
        // highlight current
        Region current = turnIndicators.get(currentTurn);
        if (current != null) {
            current.setStyle("-fx-border-color: gold; -fx-border-radius: 5; -fx-padding: 4; -fx-border-width: 2;");
        }
    }

    private void updateDecisionButtons(List<String> decisions) {
        if (decisions == null) {
            pongButton.setVisible(false);
            sheungButton.setVisible(false);
            kongButton.setVisible(false);
            winButton.setVisible(false);
            passButton.setVisible(false);
            return;
        }

        pongButton.setVisible(decisions.contains("PONG"));
        sheungButton.setVisible(decisions.contains("SHEUNG"));
        kongButton.setVisible(decisions.contains("BRIGHT_KONG") || decisions.contains("DARK_KONG"));
        winButton.setVisible(decisions.contains("WIN"));
        passButton.setVisible(decisions.contains("PASS"));
    }

    private void onSouthHandTileClicked(TileNode clicked) {
        if (clicked.getLastClickCount() == 2) {
            // double-click -> discard
            String tileName = clicked.getTileName();
            if (AppState.getGameSocketClient() != null) {
                AppState.getGameSocketClient().sendDiscardResponse(tileName);
            }
            selectedTile = null;
            return;
        }
        
        // single-click -> toggle selection
        if (selectedTile != null && selectedTile != clicked) {
            selectedTile.clearSelection();
        }
        selectedTile = clicked;
        selectedTile.setSelected(true);
    }

    private void handleClaim(String decision) {
        System.out.println("Claiming: " + decision);
        
        if (AppState.getGameSocketClient() != null) {
            if ("SHEUNG".equals(decision)) {
                // TODO: show sheung combo selection dialog
                AppState.getGameSocketClient().sendDrawDecision(decision);
            } else {
                AppState.getGameSocketClient().sendDrawDecision(decision);
            }
        }
    }
}
