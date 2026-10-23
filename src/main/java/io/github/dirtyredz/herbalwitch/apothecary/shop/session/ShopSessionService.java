package io.github.dirtyredz.herbalwitch.apothecary.shop.session;

import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopServerConfig;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopFixtureBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link.ShopBindingService;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link.ShopLinkService;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link.ShopProjection;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStore;
import io.github.dirtyredz.herbalwitch.apothecary.shop.order.ShopOrderService;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopManagement;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopResult;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopTrades;
import io.github.dirtyredz.herbalwitch.platform.network.RateLimit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Opens commerce menus, validates every request against its session (menu id, nonce, next sequence,
 * presence, reach, sight, permission, rate) and dispatches it to the owning service. A retransmitted
 * request returns the previous result without acting again; a stale or reordered one acts never.
 * Results and fresh projections go back to the requester only.
 */
public final class ShopSessionService {
    private static final Map<UUID, ShopSession> SESSIONS = new HashMap<>();
    private static final Map<UUID, Integer> PAGES = new HashMap<>();
    private static final RateLimit MUTATIONS = new RateLimit(5, 20);
    private static final RateLimit READS = new RateLimit(10, 20);
    private static BiConsumer<ServerPlayer, ShopUpdate> sender = ShopPackets::send;

    private ShopSessionService() {
    }

    /** GameTest seam: capture updates for players without a network connection. */
    public static void observe(BiConsumer<ServerPlayer, ShopUpdate> observer) {
        sender = observer == null ? ShopPackets::send : (player, update) -> {
            observer.accept(player, update);
            ShopPackets.send(player, update);
        };
    }

    public static ShopSession session(ServerPlayer player) {
        return SESSIONS.get(player.getUUID());
    }

    /** Opens a menu for {@code shop} (null: an unbound register) from a fixture or counter. */
    public static void open(ServerPlayer player, ShopSession.View view, UUID shop, ShopSession.Origin origin, ServerLevel level, BlockPos pos) {
        ShopStore store = ShopStore.get(player.server);
        ShopRecord record = shop == null ? null : store.record(shop).orElse(null);
        long generation = record == null || record.binding == null ? 0 : record.binding.generation();
        boolean blocked = record != null && record.blocked();
        UUID nonce = UUID.randomUUID();
        Component title = Component.translatable(ShopMenu.TITLE);
        NetworkHooks.openScreen(player, new SimpleMenuProvider((id, inventory, actor) -> {
            ShopSession session = new ShopSession(player.getUUID(), id, nonce, shop, origin, view, ShopBindingService.dimension(level), pos.asLong(),
                    generation, ShopServerConfig.revision(), blocked);
            SESSIONS.put(player.getUUID(), session);
            return new ShopMenu(id, session);
        }, title), buf -> {
            buf.writeEnum(view);
            buf.writeUUID(nonce);
        });
        ShopSession session = SESSIONS.get(player.getUUID());
        if (session != null && player.containerMenu instanceof ShopMenu menu && menu.session() == session) send(player, session, null);
    }

    public static boolean valid(ServerPlayer player, ShopSession session) {
        if (SESSIONS.get(player.getUUID()) != session || !ShopAccess.present(player, session)) return false;
        if (session.rulesRevision() != ShopServerConfig.revision()) return false;
        if (session.shop() == null) return true;
        ShopRecord record = ShopStore.get(player.server).record(session.shop()).orElse(null);
        if (record == null || record.retired() || record.blocked() != session.blocked()) return false;
        if (session.origin() == ShopSession.Origin.REGISTER) {
            return record.binding != null && record.binding.dimension().equals(session.dimension()) && record.binding.pos() == session.pos()
                    && record.binding.generation() == session.generation();
        }
        long generation = record.binding == null ? 0 : record.binding.generation();
        return generation == session.generation() && record.link(session.dimension(), session.pos()).isPresent();
    }

    public static void closed(ServerPlayer player, ShopSession session) {
        if (SESSIONS.get(player.getUUID()) == session) {
            SESSIONS.remove(player.getUUID());
            PAGES.remove(player.getUUID());
        }
    }

    public static void logout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        SESSIONS.remove(id);
        PAGES.remove(id);
        MUTATIONS.forget(id);
        READS.forget(id);
        ShopLinkService.forget(id);
    }

    public static void stopped(MinecraftServer server) {
        SESSIONS.clear();
        PAGES.clear();
    }

    /** Handles one request from its real sender. */
    public static void request(ServerPlayer sender, ShopRequest request) {
        ShopSession session = SESSIONS.get(sender.getUUID());
        if (session == null || !session.matches(request.container(), request.nonce()) || !(sender.containerMenu instanceof ShopMenu menu)
                || menu.session() != session) return;
        RateLimit gate = request.action().mutation() ? MUTATIONS : READS;
        if (!gate.allow(sender.getUUID(), sender.server.getTickCount())) {
            send(sender, session, ShopResult.refused("rate"));
            return;
        }
        switch (session.arrive(request.sequence())) {
            case REPLAY -> send(sender, session, session.last());
            case STALE -> send(sender, session, ShopResult.refused("stale_request"));
            case NEW -> {
                ShopResult result;
                if (!valid(sender, session)) result = ShopResult.refused("session");
                else if (!ShopAccess.sight(sender, sender.level(), BlockPos.of(session.pos()))) result = ShopResult.refused("sight");
                else result = dispatch(sender, session, request);
                session.answered(result);
                if (result.success() && session.shop() != null) ShopProjection.publish(sender.server, session.shop());
                if (sender.containerMenu instanceof ShopMenu open && open.session() == session) send(sender, session, result);
            }
        }
    }

    private static ShopResult dispatch(ServerPlayer player, ShopSession session, ShopRequest r) {
        ShopStore store = ShopStore.get(player.server);
        UUID shop = session.shop();
        if (shop == null) return unbound(player, session, r);
        return switch (r.action()) {
            case REFRESH -> ShopResult.ok("refreshed");
            case HISTORY_PAGE -> {
                PAGES.put(player.getUUID(), Math.max(0, Math.min(r.a(), 64)));
                yield ShopResult.ok("refreshed");
            }
            case CREATE, RECONNECT -> ShopResult.refused("already_bound");
            case RENAME -> ShopManagement.rename(player, store, shop, r.text());
            case OPEN -> ShopManagement.open(player, store, shop);
            case CLOSE -> ShopManagement.close(player, store, shop);
            case RETIRE -> ShopBindingService.retire(player, shop);
            case PUBLIC -> ShopManagement.publicBuying(player, store, shop, r.flag());
            case NPC -> ShopManagement.villagers(player, store, shop, r.flag());
            case BAN -> ShopManagement.ban(player, store, shop, r.text());
            case UNBAN -> ShopManagement.unban(player, store, shop, r.target());
            case DEPOSIT -> ShopTrades.deposit(player, store, shop, r.a(), r.b());
            case WITHDRAW -> ShopTrades.withdraw(player, store, shop, r.a(), r.b());
            case PROCEEDS -> ShopTrades.proceeds(player, store, shop, r.a());
            case OFFER_SAVE -> ShopManagement.saveOffer(player, store, shop, r.targetOrNull(), r.a(), r.b(), r.c(), r.flag());
            case OFFER_REMOVE -> ShopManagement.removeOffer(player, store, shop, r.target());
            case BUY -> ShopTrades.purchase(player, store, shop, r.target(), r.revision(), r.target2OrNull());
            case LINK -> r.a() < 0 || r.a() >= ShopLinkService.Mode.values().length ? ShopResult.refused("stale_link")
                    : linkThenClose(player, ShopLinkService.arm(player, store, shop, ShopLinkService.Mode.values()[r.a()]));
            case UNLINK -> ShopLinkService.unlink(player, store, shop, r.a());
            case DISPLAY_ASSIGN -> ShopManagement.assign(player, store, shop, r.a(), r.b(), r.targetOrNull());
            case ORDER_CREATE -> ShopOrderService.create(player, store, shop, r.text(), r.flag(), r.a(), r.b());
            case ORDER_CANCEL -> ShopOrderService.cancel(player, store, shop, r.target());
            case ORDER_DECLINE -> ShopOrderService.decline(player, store, shop, r.target());
            case ORDER_OFFER -> ShopOrderService.associate(player, store, shop, r.target(), r.target2());
        };
    }

    /** Arming a link closes the menu so the owner's next click selects the fixture. */
    private static ShopResult linkThenClose(ServerPlayer player, ShopResult armed) {
        if (armed.success()) {
            player.displayClientMessage(Component.translatable(armed.key()), true);
            player.server.execute(player::closeContainer);
        }
        return armed;
    }

    private static ShopResult unbound(ServerPlayer player, ShopSession session, ShopRequest r) {
        if (session.origin() != ShopSession.Origin.REGISTER) return ShopResult.refused("unavailable");
        if (r.action() == ShopRequest.Action.REFRESH) return ShopResult.ok("refreshed");
        ServerLevel level = player.serverLevel();
        BlockPos pos = BlockPos.of(session.pos());
        if (!(level.getBlockEntity(pos) instanceof ShopFixtureBlockEntity register)) return ShopResult.refused("unavailable");
        ShopResult result = switch (r.action()) {
            case CREATE -> ShopBindingService.create(player, level, pos, register, r.text());
            case RECONNECT -> ShopBindingService.reconnect(player, level, pos, register, r.target());
            default -> ShopResult.refused("unbound");
        };
        if (result.success()) player.server.execute(() -> {
            player.closeContainer();
            io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link.ShopFixtureHooks.openRegister(player, level, pos, register);
        });
        return result;
    }

    static void send(ServerPlayer player, ShopSession session, ShopResult result) {
        var snapshot = ShopSnapshots.build(player, session, PAGES.getOrDefault(player.getUUID(), 0));
        sender.accept(player, new ShopUpdate(session.container(), session.nonce(), session.expected() - 1, snapshot, result));
    }

    /** Publication listener: pushes a fresh projection to every open session of that shop. */
    public static void published(MinecraftServer server, UUID shop) {
        for (ShopSession session : SESSIONS.values()) {
            if (!shop.equals(session.shop())) continue;
            ServerPlayer player = server.getPlayerList().getPlayer(session.player());
            if (player != null && player.containerMenu instanceof ShopMenu menu && menu.session() == session) send(player, session, null);
        }
    }
}
