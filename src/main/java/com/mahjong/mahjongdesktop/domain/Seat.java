package com.mahjong.mahjongdesktop.domain;

/**
 * Represents the four seats in a mahjong game.
 */
public enum Seat {
    EAST,
    SOUTH,
    WEST,
    NORTH;

    /**
     * Returns the next seat in clockwise order.
     */
    public Seat next() {
        return values()[(this.ordinal() + 1) % values().length];
    }

    /**
     * Returns the seat opposite to this one.
     */
    public Seat opposite() {
        return values()[(this.ordinal() + 2) % values().length];
    }

    /**
     * Returns the previous seat in clockwise order.
     */
    public Seat previous() {
        return values()[(this.ordinal() + 3) % values().length];
    }
}
