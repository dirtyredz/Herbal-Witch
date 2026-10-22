package io.github.dirtyredz.herbalwitch.platform.placement;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Writes a checked plan as one transaction: every cell, origin first. A player's item use already
 * runs inside Forge's block-snapshot capture (ForgeHooks.onPlaceItemIntoWorld), which defers
 * neighbour updates until all cells exist, fires one multi-place event for the whole structure, and
 * on cancellation restores every displaced state and the item. Outside that capture (another mod
 * calling {@code place} directly) this class does the same itself: snapshots, the event, and a
 * reverse restore when cancelled.
 */
public final class PlacementCommit {
    private PlacementCommit() {
    }

    /** Places the plan; false when the placement event was cancelled (and everything was restored). */
    public static boolean commit(Level level, PlacementPlan plan, Player player, Direction face) {
        if (level.captureBlockSnapshots || player == null) {
            setAll(level, plan);
            return true; // Forge's capture fires the event and restores on cancellation
        }
        List<BlockSnapshot> snapshots = new ArrayList<>();
        for (PlacementPlan.Cell cell : plan.cells()) {
            snapshots.add(BlockSnapshot.create(level.dimension(), level, cell.pos()));
        }
        setAll(level, plan);
        boolean cancelled = snapshots.size() > 1 ? ForgeEventFactory.onMultiBlockPlace(player, snapshots, face)
                : ForgeEventFactory.onBlockPlace(player, snapshots.get(0), face);
        if (cancelled) {
            for (int i = snapshots.size() - 1; i >= 0; i--) {
                level.restoringBlockSnapshots = true;
                snapshots.get(i).restore(true, false);
                level.restoringBlockSnapshots = false;
            }
            return false;
        }
        return true;
    }

    private static void setAll(Level level, PlacementPlan plan) {
        for (PlacementPlan.Cell cell : plan.cells()) {
            level.setBlock(cell.pos(), cell.state(), Block.UPDATE_ALL);
        }
    }
}
