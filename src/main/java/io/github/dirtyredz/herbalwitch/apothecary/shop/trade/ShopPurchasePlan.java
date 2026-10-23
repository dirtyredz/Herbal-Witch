package io.github.dirtyredz.herbalwitch.apothecary.shop.trade;

import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopOffer;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStock;

/**
 * Pure simulations of every player transfer against copies of the inventory and stock. A purchase
 * removes the payment first and only then places the lot into the resulting inventory, so a full
 * inventory is refused even when paying would have freed a slot only partly; nothing is ever dropped
 * and no change is given. Emerald blocks and tagged emeralds are not money.
 */
public final class ShopPurchasePlan {
    public enum Refusal { NONE, DISABLED, SOLD_OUT, PAYMENT, INVENTORY_FULL, PROCEEDS_FULL, NO_ROOM, AMOUNT, NOT_TRADEABLE }

    /** The refusal, or the inventory after the transfer. */
    public record Outcome(Refusal refusal, ShopInventoryModel after) {
        static Outcome refuse(Refusal refusal) {
            return new Outcome(refusal, null);
        }

        public boolean allowed() {
            return refusal == Refusal.NONE;
        }
    }

    private ShopPurchasePlan() {
    }

    /** One displayed lot for its price. {@code capacity} is the configured proceeds ceiling. */
    public static Outcome purchase(ShopInventoryModel inventory, ShopOffer offer, ShopStock stock, long proceeds, long capacity, int productLimit) {
        if (!offer.enabled()) return Outcome.refuse(Refusal.DISABLED);
        if (stock.count(offer.key()) < offer.lot()) return Outcome.refuse(Refusal.SOLD_OUT);
        if (inventory.count(ShopInventoryModel.EMERALD) < offer.price()) return Outcome.refuse(Refusal.PAYMENT);
        if (proceeds > capacity - offer.price()) return Outcome.refuse(Refusal.PROCEEDS_FULL);
        ShopInventoryModel after = inventory.copy();
        if (!after.remove(ShopInventoryModel.EMERALD, offer.price())) return Outcome.refuse(Refusal.PAYMENT);
        if (!after.insert(offer.key(), offer.lot(), productLimit)) return Outcome.refuse(Refusal.INVENTORY_FULL);
        return new Outcome(Refusal.NONE, after);
    }

    /** Owner deposit of {@code count} from one main-inventory slot. Eligibility is checked by the caller. */
    public static Outcome deposit(ShopInventoryModel inventory, int slot, int count, ShopStock stock) {
        if (slot < 0 || slot >= ShopInventoryModel.SLOTS) return Outcome.refuse(Refusal.AMOUNT);
        ShopInventoryModel.Held held = inventory.slot(slot);
        if (held.empty() || count < 1 || count > held.count()) return Outcome.refuse(Refusal.AMOUNT);
        if (stock.room(held.key(), held.limit()) < count) return Outcome.refuse(Refusal.NO_ROOM);
        ShopInventoryModel after = inventory.copy();
        after.removeFrom(slot, count);
        return new Outcome(Refusal.NONE, after);
    }

    /** Owner withdrawal of {@code count} from one stock slot into the main inventory. */
    public static Outcome withdraw(ShopInventoryModel inventory, ShopStock stock, int stockSlot, int count) {
        if (stockSlot < 0 || stockSlot >= ShopStock.SLOTS) return Outcome.refuse(Refusal.AMOUNT);
        ShopStock.Slot slot = stock.slot(stockSlot);
        if (slot.empty() || count < 1 || count > slot.count()) return Outcome.refuse(Refusal.AMOUNT);
        ShopInventoryModel after = inventory.copy();
        if (!after.insert(slot.key(), count, slot.limit())) return Outcome.refuse(Refusal.INVENTORY_FULL);
        return new Outcome(Refusal.NONE, after);
    }

    /** Owner withdrawal of {@code count} emeralds from proceeds. */
    public static Outcome proceeds(ShopInventoryModel inventory, long proceeds, int count) {
        if (count < 1 || count > proceeds || count > 64 * ShopInventoryModel.SLOTS) return Outcome.refuse(Refusal.AMOUNT);
        ShopInventoryModel after = inventory.copy();
        if (!after.insert(ShopInventoryModel.EMERALD, count, 64)) return Outcome.refuse(Refusal.INVENTORY_FULL);
        return new Outcome(Refusal.NONE, after);
    }
}
