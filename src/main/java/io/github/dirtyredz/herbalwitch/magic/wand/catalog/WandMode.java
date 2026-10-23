package io.github.dirtyredz.herbalwitch.magic.wand.catalog;

import java.util.Arrays;
import java.util.Optional;

/** Permanent saved selector IDs; bounded enum ordinals are used only by the versioned network protocol. */
public enum WandMode {
    HARVEST("harvest"), PLANT("plant"), WATER("water"), COLLECT("collect"), LIGHT("light"), FOCUS("focus"), FAMILIAR("familiar");

    private final String id;
    WandMode(String id) { this.id = id; }
    public String id() { return id; }
    public static Optional<WandMode> parse(String id) {
        return Arrays.stream(values()).filter(mode -> mode.id.equals(id)).findFirst();
    }
}
