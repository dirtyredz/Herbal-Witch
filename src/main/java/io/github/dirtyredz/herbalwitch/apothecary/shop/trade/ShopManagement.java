package io.github.dirtyredz.herbalwitch.apothecary.shop.trade;

import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link.ShopBindingService;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopHistoryEntry;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopLink;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopOffer;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStore;
import io.github.dirtyredz.herbalwitch.apothecary.shop.order.ShopOrderRules;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Owner-only management that moves no value: name, open/close, public buying, villager service, bans,
 * offers and display assignments. Offers are built from a stock sample's exact key; any edit makes a
 * new revision and returns stale requests to open. Lowering limits never deletes anything.
 */
public final class ShopManagement {
    private ShopManagement() {
    }

    /** Applies an owner edit to the next revision and saves it (ordinary verified save; no value moves). */
    static ShopResult edit(ServerPlayer owner, ShopStore store, UUID shop, Function<ShopRecord, String> change) {
        ShopRecord record = store.record(shop).orElse(null);
        if (record == null || !record.owner.equals(owner.getUUID())) return ShopResult.refused("not_owner");
        if (record.retired()) return ShopResult.refused("retired");
        if (!store.writable(shop)) return ShopResult.refused("unavailable");
        ShopRecord next = record.next();
        String refusal = change.apply(next);
        if (refusal != null) return ShopResult.refused(refusal);
        next.orders = new ArrayList<>(ShopOrderRules.refresh(next.orders, next.offers, io.github.dirtyredz.herbalwitch.apothecary.shop.customer.demand.NpcDemandService.now(owner.server)));
        if (!store.update(next, true)) return ShopResult.refused("unavailable");
        return ShopResult.ok("saved");
    }

    public static ShopResult rename(ServerPlayer owner, ShopStore store, UUID shop, String name) {
        return edit(owner, store, shop, r -> {
            if (!ShopRecord.validName(name)) return "name";
            r.name = name;
            return null;
        });
    }

    public static ShopResult open(ServerPlayer owner, ShopStore store, UUID shop) {
        return edit(owner, store, shop, r -> {
            if (r.blocked()) return "recovery";
            if (r.binding == null || !ShopBindingService.registerLoaded(owner.server, r)) return "no_register";
            boolean stocked = r.offers.stream().anyMatch(o -> o.enabled() && r.stock.count(o.key()) >= o.lot());
            if (!stocked) return "no_stocked_offer";
            if (r.lifecycle == ShopRecord.Lifecycle.OPEN) return "already_open";
            r.lifecycle = ShopRecord.Lifecycle.OPEN;
            r.record(new ShopHistoryEntry(UUID.randomUUID(), owner.serverLevel().getGameTime(), owner.getUUID(), r.ownerName,
                    ShopHistoryEntry.Type.OPENED, "", 0, 0));
            return null;
        });
    }

    public static ShopResult close(ServerPlayer owner, ShopStore store, UUID shop) {
        return edit(owner, store, shop, r -> {
            if (r.lifecycle != ShopRecord.Lifecycle.OPEN) return "not_open";
            r.lifecycle = ShopRecord.Lifecycle.CLOSED;
            r.record(new ShopHistoryEntry(UUID.randomUUID(), owner.serverLevel().getGameTime(), owner.getUUID(), r.ownerName,
                    ShopHistoryEntry.Type.CLOSED, "", 0, 0));
            return null;
        });
    }

    public static ShopResult publicBuying(ServerPlayer owner, ShopStore store, UUID shop, boolean enabled) {
        return edit(owner, store, shop, r -> {
            r.publicBuying = enabled;
            return null;
        });
    }

    public static ShopResult villagers(ServerPlayer owner, ShopStore store, UUID shop, boolean enabled) {
        return edit(owner, store, shop, r -> {
            r.npcService = enabled;
            return null;
        });
    }

    /** Bans an online player by name (their UUID is kept; names are never authority). */
    public static ShopResult ban(ServerPlayer owner, ShopStore store, UUID shop, String name) {
        ServerPlayer target = owner.server.getPlayerList().getPlayerByName(name);
        if (target == null) return ShopResult.refused("player_unknown");
        if (target.getUUID().equals(owner.getUUID())) return ShopResult.refused("ban_self");
        return edit(owner, store, shop, r -> {
            if (r.bans.contains(target.getUUID())) return "already_banned";
            if (r.bans.size() >= ShopRecord.MAX_BANS) return "ban_limit";
            r.bans.add(target.getUUID());
            return null;
        });
    }

    public static ShopResult unban(ServerPlayer owner, ShopStore store, UUID shop, UUID player) {
        return edit(owner, store, shop, r -> r.bans.remove(player) ? null : "not_banned");
    }

    /** Creates ({@code offerId == null}) or edits an offer from the product in one stock slot. */
    public static ShopResult saveOffer(ServerPlayer owner, ShopStore store, UUID shop, UUID offerId, int stockSlot, int lot, int price, boolean enabled) {
        return edit(owner, store, shop, r -> {
            if (stockSlot < 0 || stockSlot >= ShopStock.SLOTS || r.stock.slot(stockSlot).empty()) return "no_sample";
            ShopStock.Slot sample = r.stock.slot(stockSlot);
            if (lot < 1 || lot > Math.min(ShopOffer.MAX_LOT, sample.limit())) return "lot";
            if (price < ShopOffer.MIN_PRICE || price > ShopOffer.MAX_PRICE) return "price";
            if (offerId == null) {
                if (r.offers.size() >= ShopRecord.MAX_OFFERS) return "offer_limit";
                r.offers.add(new ShopOffer(UUID.randomUUID(), 1, sample.key(), lot, price, enabled));
                return null;
            }
            ShopOffer existing = r.offer(offerId).orElse(null);
            if (existing == null) return "stale_offer";
            r.offers.set(r.offers.indexOf(existing), existing.edit(sample.key(), lot, price, enabled));
            return null;
        });
    }

    public static ShopResult removeOffer(ServerPlayer owner, ShopStore store, UUID shop, UUID offerId) {
        return edit(owner, store, shop, r -> {
            if (!r.offers.removeIf(o -> o.id().equals(offerId))) return "stale_offer";
            r.links.replaceAll(link -> link.offers().contains(offerId) ? link.withOffers(link.offers().stream().filter(o -> !o.equals(offerId)).toList()) : link);
            return null;
        });
    }

    /** Selects which offer a display slot shows ({@code offerId == null} clears it). Moves no inventory. */
    public static ShopResult assign(ServerPlayer owner, ShopStore store, UUID shop, int linkIndex, int slot, UUID offerId) {
        return edit(owner, store, shop, r -> {
            if (linkIndex < 0 || linkIndex >= r.links.size()) return "stale_link";
            ShopLink link = r.links.get(linkIndex);
            int slots = ShopLink.slots(link.kind());
            if (slot < 0 || slot >= slots) return "stale_link";
            if (offerId != null && r.offer(offerId).isEmpty()) return "stale_offer";
            List<UUID> offers = new ArrayList<>(link.offers());
            while (offers.size() <= slot && offers.size() < slots) offers.add(null);
            offers.set(slot, offerId);
            r.links.set(linkIndex, link.withOffers(offers.stream().filter(java.util.Objects::nonNull).distinct().toList()));
            return null;
        });
    }

}
