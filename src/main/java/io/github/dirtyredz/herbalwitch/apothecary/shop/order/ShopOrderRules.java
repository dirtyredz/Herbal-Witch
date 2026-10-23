package io.github.dirtyredz.herbalwitch.apothecary.shop.order;

import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopRules;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopOffer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Pure request lifecycle. Times are commerce-clock ticks (running server ticks, immune to time
 * commands). Every transition moves no items and no emeralds; only an ordinary purchase that names
 * an offered request advances its purchased quantity, inside that purchase's own transaction.
 */
public final class ShopOrderRules {
    /** Terminal requests kept for the customer's next visit. */
    public static final int RECENT_TERMINAL = 48;

    public enum Refusal { NONE, QUANTITY, PRICE, CUSTOMER_LIMIT, SHOP_LIMIT, COOLDOWN, STATE, OWNER, INCOMPATIBLE, STALE }

    private ShopOrderRules() {
    }

    public static Refusal canCreate(List<ShopOrder> orders, UUID requester, int quantity, int maxPrice, long now) {
        if (quantity < 1 || quantity > ShopOrder.MAX_QUANTITY) return Refusal.QUANTITY;
        if (maxPrice < ShopOffer.MIN_PRICE || maxPrice > ShopOffer.MAX_PRICE) return Refusal.PRICE;
        if (orders.stream().filter(ShopOrder::active).count() >= ShopRules.ORDERS_PER_SHOP) return Refusal.SHOP_LIMIT;
        if (orders.stream().filter(o -> o.active() && o.requester().equals(requester)).count() >= ShopRules.ORDERS_PER_CUSTOMER) return Refusal.CUSTOMER_LIMIT;
        Optional<Long> last = orders.stream().filter(o -> o.requester().equals(requester)).map(ShopOrder::created).max(Long::compare);
        if (last.isPresent() && now - last.get() < ShopRules.ORDER_COOLDOWN) return Refusal.COOLDOWN;
        return Refusal.NONE;
    }

    public static ShopOrder create(UUID id, UUID requester, String requesterName, String item, ShopOrder.Requirement requirement,
                                   int quantity, int maxPrice, long now, long lifetime) {
        return new ShopOrder(id, 1, requester, requesterName, item, requirement, quantity, 0, maxPrice, now, Math.addExact(now, lifetime),
                ShopOrder.State.OPEN, null, 0);
    }

    public static Refusal canCancel(ShopOrder order, UUID requester) {
        if (!order.requester().equals(requester)) return Refusal.OWNER;
        return order.active() ? Refusal.NONE : Refusal.STATE;
    }

    public static ShopOrder cancel(ShopOrder order) {
        return order.with(ShopOrder.State.CANCELLED, null, 0, order.purchased());
    }

    public static ShopOrder decline(ShopOrder order) {
        return order.with(ShopOrder.State.DECLINED, null, 0, order.purchased());
    }

    /** Whether an offer can answer a request: same product, accepted metadata, lot within the remainder, price within the ceiling. */
    public static Refusal compatible(ShopOrder order, ShopOffer offer) {
        if (!order.active()) return Refusal.STATE;
        if (!offer.enabled() || !offer.key().item().equals(order.item())) return Refusal.INCOMPATIBLE;
        if (order.requirement() == ShopOrder.Requirement.DEFAULT && !offer.key().plain()) return Refusal.INCOMPATIBLE;
        if (offer.lot() > order.remaining()) return Refusal.QUANTITY;
        if (offer.price() > order.maxPrice()) return Refusal.PRICE;
        return Refusal.NONE;
    }

    public static ShopOrder associate(ShopOrder order, ShopOffer offer) {
        return order.with(ShopOrder.State.OFFERED, offer.id(), offer.revision(), order.purchased());
    }

    /** Whether {@code buyer} may buy {@code offer} against this request right now. */
    public static Refusal canFulfil(ShopOrder order, ShopOffer offer, UUID buyer) {
        if (!order.requester().equals(buyer)) return Refusal.OWNER;
        if (order.state() != ShopOrder.State.OFFERED) return Refusal.STATE;
        if (!offer.id().equals(order.offer()) || offer.revision() != order.offerRevision()) return Refusal.STALE;
        return compatible(order, offer);
    }

    public static ShopOrder purchased(ShopOrder order, int quantity) {
        int total = Math.min(order.quantity(), order.purchased() + quantity);
        if (total >= order.quantity()) return order.with(ShopOrder.State.FULFILLED, null, 0, total);
        return order.with(ShopOrder.State.OFFERED, order.offer(), order.offerRevision(), total);
    }

    /**
     * Reconciles requests with the current offers and clock: an offered request whose offer changed or
     * vanished returns to open; an active request past its expiry expires; old terminal requests beyond
     * the retained window are dropped. Returns the same list when nothing changed.
     */
    public static List<ShopOrder> refresh(List<ShopOrder> orders, List<ShopOffer> offers, long now) {
        List<ShopOrder> out = new ArrayList<>();
        boolean changed = false;
        for (ShopOrder order : orders) {
            ShopOrder next = order;
            if (next.active() && now >= next.expires()) next = next.with(ShopOrder.State.EXPIRED, null, 0, next.purchased());
            if (next.state() == ShopOrder.State.OFFERED) {
                ShopOrder offered = next;
                boolean current = offers.stream().anyMatch(o -> o.id().equals(offered.offer()) && o.revision() == offered.offerRevision()
                        && compatible(offered, o) == Refusal.NONE);
                if (!current) next = next.with(ShopOrder.State.OPEN, null, 0, next.purchased());
            }
            changed |= next != order;
            out.add(next);
        }
        List<ShopOrder> terminal = out.stream().filter(o -> !o.active()).sorted(Comparator.comparingLong(ShopOrder::created)).toList();
        if (terminal.size() > RECENT_TERMINAL) {
            out.removeAll(terminal.subList(0, terminal.size() - RECENT_TERMINAL));
            changed = true;
        }
        return changed ? out : orders;
    }
}
