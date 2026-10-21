package io.github.dirtyredz.herbalwitch.showcase.construction;

import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlan;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/**
 * Writes a checked plan ({@link ShowcasePreflight} first). Clears the box above the floor without
 * drops or neighbour updates, lays the floor, then places supports, content, block entity samples
 * and signs. Every write is a cell of the plan's box.
 */
public final class ShowcaseBuilder {
    /** Quiet writes for clearing and the floor: clients see them, neighbours are not updated, nothing drops. */
    private static final int QUIET = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    public static final BlockState DEFAULT_FLOOR = Blocks.SMOOTH_STONE.defaultBlockState();

    /** What was written. */
    public record Result(int cleared, int placed) {
    }

    private ShowcaseBuilder() {
    }

    public static Result build(ShowcasePlan plan, ServerLevel level, BlockPos origin) {
        int cleared = 0;
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int dx = 0; dx < plan.width(); dx++) {
            for (int dz = 0; dz < plan.depth(); dz++) {
                for (int dy = ShowcasePlan.HEIGHT - 1; dy >= 1; dy--) {
                    BlockPos cell = origin.offset(dx, dy, dz);
                    if (!level.getBlockState(cell).isAir()) {
                        level.setBlock(cell, air, QUIET);
                        cleared++;
                    }
                }
            }
        }
        Map<Long, BlockState> floors = new HashMap<>();
        for (ShowcasePlacement p : plan.placements()) {
            if (p.kind() == ShowcasePlacement.Kind.FLOOR) {
                floors.put(column(p.x(), p.z()), p.state().get());
            }
        }
        for (int dx = 0; dx < plan.width(); dx++) {
            for (int dz = 0; dz < plan.depth(); dz++) {
                level.setBlock(origin.offset(dx, 0, dz), floors.getOrDefault(column(dx, dz), DEFAULT_FLOOR), QUIET);
            }
        }
        int placed = 0;
        for (ShowcasePlacement p : plan.placements()) {
            BlockPos pos = origin.offset(p.x(), p.y(), p.z());
            switch (p.kind()) {
                case FLOOR -> {
                }
                case SUPPORT, CONTENT -> {
                    level.setBlock(pos, p.state().get(), Block.UPDATE_ALL);
                    placed++;
                }
                case LABEL -> {
                    ShowcaseLabels.place(level, pos, p.lines());
                    placed++;
                }
            }
        }
        for (ShowcasePlacement p : plan.placements()) {
            if (p.init() != null) {
                BlockPos pos = origin.offset(p.x(), p.y(), p.z());
                BlockEntity entity = level.getBlockEntity(pos);
                if (entity != null) {
                    p.init().accept(entity);
                    entity.setChanged();
                    level.sendBlockUpdated(pos, entity.getBlockState(), entity.getBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
        return new Result(cleared, placed);
    }

    private static long column(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
}
