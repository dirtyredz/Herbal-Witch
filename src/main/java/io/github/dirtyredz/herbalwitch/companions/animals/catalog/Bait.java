package io.github.dirtyredz.herbalwitch.companions.animals.catalog;

import java.util.Locale;

/** Stable bait identities; foods and species membership are datapack tags. */
public enum Bait {
    GRAIN, ROOT, SEED, FISH, MEAT, BERRY;

    public String id() { return name().toLowerCase(Locale.ROOT); }
    public String itemPath() { return "animals/bait/" + id(); }
}
