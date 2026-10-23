package io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link;

import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopDisplayBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopFixtureBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopRegisterBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopLink;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopOffer;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStore;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

/**
 * Publishes a shop's public face after any change: the register's open/closed indicator and each
 * loaded, still-matching display's sample projection (sold out when the register is unloaded, the shop
 * is closed or blocked, or stock is short). Only loaded positions are touched; nothing loads a chunk.
 * Listeners (open menu sessions) hear every publication.
 */
public final class ShopProjection {
    private static final List<BiConsumer<MinecraftServer, UUID>> LISTENERS = new CopyOnWriteArrayList<>();
    private static int cursor;

    private ShopProjection() {
    }

    public static void listen(BiConsumer<MinecraftServer, UUID> listener) {
        LISTENERS.add(listener);
    }

    public static void publish(MinecraftServer server, UUID shop) {
        ShopStore.get(server).record(shop).ifPresent(record -> project(server, record));
        for (var listener : LISTENERS) listener.accept(server, shop);
    }

    /** Bounded periodic refresh (a few shops per call), so register unloads show on their displays. */
    public static void refresh(MinecraftServer server) {
        ShopStore store = ShopStore.get(server);
        if (!store.ready()) return;
        List<UUID> ids = store.ids();
        if (ids.isEmpty()) return;
        for (int i = 0; i < Math.min(4, ids.size()); i++) {
            cursor = Math.floorMod(cursor + 1, ids.size());
            store.record(ids.get(cursor)).filter(r -> !r.links.isEmpty() || r.binding != null).ifPresent(r -> project(server, r));
        }
    }

    static void project(MinecraftServer server, ShopRecord record) {
        boolean registerLoaded = ShopBindingService.registerLoaded(server, record);
        boolean selling = registerLoaded && record.lifecycle == ShopRecord.Lifecycle.OPEN && !record.blocked();
        if (record.binding != null) {
            ServerLevel level = ShopBindingService.level(server, record.binding.dimension());
            BlockPos pos = BlockPos.of(record.binding.pos());
            if (level != null && registerLoaded) {
                BlockState state = level.getBlockState(pos);
                boolean open = record.lifecycle == ShopRecord.Lifecycle.OPEN && !record.blocked();
                if (state.getValue(ShopRegisterBlock.OPEN) != open) level.setBlock(pos, state.setValue(ShopRegisterBlock.OPEN, open), Block.UPDATE_CLIENTS);
            }
        }
        for (ShopLink link : record.links) {
            if (link.kind() != ShopLink.Kind.SHELF && link.kind() != ShopLink.Kind.JAR) continue;
            ServerLevel level = ShopBindingService.level(server, link.dimension());
            BlockPos pos = BlockPos.of(link.pos());
            if (level == null || !level.hasChunkAt(pos) || !(level.getBlockEntity(pos) instanceof ShopDisplayBlockEntity display)) continue;
            if (!link.instance().equals(display.instance()) || !record.id.equals(display.shop())) continue;
            display.project(samples(record, link, selling));
        }
    }

    static List<ShopDisplayBlockEntity.Sample> samples(ShopRecord record, ShopLink link, boolean selling) {
        List<ShopDisplayBlockEntity.Sample> out = new ArrayList<>();
        for (UUID id : link.offers()) {
            ShopOffer offer = record.offer(id).orElse(null);
            if (offer == null) continue;
            ItemStack stack = offer.key().stack(1);
            if (stack.isEmpty()) continue;
            boolean available = selling && offer.enabled() && record.stock.count(offer.key()) >= offer.lot();
            out.add(new ShopDisplayBlockEntity.Sample(stack, available, offer.lot(), offer.price()));
        }
        return out;
    }

    /** Clears a display or board that no longer belongs to a shop. */
    static void clear(ShopFixtureBlockEntity fixture) {
        if (fixture instanceof ShopDisplayBlockEntity display && !display.demonstration()) display.project(List.of());
        fixture.release();
    }
}
