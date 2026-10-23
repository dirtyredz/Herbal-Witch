package io.github.dirtyredz.herbalwitch.apothecary.shop.registry;

import io.github.dirtyredz.herbalwitch.apothecary.shop.customer.ShopCustomerScheduler;
import io.github.dirtyredz.herbalwitch.apothecary.shop.customer.demand.NpcDemandService;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link.ShopProjection;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStore;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopPlayerReceipt;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopReconciliation;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopSessionService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;

/**
 * Commerce's server lifecycle: the demand record loads before shops are indexed (restart
 * reconciliation consults it), shards index 32 per tick with reconciliation as each loads, and each
 * tick advances the commerce clock, villager visits, a bounded display refresh and, every 200 ticks,
 * re-checks online players whose receipt is still pending (resolved only after a verified save).
 */
public final class ShopServerEvents {
    private ShopServerEvents() {
    }

    public static void started(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        NpcDemandService.get(server);
        ShopStore store = ShopStore.get(server);
        store.onLoaded(shard -> ShopReconciliation.startup(server, store, shard));
    }

    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        NpcDemandService.tick(server);
        ShopStore store = ShopStore.get(server);
        store.indexBatch();
        if (!store.ready()) return;
        ShopCustomerScheduler.tick(server);
        ShopProjection.refresh(server);
        if (server.getTickCount() % 200 == 0) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (ShopPlayerReceipt.of(player).map(ShopPlayerReceipt.Marker::pending).orElse(false)) ShopReconciliation.player(player, false);
            }
        }
    }

    public static void stopped(ServerStoppedEvent event) {
        MinecraftServer server = event.getServer();
        ShopCustomerScheduler.stopped(server);
        ShopSessionService.stopped(server);
        NpcDemandService.stopped(server);
        ShopStore.stopped(server);
    }
}
