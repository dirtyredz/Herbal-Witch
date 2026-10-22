package io.github.dirtyredz.herbalwitch.magic.ritual.circle;

import io.github.dirtyredz.herbalwitch.magic.ritual.altar.AltarBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.CircleCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Whether a complete ritual circle surrounds an altar: every one of its 16 cells, at the place the
 * altar's position and facing dictate, is a circle cell with the right index and the same facing.
 * Never loads a chunk: a cell in an unloaded chunk is reported as unloaded (a running ritual pauses),
 * a wrong or missing one as missing (a running ritual ends).
 */
public final class CircleValidation {
    /** What the circle around an altar looks like now. */
    public record Report(List<Integer> missing, List<Integer> unloaded) {
        public Report {
            missing = List.copyOf(missing);
            unloaded = List.copyOf(unloaded);
        }

        public boolean complete() {
            return missing.isEmpty() && unloaded.isEmpty();
        }
    }

    private CircleValidation() {
    }

    /** The circle around the altar whose origin is {@code altarOrigin} (its facing is read from the altar). */
    public static Report around(Level level, BlockPos altarOrigin) {
        BlockState altar = level.isLoaded(altarOrigin) ? level.getBlockState(altarOrigin) : null;
        if (altar == null || !(altar.getBlock() instanceof AltarBlock)) {
            return new Report(allCells(), List.of());
        }
        return check(level, CircleLayout.circleOrigin(altarOrigin, altar.getValue(AltarBlock.FACING)), altar.getValue(AltarBlock.FACING));
    }

    /** The circle whose origin is {@code circleOrigin}, facing {@code facing}. */
    public static Report check(Level level, BlockPos circleOrigin, Direction facing) {
        List<Integer> missing = new ArrayList<>();
        List<Integer> unloaded = new ArrayList<>();
        List<BlockPos> cells = CircleLayout.cells(circleOrigin, facing);
        for (int i = 0; i < cells.size(); i++) {
            BlockPos pos = cells.get(i);
            if (!level.isLoaded(pos)) {
                unloaded.add(i);
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof RitualCircleBlock) || RitualCircleBlock.cell(state) != i
                    || state.getValue(RitualCircleBlock.FACING) != facing) {
                missing.add(i);
            }
        }
        return new Report(missing, unloaded);
    }

    private static List<Integer> allCells() {
        List<Integer> all = new ArrayList<>();
        for (int i = 0; i < CircleCatalog.LAYOUT.size(); i++) {
            all.add(i);
        }
        return all;
    }
}
