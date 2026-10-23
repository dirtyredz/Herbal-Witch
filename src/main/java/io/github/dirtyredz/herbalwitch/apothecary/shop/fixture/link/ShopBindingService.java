package io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link;

import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopServerConfig;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopFixtureBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopRegisterBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopBinding;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopLink;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStore;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopResult;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

/**
 * The register lifecycle: create a shop at an unbound register, detach it when that register goes,
 * reconnect a detached shop to a new register under a newer binding generation (persisted before it
 * is enabled), and retire an empty shop to a permanent tombstone. A register's claim is honoured only
 * when the record's binding names exactly its dimension, position, instance and generation.
 */
public final class ShopBindingService {
    private ShopBindingService() {
    }

    public static String dimension(Level level) {
        return level.dimension().location().toString();
    }

    /** The shop this register really is, repairing a lost claim and releasing a stale or copied one. */
    public static Optional<ShopRecord> resolveRegister(ServerLevel level, BlockPos pos, ShopFixtureBlockEntity register) {
        ShopStore store = ShopStore.get(level.getServer());
        if (!store.ready() || !register.writable()) return Optional.empty();
        String dimension = dimension(level);
        long at = pos.asLong();
        Optional<ShopRecord> bound = store.indexes().register(dimension, at).flatMap(store::record)
                .filter(r -> r.binding != null && r.binding.dimension().equals(dimension) && r.binding.pos() == at && r.binding.instance().equals(register.instance()));
        if (bound.isPresent()) {
            ShopBinding b = bound.get().binding;
            if (!b.instance().equals(register.instance()) || register.shop() == null || !register.shop().equals(bound.get().id) || register.generation() != b.generation()) {
                register.claim(bound.get().id, b.generation());
            }
            return bound;
        }
        UUID claimed = register.shop();
        if (claimed != null && store.problem(claimed).isEmpty()) {
            ShopRecord record = store.record(claimed).orElse(null);
            boolean valid = record != null && record.binding != null && record.binding.matches(dimension, at, register.instance(), register.generation());
            if (valid) return Optional.of(record);
            register.release();
        }
        return Optional.empty();
    }

    public static ShopResult create(ServerPlayer player, ServerLevel level, BlockPos pos, ShopFixtureBlockEntity register, String name) {
        ShopStore store = ShopStore.get(level.getServer());
        if (!store.ready()) return ShopResult.refused("indexing");
        if (!WitchStatusService.isWitch(player)) return ShopResult.refused("not_witch");
        if (!ShopRecord.validName(name)) return ShopResult.refused("name");
        if (resolveRegister(level, pos, register).isPresent() || register.shop() != null) return ShopResult.refused("already_bound");
        var rules = ShopServerConfig.current();
        if (store.indexes().owned(player.getUUID()).size() >= rules.shopsPerOwner()) return ShopResult.refused("owner_limit");
        if (store.indexes().active() >= rules.shopsPerWorld()) return ShopResult.refused("world_limit");
        ShopRecord record = new ShopRecord(UUID.randomUUID(), player.getUUID(), bounded(player.getGameProfile().getName()), name, level.getGameTime());
        record.generation = 1;
        record.binding = new ShopBinding(dimension(level), pos.asLong(), register.instance(), 1);
        if (!store.create(record)) return ShopResult.refused("unavailable");
        register.claim(record.id, 1);
        ShopProjection.publish(level.getServer(), record.id);
        return ShopResult.ok("created");
    }

    /** Binds the owner's detached shop to this unbound register under a fresh generation. */
    public static ShopResult reconnect(ServerPlayer player, ServerLevel level, BlockPos pos, ShopFixtureBlockEntity register, UUID shop) {
        ShopStore store = ShopStore.get(level.getServer());
        ShopRecord record = store.record(shop).orElse(null);
        if (record == null || !record.owner.equals(player.getUUID())) return ShopResult.refused("not_owner");
        if (record.lifecycle != ShopRecord.Lifecycle.DETACHED || record.binding != null) return ShopResult.refused("not_detached");
        if (resolveRegister(level, pos, register).isPresent() || register.shop() != null) return ShopResult.refused("already_bound");
        ShopRecord next = record.next();
        next.generation = Math.addExact(record.generation, 1);
        next.binding = new ShopBinding(dimension(level), pos.asLong(), register.instance(), next.generation);
        next.lifecycle = ShopRecord.Lifecycle.CLOSED;
        if (!store.update(next, true)) return ShopResult.refused("unavailable");
        register.claim(record.id, next.generation);
        ShopProjection.publish(level.getServer(), record.id);
        return ShopResult.ok("reconnected");
    }

    /** The bound register is gone: the shop detaches; stock and proceeds stay protected in its record. */
    public static void registerRemoved(ServerLevel level, BlockPos pos, ShopFixtureBlockEntity register) {
        ShopStore store = ShopStore.get(level.getServer());
        if (!store.ready()) return;
        ShopRecord record = store.record(register.shop() == null ? new UUID(0, 0) : register.shop()).orElse(null);
        if (record == null || record.binding == null || !record.binding.matches(dimension(level), pos.asLong(), register.instance(), register.generation())) return;
        ShopRecord next = record.next();
        next.binding = null;
        if (next.lifecycle != ShopRecord.Lifecycle.QUARANTINED) next.lifecycle = ShopRecord.Lifecycle.DETACHED;
        store.update(next, true);
        ShopProjection.publish(level.getServer(), record.id);
    }

    /** Retirement leaves a permanent identity tombstone; only an empty, settled shop may retire. */
    public static ShopResult retire(ServerPlayer player, UUID shop) {
        ShopStore store = ShopStore.get(player.server);
        ShopRecord record = store.record(shop).orElse(null);
        if (record == null || !record.owner.equals(player.getUUID())) return ShopResult.refused("not_owner");
        if (record.retired()) return ShopResult.refused("retired");
        if (!record.stock.empty() || record.proceeds != 0 || !record.transactions.isEmpty() || record.orders.stream().anyMatch(o -> o.active()))
            return ShopResult.refused("retire_not_empty");
        ShopRecord next = record.next();
        next.lifecycle = ShopRecord.Lifecycle.RETIRED;
        next.binding = null;
        next.links = new java.util.ArrayList<>();
        next.offers = new java.util.ArrayList<>();
        if (!store.update(next, true)) return ShopResult.refused("unavailable");
        ShopProjection.publish(player.server, shop);
        return ShopResult.ok("retired");
    }

    /** Whether the register's chunk is loaded and it is still this shop's register (never loads a chunk). */
    public static boolean registerLoaded(net.minecraft.server.MinecraftServer server, ShopRecord record) {
        if (record.binding == null) return false;
        ServerLevel level = level(server, record.binding.dimension());
        if (level == null) return false;
        BlockPos pos = BlockPos.of(record.binding.pos());
        if (!level.hasChunkAt(pos) || !(level.getBlockState(pos).getBlock() instanceof ShopRegisterBlock)) return false;
        return level.getBlockEntity(pos) instanceof ShopFixtureBlockEntity be && be.instance().equals(record.binding.instance());
    }

    public static ServerLevel level(net.minecraft.server.MinecraftServer server, String dimension) {
        for (ServerLevel level : server.getAllLevels()) if (dimension(level).equals(dimension)) return level;
        return null;
    }

    /** Whether a fixture link still names this exact fixture. */
    public static boolean linked(ShopRecord record, Level level, BlockPos pos, ShopFixtureBlockEntity fixture) {
        return record.link(dimension(level), pos.asLong()).filter(l -> l.kind() != ShopLink.Kind.COUNTER && fixture.instance().equals(l.instance())
                && record.id.equals(fixture.shop())).isPresent();
    }

    static String bounded(String name) {
        return name.length() > 64 ? name.substring(0, 64) : name;
    }
}
