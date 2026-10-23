package io.github.dirtyredz.herbalwitch.companions.familiars.preparation;

import net.minecraft.nbt.CompoundTag;
import java.util.Set;

public final class FamiliarBuffRules {
    public static final int DURATION = 3600, APPLICATION_GATE = 20;
    public static final Set<String> PRODUCTS = Set.of("guardian", "swiftpaw", "nightwatch", "moonveil", "warding", "emberguard", "venomleaf", "spiritbond", "hearthkeeper", "shadowstep");
    private FamiliarBuffRules() {}
    public static boolean active(CompoundTag state, String buff) { return state.getCompound("buffs").getInt(buff) > 0; }
    public static CompoundTag advance(CompoundTag buffs) {
        var next = buffs.copy();
        for (var key : PRODUCTS) {
            int remaining = next.getInt(key);
            if (remaining == 1) next.remove(key); else if (remaining > 1) next.putInt(key, remaining - 1);
        }
        return next;
    }
    public static int cost(CompoundTag state, int wholeEnergy) { return Math.max(100, active(state, "spiritbond") ? wholeEnergy * 80 : wholeEnergy * 100); }
}
