package io.github.dirtyredz.herbalwitch.magic.ward.service;

import io.github.dirtyredz.herbalwitch.magic.ward.block.WardStoneBlock;
import io.github.dirtyredz.herbalwitch.magic.ward.block.WardStoneBlockEntity;
import io.github.dirtyredz.herbalwitch.magic.ward.catalog.WardCatalog;
import io.github.dirtyredz.herbalwitch.magic.ward.persistence.WardRecord;
import io.github.dirtyredz.herbalwitch.magic.ward.persistence.WardSavedData;
import io.github.dirtyredz.herbalwitch.magic.ward.targeting.WardProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;

import java.util.Optional;

/**
 * Ticking and server lifecycle for the loaded ward index. This is the top of the ward service
 * package: activation, controls, focus and lifecycle depend on {@link WardIndex} and
 * {@link WardContext}, never on this class. WardLifecycle is the sole stone/record reconciliation
 * authority.
 */
public final class WardService {
    public static final String KEY = "ward.herbalwitch.";

    private WardService() {
    }

    public static void serverStarted(ServerStartedEvent event) {
        WardIndex.loaded().clear();
    }

    public static void serverStopped(ServerStoppedEvent event) {
        WardIndex.loaded().clear();
        WardProtection.reset();
    }

    /** One loaded tick of a charged stone: the charge runs down (protecting or not) and the ward sweeps. */
    public static void tick(ServerLevel level, BlockPos pos, WardStoneBlockEntity stone) {
        WardIndex index = WardIndex.loaded();
        Optional<WardRecord> found = WardLifecycle.reconciled(level, pos, stone);
        if (found.isEmpty() || !found.get().charged()) {
            found.ifPresent(r -> index.remove(r.id()));
            WardStoneBlock.show(level, pos, false);
            return;
        }
        WardRecord record = found.get();
        WardSavedData.get(level).charge(record, record.charge() - 1);
        if (!record.charged()) {
            index.remove(record.id());
            WardStoneBlock.show(level, pos, false);
            return;
        }
        if ((level.getGameTime() + record.id()) % WardCatalog.SWEEP_INTERVAL == 0) {
            boolean wasFocused = record.focused();
            boolean focused = WardFocus.focused(level, pos);
            if (focused != wasFocused || !index.contains(record.id())) {
                WardLifecycle.index(level, record); // the focus came or went: the radius changes now
            } else {
                record.setFocused(focused);
            }
            WardIndex.Entry entry = index.entry(record.id());
            if (entry != null) {
                WardProtection.sweep(level, entry);
            }
        }
    }
}
