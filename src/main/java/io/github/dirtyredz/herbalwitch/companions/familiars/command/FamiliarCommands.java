package io.github.dirtyredz.herbalwitch.companions.familiars.command;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.config.FamiliarServerConfig;
import io.github.dirtyredz.herbalwitch.companions.familiars.menu.FamiliarMenuAccess;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import java.util.*;

/** Mutually exclusive desired commands; assignment and the bounded reservation change in one shard write. */
public final class FamiliarCommands {
    private static final Map<MinecraftServer, Long> APPLIED = new WeakHashMap<>();
    private FamiliarCommands() {}
    public static String set(ServerPlayer owner, UUID bond, FamiliarCommand command) {
        var store = FamiliarStore.get(owner.server); var r = store.record(bond).orElse(null);
        if (!FamiliarMenuAccess.roster(owner) || r == null || !r.owner().equals(owner.getUUID()) || !store.writable(bond)
                || r.phase() != FamiliarRecord.Phase.ACTIVE || r.state().contains("receipt")) return "unavailable";
        if (r.command() == command) return "command_set";
        if (command.mobile() && !r.command().mobile() && store.mobile().count(owner.getUUID()) >= FamiliarServerConfig.current().mobile()) return "mobile_cap";
        if ((command == FamiliarCommand.PROTECT_HOME || command == FamiliarCommand.RETURN_HOME) && !r.state().contains("home")) return "no_home";
        var mob = FamiliarMenuAccess.loaded(owner, r); var state = r.state();
        long position = mob == null ? state.getLong("position") : mob.blockPosition().asLong();
        String dimension = mob == null ? state.getString("dimension") : mob.level().dimension().location().toString();
        long reservation = command.mobile() ? r.command().mobile() ? state.getLong("mobileOrder") : store.mobile().nextOrder() : 0;
        var next = r.change(s -> {
            s.putString("command", command.key()); s.putString("status", mob == null ? "not_loaded" : "");
            s.putLong("anchor", position); s.putString("anchorDimension", dimension);
            if (command.mobile()) s.putLong("mobileOrder", reservation); else s.remove("mobileOrder");
        });
        if (!store.update(next, true)) return "unavailable";
        if (mob != null) { mob.setTarget(null); mob.getNavigation().stop(); }
        return "command_set";
    }
    public static void stay(Mob mob, String reason) {
        FamiliarReconciliation.record(mob).ifPresent(r -> stay(FamiliarStore.get(mob.getServer()), r, mob, reason));
    }
    private static void stay(FamiliarStore store, FamiliarRecord record, Mob mob, String reason) {
        var next = record.change(s -> {
            s.putString("command", FamiliarCommand.STAY.key()); s.remove("mobileOrder"); s.putString("status", reason);
            s.putLong("anchor", mob == null ? s.getLong("position") : mob.blockPosition().asLong());
            s.putString("anchorDimension", mob == null ? s.getString("dimension") : mob.level().dimension().location().toString());
        });
        store.update(next, true);
        if (mob != null) { mob.setTarget(null); mob.getNavigation().stop(); }
    }
    public static void stopAssignments(MinecraftServer server, UUID owner, String reason) {
        var store = FamiliarStore.get(server); if (!store.ready()) return;
        for (UUID id : store.mobile().assignments(owner)) store.record(id).ifPresent(r -> {
            stay(store, r, FamiliarReconciliation.loaded(server, r), reason);
        });
    }
    public static void reconcileCap(MinecraftServer server) {
        var store = FamiliarStore.get(server); if (!store.ready()) return;
        long revision = FamiliarServerConfig.revision(); if (Objects.equals(APPLIED.get(server), revision)) return;
        boolean startup = !APPLIED.containsKey(server);
        APPLIED.put(server, revision); int cap = FamiliarServerConfig.current().mobile();
        if (startup) {
            // A restart is not consent to resume mobile tasks, including records whose entity is unloaded.
            for (UUID owner : store.mobile().owners()) stopAssignments(server, owner, "owner_offline");
            return;
        }
        for (UUID owner : store.mobile().owners()) {
            var reservations = store.mobile().assignments(owner);
            for (int i = cap; i < reservations.size(); i++) store.record(reservations.get(i)).ifPresent(r -> {
                stay(store, r, FamiliarReconciliation.loaded(server, r), "mobile_cap");
            });
            if (reservations.size() > cap) OnlinePlayers.find(owner).ifPresent(p -> p.displayClientMessage(Component.translatable("familiar.herbalwitch.mobile_reduced", cap), false));
        }
    }
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) stopAssignments(player.server, player.getUUID(), "owner_offline");
    }
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) stopAssignments(player.server, player.getUUID(), "owner_offline");
    }
    public static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) stopAssignments(player.server, player.getUUID(), "owner_dead");
    }
    public static void stopped(MinecraftServer server) { APPLIED.remove(server); }
}
