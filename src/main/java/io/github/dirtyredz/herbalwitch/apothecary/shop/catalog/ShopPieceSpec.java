package io.github.dirtyredz.herbalwitch.apothecary.shop.catalog;

import java.util.Map;

/**
 * One commerce piece, registry-free: its permanent key, how it mounts, how many offers it shows and
 * its shapeless crafting ingredients (vanilla item paths and counts, in recipe order).
 *
 * @param key          permanent key; the registry path is {@code shop/<key>}
 * @param name         English name
 * @param mount        what holds the piece up
 * @param displaySlots offers a display shows (0 for the register and board)
 * @param ingredients  vanilla item path to count, in insertion order
 */
public record ShopPieceSpec(String key, String name, Mount mount, int displaySlots, Map<String, Integer> ingredients) {
    /** How a piece is supported. */
    public enum Mount {
        /** On top of the apothecary counter only. */
        COUNTER_TOP,
        /** Against a sturdy wall behind it. */
        WALL,
        /** On any sturdy surface below it (a counter top included). */
        SURFACE
    }

    public ShopPieceSpec {
        if (!key.matches("[a-z_]+") || displaySlots < 0 || displaySlots > 3 || ingredients.isEmpty()) {
            throw new IllegalArgumentException("invalid shop piece " + key);
        }
        ingredients = java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(ingredients));
    }

    /** {@code shop/<key>}: block, item, blockstate, loot and recipe path. */
    public String path() {
        return "shop/" + key;
    }

    public boolean display() {
        return displaySlots > 0;
    }
}
