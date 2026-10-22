package io.github.dirtyredz.herbalwitch.magic.ward.service;

import io.github.dirtyredz.herbalwitch.magic.ward.block.WardStoneBlock;
import io.github.dirtyredz.herbalwitch.magic.ward.block.WardStoneBlockEntity;
import io.github.dirtyredz.herbalwitch.magic.ward.catalog.WardCatalog;
import io.github.dirtyredz.herbalwitch.magic.ward.persistence.WardRecord;
import io.github.dirtyredz.herbalwitch.magic.ward.persistence.WardSavedData;
import io.github.dirtyredz.herbalwitch.magic.ward.targeting.WardProtection;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WorkingRules;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyService;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchState;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionContext;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionPermissionService;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;

import java.util.Optional;

/**
 * The wards on a running server: activation (directly at the stone, or by the circle's home
 * protection ritual, which pays its own energy), the owner's settings, deactivation and release, and
 * each charged ward's loaded ticks. One activation grants the rules' charge; a charged ward is never
 * topped up and a deactivated one refunds nothing. Every outcome is a key suffix
 * ({@code ward.herbalwitch.<outcome>}). Charge, owner and settings live only in {@link WardSavedData};
 * the index is rebuilt from loaded stones.
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

    /** Loaded ticks of protection one activation grants. */
    public static int chargeTicks() {
        return rules().wardChargeSeconds() * 20;
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
        record.setFocused(focused(level, BlockPos.of(record.pos())));
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

    /** Whether a functional hearth or stove stands within {@link WardCatalog#FOCUS_REACH} (loaded cells only). */
    public static boolean focused(ServerLevel level, BlockPos stone) {
        int reach = WardCatalog.FOCUS_REACH;
        for (BlockPos p : BlockPos.betweenClosed(stone.offset(-reach, -2, -reach), stone.offset(reach, 2, reach))) {
            if (!level.isLoaded(p)) {
                continue;
            }
            Block block = level.getBlockState(p).getBlock();
            if (block == StationBlocks.HEARTH.get() || block == StationBlocks.STOVE.get()) {
                return true;
            }
        }
        return false;
    }

    /** The largest radius the stone may be set to now. */
    public static int maxRadius(ServerLevel level, BlockPos stone) {
        return focused(level, stone) ? rules().wardFocusRadius() : rules().wardRadius();
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
        if (INDEX.active(dimension) >= rules().wardsPerDimension()) {
            return Optional.of("dimension_limit");
        }
        if (INDEX.centres(dimension, pos) >= WardCatalog.CENTRES_PER_CHUNK) {
            return Optional.of("chunk_limit");
        }
        int radius = Math.min(record.map(WardRecord::radius).orElse(rules().wardRadius()), maxRadius(level, pos));
        if (INDEX.overlaps(dimension, pos, radius, rules().wardHeight(), record.map(WardRecord::id).orElse(-1L))) {
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
        Optional<WitchState> before = WitchStatusService.snapshot(witch);
        if (WitchStatusService.debit(witch, units) != EnergyService.Result.OK) {
            return "not_enough_energy";
        }
        if (!charge(witch, level, pos)) {
            before.ifPresent(s -> WitchStatusService.restore(witch, s));
            return "failed";
        }
        WitchStatusService.awardPractice(witch, units);
        WitchStatusService.flush(witch);
        return "activated";
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
        unindex(record.get().id());
        show(level, pos, false);
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
        unindex(record.get().id());
        show(level, pos, false);
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
        if (r.charged() && INDEX.overlaps(r.dimension(), pos, Math.min(next, maxRadius(level, pos)), rules().wardHeight(), r.id())) {
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
            boolean focused = focused(level, pos);
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
