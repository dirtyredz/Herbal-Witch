package io.github.dirtyredz.herbalwitch.apothecary.shop.session;

import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopServerConfig;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link.ShopBindingService;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopHistoryEntry;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopLink;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopOffer;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStore;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopTransaction;
import io.github.dirtyredz.herbalwitch.apothecary.shop.order.ShopOrder;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Builds the role-filtered projection a menu shows. Customers see the shop's name, owner, enabled
 * offers with exact sample stacks (so purity, labels and charge read before buying), availability
 * and their own requests only. The owner additionally sees stock, proceeds, settings, bans, links,
 * every request, one 32-entry history page and any unresolved transaction's identity and reason.
 */
public final class ShopSnapshots {
    public static final int HISTORY_PAGE = 32;

    private ShopSnapshots() {
    }

    public static CompoundTag build(ServerPlayer viewer, ShopSession session, int historyPage) {
        CompoundTag out = new CompoundTag();
        out.putString("view", session.view().name());
        ShopStore store = ShopStore.get(viewer.server);
        if (session.shop() == null) return unbound(viewer, store, out);
        ShopRecord record = store.record(session.shop()).orElse(null);
        if (record == null) {
            out.putString("problem", store.problem(session.shop()).orElse("unavailable"));
            return out;
        }
        boolean owner = record.owner.equals(viewer.getUUID());
        out.putBoolean("owner", owner);
        out.putString("name", record.name);
        out.putString("ownerName", record.ownerName);
        out.putString("lifecycle", record.lifecycle.name());
        out.putBoolean("registerLoaded", ShopBindingService.registerLoaded(viewer.server, record));
        out.putBoolean("blocked", record.blocked());
        out.putBoolean("banned", record.bans.contains(viewer.getUUID()) || !record.publicBuying);
        out.put("offers", offers(record, owner));
        if (session.origin() == ShopSession.Origin.DISPLAY) {
            ListTag focus = new ListTag();
            record.link(session.dimension(), session.pos()).ifPresent(l -> l.offers().forEach(id -> focus.add(NbtUtils.createUUID(id))));
            out.put("focus", focus);
        }
        out.put("orders", orders(record, owner ? null : viewer.getUUID()));
        if (owner) owner(record, out, historyPage);
        while (bytes(out) > ShopUpdate.MAX_SNAPSHOT_BYTES && out.getList("history", 10).size() > 0) {
            ListTag history = out.getList("history", 10);
            history.remove(history.size() - 1);
        }
        return out;
    }

    private static CompoundTag unbound(ServerPlayer viewer, ShopStore store, CompoundTag out) {
        out.putBoolean("witch", WitchStatusService.isWitch(viewer));
        out.putInt("owned", store.indexes().owned(viewer.getUUID()).size());
        out.putInt("ownerLimit", ShopServerConfig.current().shopsPerOwner());
        ListTag detached = new ListTag();
        for (UUID id : store.indexes().owned(viewer.getUUID())) {
            store.record(id).filter(r -> r.lifecycle == ShopRecord.Lifecycle.DETACHED).ifPresent(r -> {
                CompoundTag entry = new CompoundTag();
                entry.putUUID("id", r.id);
                entry.putString("name", r.name);
                detached.add(entry);
            });
        }
        out.put("detached", detached);
        return out;
    }

    private static ListTag offers(ShopRecord record, boolean owner) {
        ListTag list = new ListTag();
        boolean selling = record.lifecycle == ShopRecord.Lifecycle.OPEN && !record.blocked();
        for (ShopOffer offer : record.offers) {
            if (!owner && !offer.enabled()) continue;
            CompoundTag entry = new CompoundTag();
            entry.putUUID("id", offer.id());
            entry.putLong("revision", offer.revision());
            ItemStack sample = offer.key().stack(1);
            entry.put("stack", sample.isEmpty() ? new CompoundTag() : sample.save(new CompoundTag()));
            entry.putInt("lot", offer.lot());
            entry.putInt("price", offer.price());
            entry.putBoolean("enabled", offer.enabled());
            entry.putBoolean("available", selling && offer.enabled() && record.stock.count(offer.key()) >= offer.lot());
            if (owner) entry.putInt("stocked", record.stock.count(offer.key()));
            list.add(entry);
        }
        return list;
    }

    private static ListTag orders(ShopRecord record, UUID customer) {
        ListTag list = new ListTag();
        List<ShopOrder> visible = new ArrayList<>(record.orders.stream().filter(o -> customer == null || o.requester().equals(customer)).toList());
        visible.sort((a, b) -> Boolean.compare(b.active(), a.active()) != 0 ? Boolean.compare(b.active(), a.active()) : Long.compare(b.created(), a.created()));
        for (ShopOrder order : visible.subList(0, Math.min(visible.size(), 32))) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("id", order.id());
            entry.putString("item", order.item());
            entry.putString("requester", order.requesterName());
            entry.putString("requirement", order.requirement().name());
            entry.putInt("quantity", order.quantity());
            entry.putInt("purchased", order.purchased());
            entry.putInt("maxPrice", order.maxPrice());
            entry.putString("state", order.state().name());
            if (order.offer() != null) {
                entry.putUUID("offer", order.offer());
                entry.putLong("offerRevision", order.offerRevision());
            }
            list.add(entry);
        }
        return list;
    }

    private static void owner(ShopRecord record, CompoundTag out, int historyPage) {
        ListTag stock = new ListTag();
        for (int i = 0; i < ShopStock.SLOTS; i++) {
            ShopStock.Slot slot = record.stock.slot(i);
            if (slot.empty()) continue;
            ItemStack stack = slot.key().stack(slot.count());
            CompoundTag entry = stack.isEmpty() ? new CompoundTag() : stack.save(new CompoundTag());
            entry.putInt("slot", i);
            entry.putInt("count", slot.count());
            stock.add(entry);
        }
        out.put("stock", stock);
        out.putLong("proceeds", record.proceeds);
        out.putLong("capacity", ShopServerConfig.current().proceedsCapacity());
        out.putBoolean("publicBuying", record.publicBuying);
        out.putBoolean("npcService", record.npcService);
        ListTag bans = new ListTag();
        record.bans.forEach(b -> bans.add(NbtUtils.createUUID(b)));
        out.put("bans", bans);
        ListTag links = new ListTag();
        for (ShopLink link : record.links) {
            CompoundTag entry = new CompoundTag();
            entry.putString("kind", link.kind().name());
            BlockPos pos = BlockPos.of(link.pos());
            entry.putIntArray("pos", new int[] {pos.getX(), pos.getY(), pos.getZ()});
            ListTag offers = new ListTag();
            link.offers().forEach(id -> offers.add(NbtUtils.createUUID(id)));
            entry.put("offers", offers);
            links.add(entry);
        }
        out.put("links", links);
        List<ShopHistoryEntry> history = new ArrayList<>(record.history);
        java.util.Collections.reverse(history);
        int pages = Math.max(1, (history.size() + HISTORY_PAGE - 1) / HISTORY_PAGE);
        int page = Math.max(0, Math.min(historyPage, pages - 1));
        ListTag entries = new ListTag();
        for (ShopHistoryEntry entry : history.subList(page * HISTORY_PAGE, Math.min(history.size(), (page + 1) * HISTORY_PAGE))) {
            CompoundTag tag = new CompoundTag();
            tag.putString("type", entry.type().name());
            tag.putString("actor", entry.actorName());
            tag.putString("product", entry.product());
            tag.putInt("quantity", entry.quantity());
            tag.putLong("emeralds", entry.emeralds());
            tag.putLong("time", entry.time());
            entries.add(tag);
        }
        out.put("history", entries);
        out.putInt("historyPage", page);
        out.putInt("historyPages", pages);
        ListTag recovery = new ListTag();
        for (ShopTransaction t : record.transactions) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("id", t.id());
            entry.putString("kind", t.kind().name());
            entry.putString("phase", t.phase().name());
            entry.putString("reason", t.reason());
            recovery.add(entry);
        }
        out.put("recovery", recovery);
    }

    static int bytes(CompoundTag tag) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            NbtIo.write(tag, new DataOutputStream(out));
            return out.size();
        } catch (IOException impossible) {
            return Integer.MAX_VALUE;
        }
    }
}
