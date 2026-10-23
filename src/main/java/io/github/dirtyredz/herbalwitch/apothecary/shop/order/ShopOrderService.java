package io.github.dirtyredz.herbalwitch.apothecary.shop.order;

import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopProductPolicy;
import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopServerConfig;
import io.github.dirtyredz.herbalwitch.apothecary.shop.customer.demand.NpcDemandService;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopOffer;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStore;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopResult;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Server adapter for nonbinding requests: customers create and cancel their own, the owner declines or
 * associates a compatible existing offer. No transition moves money or items; refusal codes name the
 * broken rule. Request times use the commerce clock.
 */
public final class ShopOrderService {
    private ShopOrderService() {
    }

    private static String code(ShopOrderRules.Refusal refusal) {
        return "order_" + refusal.name().toLowerCase(java.util.Locale.ROOT);
    }

    private static ShopResult save(ShopStore store, ShopRecord next, long now, String success) {
        next.orders = new ArrayList<>(ShopOrderRules.refresh(next.orders, next.offers, now));
        return store.update(next, true) ? ShopResult.ok(success) : ShopResult.refused("unavailable");
    }

    public static ShopResult create(ServerPlayer customer, ShopStore store, UUID shop, String item, boolean plainOnly, int quantity, int maxPrice) {
        ShopRecord record = store.record(shop).orElse(null);
        if (record == null || record.retired() || !store.writable(shop)) return ShopResult.refused("unavailable");
        if (record.owner.equals(customer.getUUID())) return ShopResult.refused("own_shop");
        if (record.bans.contains(customer.getUUID()) || !record.publicBuying) return ShopResult.refused("banned");
        if (customer instanceof net.minecraftforge.common.util.FakePlayer) return ShopResult.refused("actor");
        if (!ShopProductPolicy.tradeable(item)) return ShopResult.refused("product_not_tradeable");
        long now = NpcDemandService.now(customer.server);
        ShopRecord next = record.next();
        next.orders = new ArrayList<>(ShopOrderRules.refresh(next.orders, next.offers, now));
        var refusal = ShopOrderRules.canCreate(next.orders, customer.getUUID(), quantity, maxPrice, now);
        if (refusal != ShopOrderRules.Refusal.NONE) return ShopResult.refused(code(refusal));
        String name = customer.getGameProfile().getName();
        next.orders.add(ShopOrderRules.create(UUID.randomUUID(), customer.getUUID(), name.length() > 64 ? name.substring(0, 64) : name, item,
                plainOnly ? ShopOrder.Requirement.DEFAULT : ShopOrder.Requirement.ANY, quantity, maxPrice, now, ShopServerConfig.current().orderLifetime()));
        return save(store, next, now, "order_created");
    }

    public static ShopResult cancel(ServerPlayer customer, ShopStore store, UUID shop, UUID order) {
        ShopRecord record = store.record(shop).orElse(null);
        if (record == null || !store.writable(shop)) return ShopResult.refused("unavailable");
        ShopOrder current = record.order(order).orElse(null);
        if (current == null) return ShopResult.refused("order_stale");
        var refusal = ShopOrderRules.canCancel(current, customer.getUUID());
        if (refusal != ShopOrderRules.Refusal.NONE) return ShopResult.refused(code(refusal));
        ShopRecord next = record.next();
        next.orders.set(next.orders.indexOf(current), ShopOrderRules.cancel(current));
        return save(store, next, NpcDemandService.now(customer.server), "order_cancelled");
    }

    public static ShopResult decline(ServerPlayer owner, ShopStore store, UUID shop, UUID order) {
        ShopRecord record = store.record(shop).orElse(null);
        if (record == null || !record.owner.equals(owner.getUUID())) return ShopResult.refused("not_owner");
        if (!store.writable(shop)) return ShopResult.refused("unavailable");
        ShopOrder current = record.order(order).orElse(null);
        if (current == null || !current.active()) return ShopResult.refused("order_stale");
        ShopRecord next = record.next();
        next.orders.set(next.orders.indexOf(current), ShopOrderRules.decline(current));
        return save(store, next, NpcDemandService.now(owner.server), "order_declined");
    }

    public static ShopResult associate(ServerPlayer owner, ShopStore store, UUID shop, UUID order, UUID offerId) {
        ShopRecord record = store.record(shop).orElse(null);
        if (record == null || !record.owner.equals(owner.getUUID())) return ShopResult.refused("not_owner");
        if (!store.writable(shop)) return ShopResult.refused("unavailable");
        ShopOrder current = record.order(order).orElse(null);
        ShopOffer offer = record.offer(offerId).orElse(null);
        if (current == null || offer == null) return ShopResult.refused("order_stale");
        var refusal = ShopOrderRules.compatible(current, offer);
        if (refusal != ShopOrderRules.Refusal.NONE) return ShopResult.refused(code(refusal));
        ShopRecord next = record.next();
        next.orders.set(next.orders.indexOf(current), ShopOrderRules.associate(current, offer));
        return save(store, next, NpcDemandService.now(owner.server), "order_offered");
    }
}
