package io.github.dirtyredz.herbalwitch.platform.permission.entity;

import net.minecraftforge.fml.ModList;

import java.util.List;

/**
 * Land-claim and protection mods. This mod ships no tested adapter for any of them yet, so when a
 * recognised one is loaded the custom entity actions it could protect (harmful preparations on
 * non-hostile creatures and players, ward protection owned by an absent principal) are refused with
 * a reason instead of guessed at. Forge's block-placement events say nothing about entity effects,
 * so they are never taken as permission here.
 */
public final class ProtectionAdapters {
    /** Protection systems recognised as present but unsupported (mod ids). */
    public static final List<String> RECOGNISED = List.of("ftbchunks", "openpartiesandclaims", "cadmus", "flan", "griefdefender",
            "minecolonies");

    private static volatile List<String> loaded;

    private ProtectionAdapters() {
    }

    /** The recognised protection mods that are loaded (computed once). */
    public static List<String> unsupported() {
        List<String> l = loaded;
        if (l == null) {
            ModList mods = ModList.get();
            l = mods == null ? List.of() : RECOGNISED.stream().filter(mods::isLoaded).toList();
            loaded = l;
        }
        return l;
    }

    /** Whether an unsupported protection system is present. */
    public static boolean restricted() {
        return !unsupported().isEmpty();
    }

    /** Test seam: pretend these protection mods are (or none is) loaded. */
    public static void overrideForTest(List<String> present) {
        loaded = present == null ? null : List.copyOf(present);
    }
}
