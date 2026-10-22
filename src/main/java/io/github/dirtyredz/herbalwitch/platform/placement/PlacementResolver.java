package io.github.dirtyredz.herbalwitch.platform.placement;

import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure placement geometry: from a targeted cell and the origin state a block chose, the one plan of
 * cell positions and states. No world access, so the client preview and the server placement
 * resolve identically and the arithmetic is unit-tested. The anchor cell lands on the target; the
 * other cells follow the layout turned to the origin state's facing. Nothing is ever relocated.
 */
public final class PlacementResolver {
    private PlacementResolver() {
    }

    public static PlacementPlan resolve(BlockPos target, BlockState originState, StructurePlacement structure) {
        MultiblockLayout layout = structure.placementLayout();
        Direction facing = structure.layoutFacing(originState);
        if (layout == null) {
            return new PlacementPlan(target, facing, List.of(new PlacementPlan.Cell(0, target, originState)));
        }
        BlockPos origin = layout.origin(target, facing, structure.anchorCell(originState));
        List<PlacementPlan.Cell> cells = new ArrayList<>();
        for (int i = 0; i < layout.size(); i++) {
            cells.add(new PlacementPlan.Cell(i, layout.cellPos(origin, facing, i), structure.cellState(originState, i)));
        }
        return new PlacementPlan(origin, facing, cells);
    }
}
