package io.github.dirtyredz.herbalwitch.magic.charm.equipment;

import io.github.dirtyredz.herbalwitch.magic.charm.item.ProtectiveCharmItem;
import net.minecraft.world.item.ItemStack;

/** Shared loaded-time arithmetic; callers own their durable partial-second counter. */
public final class CharmCharge {
    public static final int TICKS_PER_SECOND = 20;
    public record Result(int partialTicks, boolean broke) {}
    private CharmCharge() {}
    public static Result wear(ItemStack charm, int partialTicks, int ticks) {
        if (partialTicks < 0 || partialTicks >= TICKS_PER_SECOND || ticks < 0) throw new IllegalArgumentException("Invalid charm clock");
        long counter = (long) partialTicks + ticks;
        boolean broke = false;
        while (counter >= TICKS_PER_SECOND && !broke) {
            counter -= TICKS_PER_SECOND;
            if (ProtectiveCharmItem.spendSecond(charm)) { charm.shrink(1); broke = true; }
        }
        return new Result(broke ? 0 : (int) counter, broke);
    }
}
