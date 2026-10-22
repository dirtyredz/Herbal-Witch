package io.github.dirtyredz.herbalwitch.companions.animals.capture.data;

import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalProfiles;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalCapability;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import java.util.*;

/** Exact-object admission and a world-global loaded index; absence is never evidence of death. */
public final class CaptureReconciliation {
    private record Permit(Mob entity, UUID transaction, long generation) {}
    private static final Map<MinecraftServer, Map<UUID, Mob>> LOADED = new WeakHashMap<>();
    private static final Map<MinecraftServer, Permit> PERMITS = new WeakHashMap<>();
    private CaptureReconciliation() {}
    public static long generation(Mob mob) { return AnimalCapability.of(mob).flatMap(AnimalCapability::state).map(s -> s.generation()).orElse(0L); }
    public static AutoCloseable permit(ServerLevel level, Mob mob, CaptureRecord record) {
        MinecraftServer server = level.getServer();
        if (PERMITS.containsKey(server)) throw new IllegalStateException("Capture admission already active");
        Permit permit = new Permit(mob, record.transaction(), record.generation()); PERMITS.put(server, permit);
        return () -> PERMITS.remove(server, permit);
    }
    public static void join(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof Mob mob)) return;
        var data = CaptureSavedData.get(level.getServer());
        var record = data.record(mob.getUUID()).orElse(null);
        long generation = generation(mob);
        if (!data.writable() && (generation > 0 || AnimalProfiles.find(EntityType.getKey(mob.getType()).toString()).isPresent())) { event.setCanceled(true); return; }
        if (record == null) { if (generation > 0) event.setCanceled(true); return; }
        Permit permit = PERMITS.get(level.getServer());
        boolean permitted = permit != null && permit.entity() == mob && permit.transaction().equals(record.transaction())
                && permit.generation() == generation && record.phase() == CaptureRecord.Phase.RELEASE_PREPARED;
        var loaded = LOADED.computeIfAbsent(level.getServer(), key -> new HashMap<>());
        Mob prior = loaded.get(mob.getUUID());
        if (generation != record.generation() || !permitted && record.phase() != CaptureRecord.Phase.RELEASED
                || prior != null && prior != mob && !prior.isRemoved()) { event.setCanceled(true); return; }
        loaded.put(mob.getUUID(), mob);
    }
    public static void leave(EntityLeaveLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof Mob mob) {
            var map = LOADED.get(level.getServer()); if (map != null) map.remove(mob.getUUID(), mob);
        }
    }
    public static void retire(MinecraftServer server, UUID animal) {
        var loaded = LOADED.get(server); Mob mob = loaded == null ? null : loaded.remove(animal);
        if (mob != null && !mob.isRemoved()) mob.discard();
        // The initial incarnation may have joined before its first ledger entry was created.
        for (ServerLevel level : server.getAllLevels()) if (level.getEntity(animal) instanceof Mob existing) existing.discard();
    }
    public static boolean live(MinecraftServer server, UUID animal) {
        for (ServerLevel level : server.getAllLevels()) if (level.getEntity(animal) != null) return true;
        return false;
    }
    public static void reset() { LOADED.clear(); PERMITS.clear(); }
}
