package io.github.dirtyredz.herbalwitch.magic.ward.service;

import io.github.dirtyredz.herbalwitch.magic.ward.block.WardStoneBlockEntity;
import io.github.dirtyredz.herbalwitch.magic.ward.catalog.WardCatalog;
import io.github.dirtyredz.herbalwitch.magic.ward.persistence.WardRecord;
import io.github.dirtyredz.herbalwitch.magic.ward.persistence.WardSavedData;
import io.github.dirtyredz.herbalwitch.magic.witch.status.EnergyService;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionContext;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionPermissionService;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

import static io.github.dirtyredz.herbalwitch.magic.ward.service.WardService.rules;
import static io.github.dirtyredz.herbalwitch.magic.ward.service.WardService.stone;
import static io.github.dirtyredz.herbalwitch.magic.ward.service.WardService.index;
import static io.github.dirtyredz.herbalwitch.magic.ward.service.WardService.show;
import static io.github.dirtyredz.herbalwitch.magic.ward.service.WardFocus.maxRadius;

/** Activation checks and charging through the direct spend or already-paid ritual path. */
public final class WardActivation {
    private WardActivation() { }

    /** Loaded ticks of protection one activation grants. */
    public static int chargeTicks() {
        return rules().wardChargeSeconds() * 20;
    }

    // ---------------------------------------------------------------- activation

    /**
     * Why {@code witch} may not activate the stone now (ignoring energy and reach, which only direct
     * activation adds), or empty. Used by direct activation and by the home protection ritual.
     */
    public static Optional<String> refusal(ServerPlayer witch, ServerLevel level, BlockPos pos) {
        Optional<WardStoneBlockEntity> stone = stone(level, pos);
        if (stone.isEmpty()) {
            return Optional.of("no_stone");
        }
        WardSavedData data = WardSavedData.get(level);
        if (!data.writable()) {
            return Optional.of("unsupported");
        }
        Optional<WardRecord> record = WardLifecycle.reconciled(level, pos, stone.get());
        if (record.isPresent() && record.get().charged()) {
            return Optional.of("already_charged");
        }
        if (record.flatMap(WardRecord::owner).filter(o -> !o.equals(witch.getUUID())).isPresent()) {
            return Optional.of("not_owner");
        }
        if (!WitchStatusService.isWitch(witch)) {
            return Optional.of("not_witch");
        }
        boolean binding = record.flatMap(WardRecord::owner).isEmpty();
        if (binding && data.boundTo(witch.getUUID()) >= rules().wardsPerOwner()) {
            return Optional.of("owner_limit");
        }
        if (record.isEmpty() && data.size() >= WardCatalog.MAX_RECORDS) {
            return Optional.of("world_limit");
        }
        String dimension = level.dimension().location().toString();
        if (WardService.index().active(dimension) >= rules().wardsPerDimension()) {
            return Optional.of("dimension_limit");
        }
        if (WardService.index().centres(dimension, pos) >= WardCatalog.CENTRES_PER_CHUNK) {
            return Optional.of("chunk_limit");
        }
        int radius = Math.min(record.map(WardRecord::radius).orElse(rules().wardRadius()), maxRadius(level, pos));
        if (WardService.index().overlaps(dimension, pos, radius, rules().wardHeight(), record.map(WardRecord::id).orElse(-1L))) {
            return Optional.of("overlap");
        }
        if (!EntityActionPermission.checkProtect(level, witch.getUUID(), pos).allowed()) {
            return Optional.of("protected");
        }
        return Optional.empty();
    }

    /** Whether the witch stands close enough and may use the stone. */
    public static boolean reaches(ServerPlayer player, BlockPos pos) {
        return player.isAlive() && !player.isSpectator() && player.position().distanceTo(Vec3.atCenterOf(pos)) <= WardCatalog.REACH
                && ActionPermissionService.mayUse(ActionContext.of(player, player.level(), pos));
    }

    /** Direct activation: the witch pays the activation cost and gains practice from it. */
    public static String activate(ServerPlayer witch, ServerLevel level, BlockPos pos) {
        Optional<String> refusal = refusal(witch, level, pos);
        if (refusal.isPresent()) {
            return refusal.get();
        }
        if (!reaches(witch, pos)) {
            return "too_far";
        }
        int units = EnergyService.units(rules().wardActivationCost());
        if (WitchStatusService.check(witch, units) != EnergyService.Result.OK) {
            return "not_enough_energy";
        }
        boolean[] attempted = {false};
        boolean activated = WitchStatusService.spend(witch, units, () -> {
            attempted[0] = true;
            return charge(witch, level, pos);
        });
        return activated ? "activated" : attempted[0] ? "failed" : "not_enough_energy";
    }

    /** The ritual route: the ritual already paid; checks again and charges. */
    public static String activateByRitual(ServerPlayer witch, ServerLevel level, BlockPos pos) {
        Optional<String> refusal = refusal(witch, level, pos);
        if (refusal.isPresent()) {
            return refusal.get();
        }
        return charge(witch, level, pos) ? "activated" : "failed";
    }

    /** Binds (if unbound), charges and indexes the stone's record. */
    private static boolean charge(ServerPlayer witch, ServerLevel level, BlockPos pos) {
        WardStoneBlockEntity stone = stone(level, pos).orElse(null);
        if (stone == null) {
            return false;
        }
        WardSavedData data = WardSavedData.get(level);
        Optional<WardRecord> existing = WardLifecycle.reconciled(level, pos, stone);
        WardRecord record = existing.orElseGet(() -> data.create(level, pos, rules().wardRadius()).orElse(null));
        if (record == null) {
            return false;
        }
        stone.setRecordId(record.id());
        if (record.owner().isEmpty()) {
            data.bind(record, witch.getUUID());
        }
        if (record.radius() > maxRadius(level, pos)) {
            data.radius(record, maxRadius(level, pos));
        }
        data.charge(record, chargeTicks());
        index(level, record);
        show(level, pos, true);
        return true;
    }

}
