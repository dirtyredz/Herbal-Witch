package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction;

import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStackKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * One unresolved economic transfer retained in the shop record between its prepare checkpoint and its
 * commit. While prepared, {@code stockOut} and {@code proceedsOut} are already removed from spendable
 * shop value (reserved here) and {@code stockIn}/{@code proceedsIn} are not yet spendable. The exact
 * before/after player slots and the expected receipt sequence are the recovery evidence; a commit or a
 * proven rollback removes the entry, anything else keeps it quarantined.
 */
public record ShopTransaction(UUID id, Kind kind, Phase phase, UUID actor, String actorName, boolean playerEndpoint, long sequence,
                              List<SlotChange> slots, List<Goods> stockOut, List<Goods> stockIn, long proceedsOut, long proceedsIn,
                              UUID order, int orderQuantity, UUID offer, long time, String reason) {
    public enum Kind { DEPOSIT, WITHDRAW, PURCHASE, PROCEEDS, NPC_SALE }
    public enum Phase { PREPARED, QUARANTINED }

    /** One player inventory slot (0..35) before and after, as saved item NBT ({} for empty). */
    public record SlotChange(int slot, CompoundTag before, CompoundTag after) {
        public SlotChange {
            if (slot < 0 || slot > 35) throw new IllegalArgumentException("Invalid player slot");
            before = before.copy();
            after = after.copy();
        }
    }

    /** Products moving into or out of stock, with their stack limit. */
    public record Goods(ShopStackKey key, int count, int limit) {
        public Goods {
            if (key == null || count < 1 || count > 64 * 36 || limit < 1 || limit > 64) throw new IllegalArgumentException("Invalid goods");
        }
    }

    private static final Set<String> FIELDS = Set.of("id", "kind", "phase", "actor", "actorName", "playerEndpoint", "sequence", "slots",
            "stockOut", "stockIn", "proceedsOut", "proceedsIn", "order", "orderQuantity", "offer", "time", "reason");

    public ShopTransaction {
        slots = List.copyOf(slots);
        stockOut = List.copyOf(stockOut);
        stockIn = List.copyOf(stockIn);
        if (id == null || kind == null || phase == null || actor == null || actorName == null || actorName.length() > 64
                || reason == null || reason.length() > 128 || slots.size() > 36 || stockOut.size() > 27 || stockIn.size() > 27
                || proceedsOut < 0 || proceedsIn < 0 || orderQuantity < 0 || (playerEndpoint ? sequence < 1 : sequence != 0 || !slots.isEmpty())) {
            throw new IllegalArgumentException("Invalid transaction");
        }
    }

    public ShopTransaction quarantine(String why) {
        return new ShopTransaction(id, kind, Phase.QUARANTINED, actor, actorName, playerEndpoint, sequence, slots, stockOut, stockIn,
                proceedsOut, proceedsIn, order, orderQuantity, offer, time, why.length() > 128 ? why.substring(0, 128) : why);
    }

    public CompoundTag save() {
        CompoundTag out = new CompoundTag();
        out.putUUID("id", id);
        out.putString("kind", kind.name());
        out.putString("phase", phase.name());
        out.putUUID("actor", actor);
        out.putString("actorName", actorName);
        out.putBoolean("playerEndpoint", playerEndpoint);
        out.putLong("sequence", sequence);
        ListTag list = new ListTag();
        for (SlotChange change : slots) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("slot", change.slot());
            entry.put("before", change.before());
            entry.put("after", change.after());
            list.add(entry);
        }
        out.put("slots", list);
        out.put("stockOut", goods(stockOut));
        out.put("stockIn", goods(stockIn));
        out.putLong("proceedsOut", proceedsOut);
        out.putLong("proceedsIn", proceedsIn);
        if (order != null) out.putUUID("order", order);
        out.putInt("orderQuantity", orderQuantity);
        if (offer != null) out.putUUID("offer", offer);
        out.putLong("time", time);
        out.putString("reason", reason);
        return out;
    }

    private static ListTag goods(List<Goods> goods) {
        ListTag list = new ListTag();
        for (Goods g : goods) {
            CompoundTag entry = g.key().save();
            entry.putInt("count", g.count());
            entry.putInt("limit", g.limit());
            list.add(entry);
        }
        return list;
    }

    private static List<Goods> goods(ListTag list) {
        List<Goods> out = new ArrayList<>();
        for (Tag tag : list) {
            CompoundTag entry = ((CompoundTag) tag).copy();
            int count = entry.getInt("count"), limit = entry.getInt("limit");
            entry.remove("count");
            entry.remove("limit");
            out.add(new Goods(ShopStackKey.load(entry), count, limit));
        }
        return out;
    }

    public static ShopTransaction load(CompoundTag in) {
        if (!FIELDS.containsAll(in.getAllKeys())) throw new IllegalArgumentException("Malformed transaction");
        List<SlotChange> slots = new ArrayList<>();
        for (Tag tag : in.getList("slots", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) tag;
            slots.add(new SlotChange(entry.getInt("slot"), entry.getCompound("before"), entry.getCompound("after")));
        }
        return new ShopTransaction(in.getUUID("id"), Kind.valueOf(in.getString("kind")), Phase.valueOf(in.getString("phase")),
                in.getUUID("actor"), in.getString("actorName"), in.getBoolean("playerEndpoint"), in.getLong("sequence"), slots,
                goods(in.getList("stockOut", Tag.TAG_COMPOUND)), goods(in.getList("stockIn", Tag.TAG_COMPOUND)),
                in.getLong("proceedsOut"), in.getLong("proceedsIn"), in.hasUUID("order") ? in.getUUID("order") : null,
                in.getInt("orderQuantity"), in.hasUUID("offer") ? in.getUUID("offer") : null, in.getLong("time"), in.getString("reason"));
    }
}
