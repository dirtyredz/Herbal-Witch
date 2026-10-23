package io.github.dirtyredz.herbalwitch.companions.familiars.command;

import java.util.Locale;

public enum FamiliarCommand {
    FOLLOW, STAY, GUARD_WITCH, PROTECT_HOME, SCOUT, PASSIVE, RETURN_HOME;
    public String key() { return name().toLowerCase(Locale.ROOT); }
    public boolean mobile() { return this == FOLLOW || this == GUARD_WITCH || this == SCOUT; }
    public static FamiliarCommand parse(String value) {
        for (var command : values()) if (command.key().equals(value)) return command;
        throw new IllegalArgumentException("Unknown familiar command: " + value);
    }
}
