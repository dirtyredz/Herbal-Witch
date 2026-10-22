package io.github.dirtyredz.herbalwitch.magic.ward.service;

import io.github.dirtyredz.herbalwitch.magic.ward.block.WardStoneBlock;
import io.github.dirtyredz.herbalwitch.magic.ward.persistence.WardRecord;
import io.github.dirtyredz.herbalwitch.magic.ward.persistence.WardSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

import static io.github.dirtyredz.herbalwitch.magic.ward.service.WardContext.rules;
import static io.github.dirtyredz.herbalwitch.magic.ward.service.WardFocus.maxRadius;
import static io.github.dirtyredz.herbalwitch.magic.ward.service.WardLifecycle.index;
import static io.github.dirtyredz.herbalwitch.magic.ward.service.WardLifecycle.record;

/** Owner settings, deactivation and release. Menu access checks remain with the menu. */
public final class WardControls {
    private WardControls() { }

    // ---------------------------------------------------------------- the owner's controls

    /** Ends the protection now; the remaining charge is discarded. */
    public static String deactivate(ServerPlayer owner, ServerLevel level, BlockPos pos) {
        Optional<WardRecord> record = record(level, pos);
        if (record.isEmpty() || !record.get().charged()) {
            return "not_charged";
        }
        if (!record.get().ownedBy(owner.getUUID())) {
            return "not_owner";
        }
        WardSavedData.get(level).charge(record.get(), 0);
        WardIndex.loaded().remove(record.get().id());
        WardStoneBlock.show(level, pos, false);
        return "deactivated";
    }

    /** Unbinds the stone from its owner, ending any protection (nothing refunded). */
    public static String release(ServerPlayer owner, ServerLevel level, BlockPos pos) {
        Optional<WardRecord> record = record(level, pos);
        if (record.isEmpty() || record.get().owner().isEmpty()) {
            return "unbound";
        }
        if (!record.get().ownedBy(owner.getUUID())) {
            return "not_owner";
        }
        WardSavedData data = WardSavedData.get(level);
        data.charge(record.get(), 0);
        data.bind(record.get(), null);
        WardIndex.loaded().remove(record.get().id());
        WardStoneBlock.show(level, pos, false);
        return "released";
    }

    /** Changes the chosen radius by {@code delta}, within 1 and the current maximum; an active ward must not overlap. */
    public static String radius(ServerPlayer owner, ServerLevel level, BlockPos pos, int delta) {
        Optional<WardRecord> record = record(level, pos);
        if (record.isEmpty() || !record.get().ownedBy(owner.getUUID())) {
            return record.isEmpty() ? "unbound" : "not_owner";
        }
        WardRecord r = record.get();
        int next = Math.max(1, Math.min(maxRadius(level, pos), r.radius() + delta));
        if (next == r.radius()) {
            return "radius_limit";
        }
        if (r.charged() && WardIndex.loaded().overlaps(r.dimension(), pos, Math.min(next, maxRadius(level, pos)), rules().wardHeight(), r.id())) {
            return "overlap";
        }
        WardSavedData.get(level).radius(r, next);
        if (r.charged()) {
            index(level, r);
        }
        return "radius";
    }

    /** Cycles the target profile (undead, arthropods, both). */
    public static String profile(ServerPlayer owner, ServerLevel level, BlockPos pos) {
        Optional<WardRecord> record = record(level, pos);
        if (record.isEmpty() || !record.get().ownedBy(owner.getUUID())) {
            return record.isEmpty() ? "unbound" : "not_owner";
        }
        WardSavedData.get(level).profile(record.get(), record.get().profile().next());
        if (record.get().charged()) {
            index(level, record.get());
        }
        return "profile";
    }

}
