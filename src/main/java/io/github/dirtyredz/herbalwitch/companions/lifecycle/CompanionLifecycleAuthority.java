package io.github.dirtyredz.herbalwitch.companions.lifecycle;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Shared cross-dimension live-identity check. Capture and familiar records retain their own phase,
 * generation and exact-candidate permits; this layer owns neither payloads nor transport tokens.
 * All calls run on the server thread. UUID lookup never requests a chunk or enumerates entities.
 */
public final class CompanionLifecycleAuthority {
    private static final Map<MinecraftServer, CompanionEntityIndex<Mob>> WORLDS = new WeakHashMap<>();
    private CompanionLifecycleAuthority() {}

    public static boolean admit(MinecraftServer server, Mob candidate) {
        for (ServerLevel level : server.getAllLevels()) {
            var prior = level.getEntity(candidate.getUUID());
            if (prior != null && prior != candidate && !prior.isRemoved()) return false;
        }
        return WORLDS.computeIfAbsent(server, key -> new CompanionEntityIndex<>(Mob::getUUID, mob -> !mob.isRemoved()))
                .admit(candidate);
    }

    public static boolean live(MinecraftServer server, UUID id) {
        var index = WORLDS.get(server);
        if (index != null && index.current(id).isPresent()) return true;
        // An ordinary source may have joined before its first lifecycle record existed.
        for (ServerLevel level : server.getAllLevels()) {
            var entity = level.getEntity(id);
            if (entity != null && !entity.isRemoved()) return true;
        }
        return false;
    }

    public static void retire(MinecraftServer server, UUID id) {
        var index = WORLDS.get(server);
        if (index != null) index.remove(id).filter(mob -> !mob.isRemoved()).ifPresent(Mob::discard);
        for (ServerLevel level : server.getAllLevels())
            if (level.getEntity(id) instanceof Mob mob && !mob.isRemoved()) mob.discard();
    }

    public static void leave(EntityLeaveLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof Mob mob) {
            var index = WORLDS.get(level.getServer());
            if (index != null) index.leave(mob);
        }
    }

    public static void stopped(ServerStoppedEvent event) { WORLDS.remove(event.getServer()); }
}
