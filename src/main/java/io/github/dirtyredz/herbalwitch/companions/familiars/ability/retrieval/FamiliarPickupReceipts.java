package io.github.dirtyredz.herbalwitch.companions.familiars.ability.retrieval;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import java.util.*;

/** O(1) source identity lookup on admission; no world/entity scan and no startup directory materialization. */
public final class FamiliarPickupReceipts {
    private static final Map<MinecraftServer, FamiliarPickupJournal> WORLDS = new WeakHashMap<>();
    private FamiliarPickupReceipts() {}
    public static FamiliarPickupJournal get(MinecraftServer server) {
        return WORLDS.computeIfAbsent(server, s -> new FamiliarPickupJournal(s.getWorldPath(LevelResource.ROOT).resolve("data/herbalwitch_familiar_pickups")));
    }
    public static void join(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof ItemEntity item)) return;
        if (get(item.getServer()).retired(item.getUUID())) { event.setCanceled(true); item.discard(); }
    }
    public static void stopped(ServerStoppedEvent event) { WORLDS.remove(event.getServer()); }
}
