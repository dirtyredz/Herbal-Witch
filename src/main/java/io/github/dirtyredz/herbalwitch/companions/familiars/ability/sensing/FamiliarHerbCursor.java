package io.github.dirtyredz.herbalwitch.companions.familiars.ability.sensing;

import net.minecraft.core.BlockPos;

/** Morton order searches near positions first, retaining a cursor instead of materializing a search cube. */
public final class FamiliarHerbCursor {
    private int index;
    public BlockPos next(BlockPos centre) {
        int value = index++;
        if (index >= 1 << 21) index = 0;
        return centre.offset(coordinate(value, 0), coordinate(value, 1), coordinate(value, 2));
    }
    private static int coordinate(int value, int axis) {
        int result = 0; for (int bit = 0; bit < 7; bit++) result |= ((value >>> (bit * 3 + axis)) & 1) << bit;
        return (result & 1) == 0 ? -(result / 2) : (result + 1) / 2;
    }
}
