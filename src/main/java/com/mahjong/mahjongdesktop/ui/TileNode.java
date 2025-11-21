package com.mahjong.mahjongdesktop.ui;

import java.util.function.Consumer;

import com.mahjong.mahjongdesktop.domain.Tile;

import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * A reusable tile component that represents a single mahjong tile.
 * Provides state management (selected, face-up/down), styling, and event handling.
 */
public class TileNode extends StackPane {

    private final String tileName;
    private boolean selected;
    private boolean faceUp;
    private boolean clickable;
    private Label tileLabel;
    private Consumer<TileNode> onTileClicked;
    private int lastClickCount = 0;

    /**
     * Create a tile node.
     * @param tileName the tile name (e.g., "CIRCLE_5") or null for unknown
     */
    public TileNode(String tileName) {
        this.tileName = tileName;
        this.selected = false;
        this.faceUp = true;
        this.clickable = false;

        initializeUI();
        attachEventHandlers();
        updateStyle();
    }

    private void initializeUI() {
        tileLabel = new Label(getFaceUpDisplay());
        tileLabel.setStyle("-fx-font-size: 22px; -fx-text-alignment: center; -fx-text-fill: black; -fx-background-color: #dddddd; -fx-padding: 1;");

        this.setStyle("-fx-border-color: #4a6a5a; -fx-border-width: 1; -fx-padding: 0;");
        this.setPrefWidth(40);
        this.setPrefHeight(40);
        this.setMinWidth(40);
        this.setMinHeight(40);
        this.getChildren().add(tileLabel);
    }

    private void attachEventHandlers() {
        // single click -> select/deselect; double click -> discard
        this.setOnMouseClicked(e -> {
            if (clickable) {
                lastClickCount = e.getClickCount();
                if (e.getClickCount() == 2) {
                    // double-click -> trigger discard callback
                    if (onTileClicked != null) {
                        onTileClicked.accept(this);
                    }
                } else if (e.getClickCount() == 1) {
                    // single-click -> toggle selection
                    toggleSelected();
                }
            }
        });

        this.setOnMouseEntered(e -> {
            if (clickable) {
                this.setStyle("-fx-border-color: gold; -fx-border-width: 2; -fx-padding: 0; -fx-effect: dropshadow(three-pass-box, rgba(255,215,0,0.8), 6, 0, 0, 0);");
                this.setScaleX(1.08);
                this.setScaleY(1.08);
            }
        });

        this.setOnMouseExited(e -> {
            if (clickable) {
                this.setScaleX(1.0);
                this.setScaleY(1.0);
                updateStyle();
            }
        });
    }

    private void updateStyle() {
        if (selected) {
            this.setStyle("-fx-border-color: gold; -fx-border-width: 3; -fx-padding: 0; -fx-effect: dropshadow(three-pass-box, rgba(255,215,0,0.6), 8, 0, 0, 0);");
        } else if (clickable) {
            this.setStyle("-fx-border-color: #4a6a5a; -fx-border-width: 1; -fx-padding: 0;");
        } else {
            this.setStyle("-fx-border-color: #4a6a5a; -fx-border-width: 1; -fx-padding: 0;");
        }
    }

    private String getFaceUpDisplay() {
        if (!faceUp) return "🀫"; // tile back
        if (tileName == null) return "?";
        try {
            return Tile.valueOf(tileName).getUnicodeSymbol();
        } catch (IllegalArgumentException e) {
            return "?";
        }
    }

    public void toggleSelected() {
        setSelected(!selected);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        updateStyle();
    }

    public void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
        tileLabel.setText(getFaceUpDisplay());
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
        this.setCursor(clickable ? Cursor.HAND : Cursor.DEFAULT);
        updateStyle();
    }

    public void setOnTileClicked(Consumer<TileNode> callback) {
        this.onTileClicked = callback;
    }

    public String getTileName() {
        return tileName;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isClickable() {
        return clickable;
    }

    public void clearSelection() {
        setSelected(false);
    }

    public int getLastClickCount() {
        return lastClickCount;
    }
}
