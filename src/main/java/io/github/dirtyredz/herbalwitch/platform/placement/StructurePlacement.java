package io.github.dirtyredz.herbalwitch.platform.placement;

import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * What a block contributes to checked structure placement: its cells, how the cells are oriented by
 * the origin state, which cell lands on the targeted position, each cell's state, and the support a
 * cell needs. Implemented by blocks; the resolver, checks and commit never know which block it is.
 */
public interface StructurePlacement {
    /** The cells, or null for a one-block piece. */
    MultiblockLayout placementLayout();

    /** The front the layout is turned to for this origin state (the viewer's right and back follow from it). */
    Direction layoutFacing(BlockState originState);

    /** The cell placed at the targeted position (the origin for floor and wall pieces, a top cell for ceiling pieces). */
    default int anchorCell(BlockState originState) {
        return 0;
    }

    /** The state of cell {@code index} of a structure whose origin has {@code originState}. */
    BlockState cellState(BlockState originState, int index);

    /**
     * Whether the cell at {@code pos} with {@code cellState} has the support it needs. Implementations
     * check {@code level.isLoaded} before reading a neighbour and never load a chunk: an unloaded
     * supporting neighbour counts as no support, so placement against it is refused.
     */
    boolean supported(LevelReader level, BlockPos pos, BlockState cellState);
}
