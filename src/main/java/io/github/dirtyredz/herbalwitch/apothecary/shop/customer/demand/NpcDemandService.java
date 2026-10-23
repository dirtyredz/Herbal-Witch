package io.github.dirtyredz.herbalwitch.apothecary.shop.customer.demand;

import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopServerConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Server lifecycle of the demand record: loaded before shops are indexed (so restart reconciliation can
 * consult debits), ticked once per running server tick, and the durable debit used by villager sales.
 * Its elapsed ticks are also the commerce clock for request lifetimes and cooldowns.
 */
public final class NpcDemandService {
    private static final Map<MinecraftServer, NpcDemandSavedData> WORLDS = new WeakHashMap<>();

    private NpcDemandService() {
    }

    public static NpcDemandSavedData get(MinecraftServer server) {
        return WORLDS.computeIfAbsent(server, s -> {
            NpcDemandSavedData data = new NpcDemandSavedData(s.getWorldPath(LevelResource.ROOT).resolve("data"));
            data.load();
            s.overworld().getDataStorage().set(NpcDemandSavedData.NAME, data);
            return data;
        });
    }

    public static void stopped(MinecraftServer server) {
        WORLDS.remove(server);
    }

    /** Running-tick commerce clock. */
    public static long now(MinecraftServer server) {
        return get(server).demand().elapsed();
    }

    public static void tick(MinecraftServer server) {
        get(server).tick(1, ShopServerConfig.current().economyWindow());
    }

    /** Durably charges one emerald for a villager sale; false when refused or not provably written. */
    public static boolean debit(MinecraftServer server, UUID transaction, UUID owner, UUID shop, UUID villager) {
        NpcDemandSavedData data = get(server);
        if (!data.writable()) return false;
        NpcDemand next = data.demand().copy();
        if (next.debit(transaction, owner, shop, villager, ShopServerConfig.current()) != NpcDemand.Refusal.NONE) return false;
        return data.commit(next);
    }

    public static boolean debited(MinecraftServer server, UUID transaction) {
        return get(server).demand().debited(transaction);
    }
}
