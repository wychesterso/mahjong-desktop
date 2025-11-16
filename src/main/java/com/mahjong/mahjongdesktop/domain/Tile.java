package com.mahjong.mahjongdesktop.domain;

/**
 * Represents all mahjong tiles with their unicode symbols.
 */
public enum Tile {
    // Circles (dots)
    CIRCLE_1("🀙"),
    CIRCLE_2("🀚"),
    CIRCLE_3("🀛"),
    CIRCLE_4("🀜"),
    CIRCLE_5("🀝"),
    CIRCLE_6("🀞"),
    CIRCLE_7("🀟"),
    CIRCLE_8("🀠"),
    CIRCLE_9("🀡"),

    // Bamboos
    BAMBOO_1("🀐"),
    BAMBOO_2("🀑"),
    BAMBOO_3("🀒"),
    BAMBOO_4("🀓"),
    BAMBOO_5("🀔"),
    BAMBOO_6("🀕"),
    BAMBOO_7("🀖"),
    BAMBOO_8("🀗"),
    BAMBOO_9("🀘"),

    // Millions (characters)
    MILLION_1("🀇"),
    MILLION_2("🀈"),
    MILLION_3("🀉"),
    MILLION_4("🀊"),
    MILLION_5("🀋"),
    MILLION_6("🀌"),
    MILLION_7("🀍"),
    MILLION_8("🀎"),
    MILLION_9("🀏"),

    // Winds
    EAST("🀀"),
    SOUTH("🀁"),
    WEST("🀂"),
    NORTH("🀃"),

    // Dragons
    RED_DRAGON("🀄"),
    GREEN_DRAGON("🀅"),
    WHITE_DRAGON("🀆"),

    // Seasons
    FLOWER_SPRING("🀦"),
    FLOWER_SUMMER("🀧"),
    FLOWER_AUTUMN("🀨"),
    FLOWER_WINTER("🀩"),

    // Plants (bonus tiles)
    FLOWER_PLUM("🀢"),
    FLOWER_ORCHID("🀣"),
    FLOWER_CHRYSANTHEMUM("🀤"),
    FLOWER_BAMBOO("🀥");

    private final String unicode;

    Tile(String unicode) {
        this.unicode = unicode;
    }

    /**
     * Returns the unicode symbol for this tile.
     */
    public String getUnicodeSymbol() {
        return unicode;
    }

    /**
     * Gets tile type (CIRCLE, BAMBOO, MILLION, WIND, DRAGON, or FLOWER).
     */
    public String getTileType() {
        if (this.name().startsWith("CIRCLE")) return "CIRCLE";
        if (this.name().startsWith("BAMBOO")) return "BAMBOO";
        if (this.name().startsWith("MILLION")) return "MILLION";
        if (this.name().startsWith("FLOWER") || this.name().startsWith("SEASON") || this.name().startsWith("PLANT")) return "FLOWER";
        if (this == EAST || this == SOUTH || this == WEST || this == NORTH) return "WIND";
        return "DRAGON";
    }

    /**
     * Parse a tile from its string representation.
     */
    public static Tile fromString(String value) {
        try {
            return Tile.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown tile: " + value);
        }
    }
}
