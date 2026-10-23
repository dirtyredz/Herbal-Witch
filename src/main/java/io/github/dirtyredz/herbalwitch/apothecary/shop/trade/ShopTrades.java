package io.github.dirtyredz.herbalwitch.apothecary.shop.trade;

import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopProductPolicy;
import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopServerConfig;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopOffer;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStackKey;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStore;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopTransaction;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopTransfer;
import io.github.dirtyredz.herbalwitch.apothecary.shop.order.ShopOrder;
import io.github.dirtyredz.herbalwitch.apothecary.shop.order.ShopOrderRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Server adapters for the four player transfers. Each derives quantity, price, payment allocation and
 * the resulting inventory from current server state (never from the client), simulates the whole
 * transfer, and hands the exact slot changes to {@link ShopTransfer}. Access, session and reach checks
 * belong to the caller; these re-check only what concerns value (owner, bans, open state, cursor).
 */
public final class ShopTrades {
    private ShopTrades() {
    }

    /** The player's main inventory as a model. */
    public static ShopInventoryModel model(ServerPlayer player) {
        List<ShopInventoryModel.Held> held = new ArrayList<>();
        for (int i = 0; i < ShopInventoryModel.SLOTS; i++) {
            ItemStack stack = player.getInventory().items.get(i);
            held.add(stack.isEmpty() ? ShopInventoryModel.Held.EMPTY
                    : new ShopInventoryModel.Held(ShopStackKey.of(stack), stack.getCount(), stack.getMaxStackSize()));
        }
        return new ShopInventoryModel(held);
    }

    /** Exact before/after NBT for each changed slot; an unchanged product keeps its real stack data. */
    static List<ShopTransaction.SlotChange> changes(ServerPlayer player, ShopInventoryModel before, ShopInventoryModel after) {
        List<ShopTransaction.SlotChange> out = new ArrayList<>();
        for (int slot : after.changed(before)) {
            ItemStack actual = player.getInventory().items.get(slot);
            ShopInventoryModel.Held next = after.slot(slot);
            ItemStack result;
            if (next.empty()) result = ItemStack.EMPTY;
            else if (!actual.isEmpty() && next.key().equals(ShopStackKey.of(actual))) result = actual.copyWithCount(next.count());
            else result = next.key().stack(next.count());
            out.add(new ShopTransaction.SlotChange(slot, save(actual), save(result)));
        }
        return out;
    }

    static CompoundTag save(ItemStack stack) {
        return stack.isEmpty() ? new CompoundTag() : stack.save(new CompoundTag());
    }

    private static ShopResult result(ShopTransfer.Result result, String success) {
        return switch (result.status()) {
            case SUCCESS -> ShopResult.ok(success, result.transaction());
            case REFUSED -> ShopResult.refused(result.reason());
            case UNAVAILABLE, RECOVERY -> ShopResult.recovery("recovery", result.transaction());
        };
    }

    private static boolean economicActor(ServerPlayer player) {
        return !(player instanceof net.minecraftforge.common.util.FakePlayer) && player.isAlive() && !player.isSpectator()
                && !player.isCreative() && player.containerMenu.getCarried().isEmpty();
    }

    /** One displayed lot of {@code offer} at {@code revision}, optionally against the buyer's offered request. */
    public static ShopResult purchase(ServerPlayer buyer, ShopStore store, UUID shop, UUID offerId, long revision, UUID orderId) {
        ShopRecord record = store.record(shop).orElse(null);
        if (record == null || !store.writable(shop)) return ShopResult.refused("unavailable");
        if (!economicActor(buyer)) return ShopResult.refused("actor");
        if (record.owner.equals(buyer.getUUID())) return ShopResult.refused("own_shop");
        if (record.bans.contains(buyer.getUUID()) || !record.publicBuying) return ShopResult.refused("banned");
        if (record.blocked()) return ShopResult.refused("recovery");
        if (record.lifecycle != ShopRecord.Lifecycle.OPEN) return ShopResult.refused("closed");
        ShopOffer offer = record.offer(offerId).orElse(null);
        if (offer == null || offer.revision() != revision) return ShopResult.refused("stale_offer");
        int orderQuantity = 0;
        if (orderId != null) {
            ShopOrder order = record.order(orderId).orElse(null);
            if (order == null || ShopOrderRules.canFulfil(order, offer, buyer.getUUID()) != ShopOrderRules.Refusal.NONE) return ShopResult.refused("order_stale");
            orderQuantity = offer.lot();
        }
        int limit = offer.key().resolve().map(item -> item.getDefaultInstance().getMaxStackSize()).orElse(0);
        if (limit < 1) return ShopResult.refused("not_tradeable");
        ShopInventoryModel before = model(buyer);
        var outcome = ShopPurchasePlan.purchase(before, offer, record.stock, record.proceeds, ShopServerConfig.current().proceedsCapacity(), limit);
        if (!outcome.allowed()) return ShopResult.refused(outcome.refusal().name().toLowerCase(java.util.Locale.ROOT));
        int stockLimit = record.stock.slots().stream().filter(s -> offer.key().equals(s.key())).mapToInt(ShopStock.Slot::limit).findFirst().orElse(limit);
        var plan = new ShopTransfer.Plan(ShopTransaction.Kind.PURCHASE, offer.id(), changes(buyer, before, outcome.after()),
                List.of(new ShopTransaction.Goods(offer.key(), offer.lot(), stockLimit)), List.of(), 0, offer.price(), orderId, orderQuantity);
        return result(ShopTransfer.player(buyer, store, shop, record.revision, plan), "purchased");
    }

    public static ShopResult deposit(ServerPlayer owner, ShopStore store, UUID shop, int slot, int count) {
        ShopRecord record = store.record(shop).orElse(null);
        if (record == null || !record.owner.equals(owner.getUUID())) return ShopResult.refused("not_owner");
        if (!store.writable(shop)) return ShopResult.refused("unavailable");
        if (!economicActor(owner)) return ShopResult.refused("actor");
        if (record.retired()) return ShopResult.refused("retired");
        if (slot < 0 || slot >= ShopInventoryModel.SLOTS) return ShopResult.refused("amount");
        ItemStack stack = owner.getInventory().items.get(slot);
        var refusal = ShopProductPolicy.check(stack);
        if (refusal != ShopProductPolicy.Refusal.NONE) return ShopResult.refused("product_" + refusal.name().toLowerCase(java.util.Locale.ROOT));
        ShopInventoryModel before = model(owner);
        var outcome = ShopPurchasePlan.deposit(before, slot, count, record.stock);
        if (!outcome.allowed()) return ShopResult.refused(outcome.refusal().name().toLowerCase(java.util.Locale.ROOT));
        var plan = new ShopTransfer.Plan(ShopTransaction.Kind.DEPOSIT, null, changes(owner, before, outcome.after()), List.of(),
                List.of(new ShopTransaction.Goods(ShopStackKey.of(stack), count, stack.getMaxStackSize())), 0, 0, null, 0);
        return result(ShopTransfer.player(owner, store, shop, record.revision, plan), "deposited");
    }

    public static ShopResult withdraw(ServerPlayer owner, ShopStore store, UUID shop, int stockSlot, int count) {
        ShopRecord record = store.record(shop).orElse(null);
        if (record == null || !record.owner.equals(owner.getUUID())) return ShopResult.refused("not_owner");
        if (!store.writable(shop)) return ShopResult.refused("unavailable");
        if (!economicActor(owner)) return ShopResult.refused("actor");
        ShopInventoryModel before = model(owner);
        var outcome = ShopPurchasePlan.withdraw(before, record.stock, stockSlot, count);
        if (!outcome.allowed()) return ShopResult.refused(outcome.refusal().name().toLowerCase(java.util.Locale.ROOT));
        ShopStock.Slot from = record.stock.slot(stockSlot);
        var plan = new ShopTransfer.Plan(ShopTransaction.Kind.WITHDRAW, null, changes(owner, before, outcome.after()),
                List.of(new ShopTransaction.Goods(from.key(), count, from.limit())), List.of(), 0, 0, null, 0);
        return result(ShopTransfer.player(owner, store, shop, record.revision, plan), "withdrawn");
    }

    public static ShopResult proceeds(ServerPlayer owner, ShopStore store, UUID shop, int count) {
        ShopRecord record = store.record(shop).orElse(null);
        if (record == null || !record.owner.equals(owner.getUUID())) return ShopResult.refused("not_owner");
        if (!store.writable(shop)) return ShopResult.refused("unavailable");
        if (!economicActor(owner)) return ShopResult.refused("actor");
        ShopInventoryModel before = model(owner);
        var outcome = ShopPurchasePlan.proceeds(before, record.proceeds, count);
        if (!outcome.allowed()) return ShopResult.refused(outcome.refusal().name().toLowerCase(java.util.Locale.ROOT));
        var plan = new ShopTransfer.Plan(ShopTransaction.Kind.PROCEEDS, null, changes(owner, before, outcome.after()), List.of(), List.of(),
                count, 0, null, 0);
        return result(ShopTransfer.player(owner, store, shop, record.revision, plan), "proceeds_withdrawn");
    }
}
