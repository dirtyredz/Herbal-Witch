package io.github.dirtyredz.herbalwitch.apothecary.botany.garden;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.Map;

/**
 * Which sides of a herb bed join a neighbouring bed: the same bed block directly beside it at the
 * same height. Joined sides drop their wooden rail, so adjacent beds read as one planter. A corner
 * whose two sides are both joined but whose diagonal neighbour is missing is an inner corner (the
 * inside of an L, U or ring) and carries its own flag so the model can close it with rail. Reads
 * loaded neighbours only and changes nothing else in the state.
 */
public final class GardenBedConnections {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    private static final Map<Direction, BooleanProperty> BY_SIDE = Map.of(Direction.NORTH, NORTH, Direction.EAST, EAST,
            Direction.SOUTH, SOUTH, Direction.WEST, WEST);

    /** Sides clockwise from north; corner {@code i} sits between side {@code i} and side {@code i + 1}. */
    private static final Direction[] ORDER = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    private static final BooleanProperty[] INNER = {
            BooleanProperty.create("inner_north_east"), BooleanProperty.create("inner_east_south"),
            BooleanProperty.create("inner_south_west"), BooleanProperty.create("inner_west_north")};

    private GardenBedConnections() {
    }

    /** The connection property of a horizontal side, or null for up/down. */
    public static BooleanProperty property(Direction side) {
        return BY_SIDE.get(side);
    }

    /** The inner-corner flag of the corner clockwise after side {@code i} of north, east, south, west (0 = north-east). */
    public static BooleanProperty innerCorner(int i) {
        return INNER[i];
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
        return refreshCorners(out, level, pos);
    }

    /**
     * {@code state} with each inner-corner flag recomputed: set where both sides of the corner are
     * joined and the diagonal bed is missing, clear otherwise. An unloaded diagonal keeps its value.
     */
    @SuppressWarnings("deprecation") // hasChunkAt is the LevelReader loaded check
    public static BlockState refreshCorners(BlockState state, LevelReader level, BlockPos pos) {
        BlockState out = state;
        for (int i = 0; i < 4; i++) {
            Direction a = ORDER[i];
            Direction b = ORDER[(i + 1) % 4];
            if (!state.getValue(BY_SIDE.get(a)) || !state.getValue(BY_SIDE.get(b))) {
                out = out.setValue(INNER[i], false);
                continue;
            }
            BlockPos diagonal = pos.relative(a).relative(b);
            if (level.hasChunkAt(diagonal)) {
                out = out.setValue(INNER[i], !joins(state, level.getBlockState(diagonal)));
            }
        }
        return out;
    }

    /**
     * A bed was placed or removed at {@code pos}: beds on its four diagonals may gain or lose an
     * inner corner, and no neighbour update reaches them, so refresh them here.
     */
    public static void refreshDiagonals(Level level, BlockPos pos) {
        for (Direction a : new Direction[] {Direction.NORTH, Direction.SOUTH}) {
            for (Direction b : new Direction[] {Direction.EAST, Direction.WEST}) {
                BlockPos at = pos.relative(a).relative(b);
                BlockState bed = level.getBlockState(at);
                if (bed.getBlock() instanceof HerbBedBlock) {
                    BlockState refreshed = refreshCorners(bed, level, at);
                    if (refreshed != bed) {
                        level.setBlock(at, refreshed, Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    /** The corner index whose two sides are exactly {@code a} and {@code b}. */
    private static int cornerOf(Direction a, Direction b) {
        for (int i = 0; i < 4; i++) {
            Direction x = ORDER[i];
            Direction y = ORDER[(i + 1) % 4];
            if ((x == a && y == b) || (x == b && y == a)) {
                return i;
            }
        }
        throw new IllegalArgumentException(a + " and " + b + " are not adjacent sides");
    }

    /** Turns the connections with the block (structure rotation). */
    public static BlockState rotate(BlockState state, Rotation rotation) {
        BlockState out = state;
        for (Map.Entry<Direction, BooleanProperty> side : BY_SIDE.entrySet()) {
            out = out.setValue(BY_SIDE.get(rotation.rotate(side.getKey())), state.getValue(side.getValue()));
        }
        for (int i = 0; i < 4; i++) {
            out = out.setValue(INNER[cornerOf(rotation.rotate(ORDER[i]), rotation.rotate(ORDER[(i + 1) % 4]))], state.getValue(INNER[i]));
        }
        return out;
    }

    /** Mirrors the connections (structure mirroring). */
    public static BlockState mirror(BlockState state, Mirror mirror) {
        BlockState out = state;
        for (Map.Entry<Direction, BooleanProperty> side : BY_SIDE.entrySet()) {
            out = out.setValue(BY_SIDE.get(mirror.mirror(side.getKey())), state.getValue(side.getValue()));
        }
        for (int i = 0; i < 4; i++) {
            out = out.setValue(INNER[cornerOf(mirror.mirror(ORDER[i]), mirror.mirror(ORDER[(i + 1) % 4]))], state.getValue(INNER[i]));
        }
        return out;
    }
}
