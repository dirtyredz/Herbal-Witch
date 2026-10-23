package io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link;

import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopRules;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopDisplayBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopFixtureBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopFixtureBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopOrderBoardBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopDisplayBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopLink;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStore;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopResult;
import io.github.dirtyredz.herbalwitch.decor.behavior.storage.StorageFurnitureBlock;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionContext;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionPermissionService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Associates fixtures with a shop through a short-lived, server-side linking session the owner arms
 * from the register's management view: the next ordinary click selects a counter, display or board.
 * Only loaded targets in the register's dimension within 16 blocks qualify, with management and
 * world-modification permission at both ends. A fixture belongs to at most one shop; ordinary
 * furniture shelves are refused and keep their own storage.
 */
public final class ShopLinkService {
    public enum Mode { COUNTER, DISPLAY, BOARD, UNLINK }

    private record Armed(UUID shop, Mode mode, long expires) {
    }

    private static final Map<UUID, Armed> ARMED = new HashMap<>();

    private ShopLinkService() {
    }

    public static ShopResult arm(ServerPlayer owner, ShopStore store, UUID shop, Mode mode) {
        ShopRecord record = store.record(shop).orElse(null);
        if (record == null || !record.owner.equals(owner.getUUID())) return ShopResult.refused("not_owner");
        if (record.binding == null || record.retired()) return ShopResult.refused("no_register");
        ARMED.put(owner.getUUID(), new Armed(shop, mode, owner.server.getTickCount() + (long) ShopRules.LINK_SESSION_TICKS));
        return ShopResult.ok("link_armed_" + mode.name().toLowerCase(java.util.Locale.ROOT));
    }

    public static boolean armed(ServerPlayer player) {
        Armed armed = ARMED.get(player.getUUID());
        if (armed != null && player.server.getTickCount() > armed.expires()) {
            ARMED.remove(player.getUUID());
            return false;
        }
        return armed != null;
    }

    public static void forget(UUID player) {
        ARMED.remove(player);
    }

    /** The armed owner clicked {@code pos}: link or unlink it. The session ends either way. */
    public static ShopResult click(ServerPlayer owner, ServerLevel level, BlockPos pos) {
        Armed armed = ARMED.remove(owner.getUUID());
        if (armed == null || owner.server.getTickCount() > armed.expires()) return ShopResult.refused("link_expired");
        ShopStore store = ShopStore.get(owner.server);
        ShopRecord record = store.record(armed.shop()).orElse(null);
        if (record == null || !record.owner.equals(owner.getUUID()) || record.binding == null || !store.writable(record.id)) return ShopResult.refused("unavailable");
        String dimension = ShopBindingService.dimension(level);
        BlockPos register = BlockPos.of(record.binding.pos());
        if (!record.binding.dimension().equals(dimension) || !ShopBindingService.registerLoaded(owner.server, record)) return ShopResult.refused("link_dimension");
        if (register.distSqr(pos) > ShopRules.LINK_RANGE * (double) ShopRules.LINK_RANGE) return ShopResult.refused("link_range");
        if (!level.isLoaded(pos) || !ActionPermissionService.mayModify(ActionContext.of(owner, level, pos))
                || !ActionPermissionService.mayModify(ActionContext.of(owner, level, register))) return ShopResult.refused("permission");
        if (armed.mode() == Mode.UNLINK) return unlink(owner.server, store, record, dimension, pos);
        BlockState state = level.getBlockState(pos);
        ShopLink.Kind kind = kind(state, armed.mode());
        if (kind == null) {
            return state.getBlock() instanceof StorageFurnitureBlock ? ShopResult.refused("link_ordinary_shelf") : ShopResult.refused("link_wrong_target");
        }
        if (store.indexes().fixture(dimension, pos.asLong()).isPresent()) return ShopResult.refused("link_taken");
        if (kind == ShopLink.Kind.COUNTER && pos.equals(register.below())) return ShopResult.refused("link_taken");
        ShopFixtureBlockEntity fixture = null;
        if (kind != ShopLink.Kind.COUNTER) {
            if (!(level.getBlockEntity(pos) instanceof ShopFixtureBlockEntity be) || !be.writable()) return ShopResult.refused("link_wrong_target");
            if (be instanceof ShopDisplayBlockEntity display && display.demonstration()) return ShopResult.refused("link_demonstration");
            fixture = be;
            if (fixture.shop() != null && store.record(fixture.shop()).map(r -> ShopBindingService.linked(r, level, pos, be)).orElse(true)) {
                return ShopResult.refused("link_taken");
            }
        }
        long counters = record.links.stream().filter(l -> l.kind() == ShopLink.Kind.COUNTER).count();
        if (kind == ShopLink.Kind.COUNTER ? counters >= ShopRecord.MAX_EXTRA_COUNTERS : record.links.size() - counters >= ShopRecord.MAX_FIXTURES) {
            return ShopResult.refused("link_limit");
        }
        ShopRecord next = record.next();
        if (fixture != null) fixture.release(); // a fresh instance: an older copy of this entity can never match
        next.links.add(new ShopLink(kind, dimension, pos.asLong(), fixture == null ? null : fixture.instance(), java.util.List.of()));
        if (!store.update(next, true)) return ShopResult.refused("unavailable");
        if (fixture != null) fixture.claim(record.id, 0);
        ShopProjection.publish(owner.server, record.id);
        return ShopResult.ok("linked");
    }

    private static ShopLink.Kind kind(BlockState state, Mode mode) {
        return switch (mode) {
            case COUNTER -> ShopFixtureBlock.isCounter(state) ? ShopLink.Kind.COUNTER : null;
            case DISPLAY -> state.getBlock() instanceof ShopDisplayBlock block
                    ? block.spec() == ShopPieceCatalog.STOCK_SHELF ? ShopLink.Kind.SHELF : ShopLink.Kind.JAR : null;
            case BOARD -> state.getBlock() instanceof ShopOrderBoardBlock ? ShopLink.Kind.BOARD : null;
            case UNLINK -> null;
        };
    }

    private static ShopResult unlink(net.minecraft.server.MinecraftServer server, ShopStore store, ShopRecord record, String dimension, BlockPos pos) {
        if (record.link(dimension, pos.asLong()).isEmpty()) return ShopResult.refused("link_missing");
        ShopRecord next = record.next();
        next.links.removeIf(l -> l.at(dimension, pos.asLong()));
        if (!store.update(next, true)) return ShopResult.refused("unavailable");
        ServerLevel level = ShopBindingService.level(server, dimension);
        if (level != null && level.isLoaded(pos) && level.getBlockEntity(pos) instanceof ShopFixtureBlockEntity fixture
                && record.id.equals(fixture.shop())) ShopProjection.clear(fixture);
        ShopProjection.publish(server, record.id);
        return ShopResult.ok("unlinked");
    }

    /** Owner unlinks by index from the management view. */
    public static ShopResult unlink(ServerPlayer owner, ShopStore store, UUID shop, int index) {
        ShopRecord record = store.record(shop).orElse(null);
        if (record == null || !record.owner.equals(owner.getUUID())) return ShopResult.refused("not_owner");
        if (index < 0 || index >= record.links.size()) return ShopResult.refused("stale_link");
        ShopLink link = record.links.get(index);
        return unlink(owner.server, store, record, link.dimension(), BlockPos.of(link.pos()));
    }

    /** A display or board was removed: its link goes; only its plain item drops. */
    public static void fixtureRemoved(ServerLevel level, BlockPos pos, ShopFixtureBlockEntity fixture) {
        if (fixture.shop() == null) return;
        ShopStore store = ShopStore.get(level.getServer());
        ShopRecord record = store.record(fixture.shop()).orElse(null);
        if (record == null || !ShopBindingService.linked(record, level, pos, fixture) || !store.writable(record.id)) return;
        ShopRecord next = record.next();
        next.links.removeIf(l -> l.at(ShopBindingService.dimension(level), pos.asLong()));
        store.update(next, true);
        ShopProjection.publish(level.getServer(), record.id);
    }
}
