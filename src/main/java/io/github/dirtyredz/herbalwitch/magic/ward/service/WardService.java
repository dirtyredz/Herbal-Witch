package io.github.dirtyredz.herbalwitch.magic.ward.service;

import io.github.dirtyredz.herbalwitch.magic.ward.block.WardStoneBlock;
import io.github.dirtyredz.herbalwitch.magic.ward.block.WardStoneBlockEntity;
import io.github.dirtyredz.herbalwitch.magic.ward.catalog.WardCatalog;
import io.github.dirtyredz.herbalwitch.magic.ward.persistence.WardRecord;
import io.github.dirtyredz.herbalwitch.magic.ward.persistence.WardSavedData;
import io.github.dirtyredz.herbalwitch.magic.ward.targeting.WardProtection;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WorkingRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;

import java.util.Optional;

/**
 * The loaded ward index, lifecycle reset and bounded ticks/sweeps. WardLifecycle is the sole
 * stone/record reconciliation authority; activation, controls and focus have separate owners.
 */
public final class WardService {
    public static final String KEY = "ward.herbalwitch.";
    private static final WardIndex INDEX = new WardIndex();

    private WardService() {
    }

    public static void serverStarted(ServerStartedEvent event) {
        INDEX.clear();
    }

    public static void serverStopped(ServerStoppedEvent event) {
        INDEX.clear();
        WardProtection.reset();
    }

    static WorkingRules rules() {
        return WitchRules.server().workings();
    }

    // ---------------------------------------------------------------- index

    public static WardIndex index() {
        return INDEX;
    }

    public static WardIndex.Entry entry(long id) {
        return INDEX.entry(id);
    }

    public static boolean indexed(long id) {
        return INDEX.contains(id);
    }

    /** Indexes a charged ward unless it would overlap another active one (then it stays unindexed and protects nothing). */
    static boolean index(ServerLevel level, WardRecord record) {
        record.setFocused(WardFocus.focused(level, BlockPos.of(record.pos())));
        WardIndex.Entry entry = entryOf(record);
        if (INDEX.overlaps(entry.dimension(), entry.centre(), entry.radius(), entry.height(), record.id())) {
            INDEX.remove(record.id());
            return false;
        }
        INDEX.add(entry);
        return true;
    }

    static void unindex(long id) {
        INDEX.remove(id);
    }

    private static WardIndex.Entry entryOf(WardRecord record) {
        WorkingRules r = rules();
        return new WardIndex.Entry(record.id(), record.dimension(), BlockPos.of(record.pos()),
                record.effectiveRadius(r.wardRadius(), r.wardFocusRadius()), r.wardHeight(), record.profile(), record.owner().orElse(null));
    }

    /** Shows (or hides) the charged look. */
    static void show(ServerLevel level, BlockPos pos, boolean active) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof WardStoneBlock && state.getValue(WardStoneBlock.ACTIVE) != active) {
            level.setBlock(pos, state.setValue(WardStoneBlock.ACTIVE, active), Block.UPDATE_ALL);
        }
    }

    // ---------------------------------------------------------------- the stone's record

    /** The stone at {@code pos}, if it is one and loaded. */
    public static Optional<WardStoneBlockEntity> stone(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos) || !(level.getBlockEntity(pos) instanceof WardStoneBlockEntity stone)) {
            return Optional.empty();
        }
        return Optional.of(stone);
    }

    /** The stone's reconciled record, if it has one. */
    public static Optional<WardRecord> record(ServerLevel level, BlockPos pos) {
        return stone(level, pos).flatMap(s -> WardLifecycle.reconciled(level, pos, s));
    }

    // ---------------------------------------------------------------- ticking

    /** One loaded tick of a charged stone: the charge runs down (protecting or not) and the ward sweeps. */
    public static void tick(ServerLevel level, BlockPos pos, WardStoneBlockEntity stone) {
        Optional<WardRecord> found = WardLifecycle.reconciled(level, pos, stone);
        if (found.isEmpty() || !found.get().charged()) {
            found.ifPresent(r -> unindex(r.id()));
            show(level, pos, false);
            return;
        }
        WardRecord record = found.get();
        WardSavedData.get(level).charge(record, record.charge() - 1);
        if (!record.charged()) {
            unindex(record.id());
            show(level, pos, false);
            return;
        }
        if ((level.getGameTime() + record.id()) % WardCatalog.SWEEP_INTERVAL == 0) {
            boolean wasFocused = record.focused();
            boolean focused = WardFocus.focused(level, pos);
            if (focused != wasFocused || !indexed(record.id())) {
                index(level, record); // the focus came or went: the radius changes now
            } else {
                record.setFocused(focused);
            }
            WardIndex.Entry entry = INDEX.entry(record.id());
            if (entry != null) {
                WardProtection.sweep(level, entry);
            }
        }
    }
}
