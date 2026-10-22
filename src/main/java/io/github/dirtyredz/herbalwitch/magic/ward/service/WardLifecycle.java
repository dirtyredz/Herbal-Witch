package io.github.dirtyredz.herbalwitch.magic.ward.service;

import io.github.dirtyredz.herbalwitch.magic.ward.block.WardStoneBlock;
import io.github.dirtyredz.herbalwitch.magic.ward.block.WardStoneBlockEntity;
import io.github.dirtyredz.herbalwitch.magic.ward.persistence.WardRecord;
import io.github.dirtyredz.herbalwitch.magic.ward.persistence.WardSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

/**
 * Keeping the stone, its record and the index in step. When a stone loads, its record id must name a
 * record for exactly this dimension and position, and the block must still be a ward stone; anything
 * else (a copied stone, a moved record) stays inert and is never indexed. A charged, reconciled ward
 * joins the index; an unloaded one leaves it (it neither protects nor spends while unloaded, and is
 * never caught up). Breaking a stone retires its record: the dropped item carries no charge.
 */
public final class WardLifecycle {
    private WardLifecycle() {
    }

    /** The record this stone may use, if its id reconciles with its dimension, position and block. */
    public static Optional<WardRecord> reconciled(ServerLevel level, BlockPos pos, WardStoneBlockEntity stone) {
        if (!(level.getBlockState(pos).getBlock() instanceof WardStoneBlock)) {
            return Optional.empty();
        }
        return recordAt(level, pos, stone);
    }

    /** The record whose id, dimension and position match the stone (the block itself is not read). */
    private static Optional<WardRecord> recordAt(ServerLevel level, BlockPos pos, WardStoneBlockEntity stone) {
        if (stone.recordId() <= 0) {
            return Optional.empty();
        }
        WardSavedData data = WardSavedData.get(level);
        if (!data.writable()) {
            return Optional.empty();
        }
        return data.record(stone.recordId()).filter(r -> r.pos() == pos.asLong()
                && r.dimension().equals(level.dimension().location().toString()));
    }

    /** A stone came into a loaded chunk (or was placed). */
    public static void loaded(ServerLevel level, BlockPos pos, WardStoneBlockEntity stone) {
        level.getServer().execute(() -> {
            if (stone.isRemoved()) {
                return;
            }
            Optional<WardRecord> record = reconciled(level, pos, stone);
            if (record.isPresent() && record.get().charged()) {
                WardService.index(level, record.get());
            }
            WardService.show(level, pos, record.map(WardRecord::charged).orElse(false) && WardService.indexed(record.get().id()));
        });
    }

    /** A stone's chunk unloaded: its ward leaves the index at once (only the stone that indexed it can). */
    public static void unloaded(ServerLevel level, BlockPos pos, long recordId) {
        WardIndex.Entry entry = recordId > 0 ? WardService.entry(recordId) : null;
        if (entry != null && entry.centre().equals(pos) && entry.dimension().equals(level.dimension().location().toString())) {
            WardService.unindex(recordId);
        }
    }

    /** A stone was broken or replaced: its record is retired (no refund, no charge in the drop). */
    public static void removed(ServerLevel level, BlockPos pos, WardStoneBlockEntity stone) {
        recordAt(level, pos, stone).ifPresent(r -> { // a copied stone's foreign id is left alone
            WardService.unindex(r.id());
            WardSavedData.get(level).retire(r.id());
        });
    }
}
