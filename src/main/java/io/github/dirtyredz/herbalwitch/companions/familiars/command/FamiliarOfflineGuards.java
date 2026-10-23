package io.github.dirtyredz.herbalwitch.companions.familiars.command;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarRecord;
import io.github.dirtyredz.herbalwitch.companions.familiars.config.FamiliarServerConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Mob;
import java.util.*;

/** Derived loaded home-guard membership. A lookup examines at most the configured eight IDs. */
public final class FamiliarOfflineGuards {
    private record Membership(MinecraftServer server, UUID owner, UUID bond) {}
    private static final Map<Mob, Membership> MEMBERS = new WeakHashMap<>();
    private static final Map<MinecraftServer, Map<UUID, NavigableSet<UUID>>> WORLDS = new WeakHashMap<>();
    private FamiliarOfflineGuards() {}
    public static void observe(Mob mob, FamiliarRecord record) {
        var previous = MEMBERS.get(mob);
        if (record.command() != FamiliarCommand.PROTECT_HOME) { if (previous != null) leave(mob); return; }
        if (previous != null) return;
        MEMBERS.put(mob, new Membership(mob.getServer(), record.owner(), record.bond()));
        WORLDS.computeIfAbsent(mob.getServer(), ignored -> new HashMap<>()).computeIfAbsent(record.owner(), ignored -> new TreeSet<>()).add(record.bond());
    }
    public static boolean allowed(Mob mob, FamiliarRecord record) {
        var owners = WORLDS.get(mob.getServer()); if (owners == null) return false;
        var set = owners.get(record.owner());
        return set != null && set.stream().limit(FamiliarServerConfig.current().offlineGuards()).anyMatch(record.bond()::equals);
    }
    public static void leave(Mob mob) {
        var previous = MEMBERS.remove(mob); if (previous == null) return;
        var owners = WORLDS.get(previous.server()); if (owners == null) return;
        var set = owners.get(previous.owner()); if (set != null) { set.remove(previous.bond()); if (set.isEmpty()) owners.remove(previous.owner()); }
    }
    public static void stopped(MinecraftServer server) { MEMBERS.entrySet().removeIf(e -> e.getValue().server() == server); WORLDS.remove(server); }
}
