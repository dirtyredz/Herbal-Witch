package io.github.dirtyredz.herbalwitch.botany.garden;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;

/**
 * Farmland-style moisture for herb beds: water within four blocks horizontally (same level or one
 * above), or rain on the bed, keeps it moist; otherwise it dries one step per random tick. It never
 * reverts to dirt and is never trampled.
 */
public final class GardenSoil {
    public static final int MAX_MOISTURE = 7;

    private GardenSoil() {
    }

    /** Water nearby (the vanilla farmland box) or rain falling on the bed. */
    public static boolean isWatered(LevelReader level, BlockPos bed, boolean rainingOnTop) {
        if (rainingOnTop) {
            return true;
        }
        for (BlockPos pos : BlockPos.betweenClosed(bed.offset(-4, 0, -4), bed.offset(4, 1, 4))) {
            if (level.getFluidState(pos).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    /** Moisture after one random tick. */
    public static int nextMoisture(int current, boolean watered) {
        return watered ? MAX_MOISTURE : Math.max(0, current - 1);
    }
}
