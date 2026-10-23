package io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarCatalog;
import io.github.dirtyredz.herbalwitch.companions.lifecycle.CompanionLifecycleAuthority;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import java.util.*;

/** Admission precedes gameplay. Unmarked old source entities are checked through the UUID index too. */
public final class FamiliarReconciliation {
    private record Permit(Mob entity, UUID transaction, long generation) {}
    private static final Map<MinecraftServer, Permit> PERMITS = new WeakHashMap<>();
    private static final Map<Mob, Boolean> WAITING = new WeakHashMap<>();
    private static final Set<Mob> HELD = Collections.newSetFromMap(new WeakHashMap<>());
    private FamiliarReconciliation() {}
    public static AutoCloseable permit(Mob candidate, FamiliarRecord r) {
        var server = candidate.getServer();
        if (PERMITS.containsKey(server)) throw new IllegalStateException("Familiar admission already active");
        var permit = new Permit(candidate, r.transaction(), r.generation()); PERMITS.put(server, permit);
        return () -> PERMITS.remove(server, permit);
    }
    public static Optional<FamiliarRecord> record(Mob mob) {
        if (!(mob.level() instanceof ServerLevel level)) return Optional.empty();
        var store = FamiliarStore.get(level.getServer());
        if (!store.ready() || HELD.contains(mob)) return Optional.empty();
        return store.creature(mob.getUUID()).filter(r -> store.writable(r.bond()) && r.phase() == FamiliarRecord.Phase.ACTIVE
                && !r.has("receipt") && matches(mob, r));
    }
    public static Mob loaded(MinecraftServer server, FamiliarRecord record) {
        for (var level : server.getAllLevels()) {
            if (level.getEntity(record.creature()) instanceof Mob mob && mob.isAlive() && !mob.isRemoved() && matches(mob, record)) return mob;
        }
        return null;
    }
    public static boolean matches(Mob mob, FamiliarRecord r) {
        return r.creature().equals(mob.getUUID()) && EntityType.getKey(mob.getType()).toString().equals(FamiliarCatalog.find(r.species()).orElseThrow().entityId())
                && FamiliarBinding.of(mob).flatMap(FamiliarBinding::reference).map(ref -> ref.bond().equals(r.bond())
                && ref.owner().equals(r.owner()) && ref.generation() == r.generation()).orElse(false);
    }
    public static boolean governed(Mob mob) {
        if (!(mob.level() instanceof ServerLevel level)) return FamiliarBinding.of(mob).isPresent();
        var store = FamiliarStore.get(level.getServer());
        return FamiliarBinding.of(mob).filter(cap -> !cap.empty()).isPresent() || store.creature(mob.getUUID()).isPresent() || WAITING.containsKey(mob);
    }
    public static boolean suspended(Mob mob) {
        if (WAITING.containsKey(mob) || HELD.contains(mob)) return true;
        if (!(mob.level() instanceof ServerLevel level)) return false;
        var store = FamiliarStore.get(level.getServer());
        return store.creature(mob.getUUID()).map(r -> !store.writable(r.bond())
                || r.phase() != FamiliarRecord.Phase.ACTIVE || r.has("receipt")).orElse(false);
    }
    /** A failed critical snapshot keeps the live object inert until explicit administrative recovery. */
    public static void hold(Mob mob) { mob.getNavigation().stop(); mob.setTarget(null); HELD.add(mob); }
    public static void join(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof Mob mob)) return;
        var store = FamiliarStore.get(level.getServer());
        if (!store.ready() || FamiliarBinding.of(mob).filter(cap -> !cap.valid()).isPresent()) {
            if (FamiliarCatalog.type(EntityType.getKey(mob.getType()).toString()).isPresent()) {
                WAITING.put(mob, mob.isNoAi());
            }
            return;
        }
        if (!admit(mob, store)) { event.setCanceled(true); mob.discard(); }
        else if (event.isCanceled() && store.creature(mob.getUUID()).isPresent()) mob.discard();
    }
    private static boolean admit(Mob mob, FamiliarStore store) {
        var r = store.creature(mob.getUUID()).orElse(null);
        var cap = FamiliarBinding.of(mob).orElse(null);
        if (!store.ready()) return r == null && cap == null && FamiliarCatalog.type(EntityType.getKey(mob.getType()).toString()).isEmpty();
        if (r == null) return cap == null || cap.empty();
        if (!store.writable(r.bond()) || !matches(mob, r)) return false;
        Permit permit = PERMITS.get(mob.getServer());
        boolean allowed = permit != null && permit.entity() == mob && permit.transaction().equals(r.transaction()) && permit.generation() == r.generation();
        if (r.phase() != FamiliarRecord.Phase.ACTIVE && !allowed) return false;
        if (!CompanionLifecycleAuthority.admit(mob.getServer(), mob)) {
            store.update(r.quarantine(), true);
            CompanionLifecycleAuthority.retire(mob.getServer(), r.creature());
            return false;
        }
        mob.setCanPickUpLoot(false); mob.setPersistenceRequired(); return true;
    }
    public static void indexed(MinecraftServer server) {
        var store = FamiliarStore.get(server); if (!store.ready()) return;
        var iterator = WAITING.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next(); var mob = entry.getKey(); if (mob.getServer() != server) continue;
            if (FamiliarBinding.of(mob).filter(cap -> !cap.valid()).isPresent()) continue;
            iterator.remove();
            if (!mob.isRemoved() && !admit(mob, store)) mob.discard();
        }
    }
    public static void stopped(MinecraftServer server) {
        PERMITS.remove(server); WAITING.keySet().removeIf(mob -> mob.getServer() == server);
        HELD.removeIf(mob -> mob.getServer() == server);
    }
}
