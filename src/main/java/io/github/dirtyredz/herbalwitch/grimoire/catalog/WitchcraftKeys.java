package io.github.dirtyredz.herbalwitch.grimoire.catalog;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import java.util.List;

/** Permanent witchcraft discovery identities, independent of the pages that describe them. */
public final class WitchcraftKeys {
    public static final String PREFIX = "grimoire." + HerbalWitch.MOD_ID + ".witchcraft.";
    public static final List<String> CONVERSION_PAGES = List.of("status", "energy", "recovery");

    public static final List<String> PREPARATION_PAGES = List.of("brewing", "preparations", "familiar");

    private WitchcraftKeys() { }

    public static EntryKey key(String page) {
        return EntryKey.of(EntryKey.Kind.GUIDE, HerbalWitch.MOD_ID + ":witchcraft/" + page);
    }
}
