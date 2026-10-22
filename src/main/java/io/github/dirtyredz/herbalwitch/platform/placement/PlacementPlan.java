package io.github.dirtyredz.herbalwitch.platform.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * One resolved placement, immutable: the origin, the facing its cells are laid out by, and every
 * cell's position and state, origin first. The preview draws exactly this and the server places
 * exactly this, or nothing.
 *
 * @param origin the origin cell's position
 * @param facing the layout's front
 * @param cells  every cell, in layout order (origin first)
 */
public record PlacementPlan(BlockPos origin, Direction facing, List<Cell> cells) {
    /** One planned cell. */
    public record Cell(int index, BlockPos pos, BlockState state) {
        public Cell {
            pos = pos.immutable();
        }
    }

    public PlacementPlan {
        origin = origin.immutable();
        cells = List.copyOf(cells);
    }

    public BlockState originState() {
        return cells.get(0).state();
    }

    public List<BlockPos> positions() {
        return cells.stream().map(Cell::pos).toList();
    }
}
