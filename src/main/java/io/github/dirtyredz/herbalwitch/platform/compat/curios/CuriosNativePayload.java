package io.github.dirtyredz.herbalwitch.platform.compat.curios;

import net.minecraft.nbt.*;
import java.util.Set;

/** Curios 5.14.1 attaches this empty serialized inventory even to animals without any slots.
 * This exact inert shape is safe to retain; populated slots, extensions and malformed tags refuse.
 * No Curios runtime classes are linked, so stored empty payloads remain readable without the mod. */
public final class CuriosNativePayload {
    public static final String ID="curios:inventory";
    private CuriosNativePayload() {}
    public static boolean empty(Tag tag) {
        return tag instanceof CompoundTag value && value.getAllKeys().equals(Set.of("Curios"))
                && value.get("Curios") instanceof ListTag list && list.isEmpty();
    }
}
