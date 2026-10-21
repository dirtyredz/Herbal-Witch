package io.github.dirtyredz.herbalwitch.botany.garden;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.Map;

/**
 * Which sides of a herb bed join a neighbouring bed: the same bed block directly beside it at the
 * same height. Joined sides drop their wooden rail, so adjacent beds read as one planter; corners
 * follow from the two sides that meet there (no diagonal state). Reads loaded neighbours only and
 * changes nothing else in the state.
 */
public final class GardenBedConnections {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    private static final Map<Direction, BooleanProperty> BY_SIDE = Map.of(Direction.NORTH, NORTH, Direction.EAST, EAST,
            Direction.SOUTH, SOUTH, Direction.WEST, WEST);

    private GardenBedConnections() {
    }

    /** The connection property of a horizontal side, or null for up/down. */
    public static BooleanProperty property(Direction side) {
        return BY_SIDE.get(side);
    }

    /** Whether {@code neighbour} joins a bed of {@code bed}'s block. */
    public static boolean joins(BlockState bed, BlockState neighbour) {
        return neighbour.is(bed.getBlock());
    }

    /** {@code state} with every side recomputed from loaded neighbours; unloaded sides keep their value. */
    @SuppressWarnings("deprecation") // hasChunkAt is the LevelReader loaded check
    public static BlockState connect(BlockState state, LevelReader level, BlockPos pos) {
        BlockState out = state;
        for (Map.Entry<Direction, BooleanProperty> side : BY_SIDE.entrySet()) {
            BlockPos next = pos.relative(side.getKey());
            if (level.hasChunkAt(next)) {
                out = out.setValue(side.getValue(), joins(state, level.getBlockState(next)));
            }
        }
        return out;
    }

    /** Turns the connections with the block (structure rotation). */
    public static BlockState rotate(BlockState state, Rotation rotation) {
        BlockState out = state;
        for (Map.Entry<Direction, BooleanProperty> side : BY_SIDE.entrySet()) {
            out = out.setValue(BY_SIDE.get(rotation.rotate(side.getKey())), state.getValue(side.getValue()));
        }
        return out;
    }

    /** Mirrors the connections (structure mirroring). */
    public static BlockState mirror(BlockState state, Mirror mirror) {
        BlockState out = state;
        for (Map.Entry<Direction, BooleanProperty> side : BY_SIDE.entrySet()) {
            out = out.setValue(BY_SIDE.get(mirror.mirror(side.getKey())), state.getValue(side.getValue()));
        }
        return out;
    }
}
