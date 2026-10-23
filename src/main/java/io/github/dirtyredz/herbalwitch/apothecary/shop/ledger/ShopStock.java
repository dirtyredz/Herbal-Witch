package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * A shop's 27 canonical stock slots. Each slot holds one exact product key, a count and that item's
 * stack limit; it is never a vanilla container, item handler or menu slot. Pure: every operation is
 * deterministic (lowest slot first) and either completes or changes nothing.
 */
public final class ShopStock {
    public static final int SLOTS = 27;

    /** One stock slot; {@code key == null} is empty. */
    public record Slot(ShopStackKey key, int count, int limit) {
        public static final Slot EMPTY = new Slot(null, 0, 0);

        public Slot {
            if (key == null ? count != 0 || limit != 0 : count < 1 || limit < 1 || limit > 64 || count > limit) {
                throw new IllegalArgumentException("Invalid stock slot");
            }
        }

        public boolean empty() {
            return key == null;
        }
    }

    private final Slot[] slots = new Slot[SLOTS];

    public ShopStock() {
        java.util.Arrays.fill(slots, Slot.EMPTY);
    }

    public ShopStock copy() {
        ShopStock out = new ShopStock();
        System.arraycopy(slots, 0, out.slots, 0, SLOTS);
        return out;
    }

    public Slot slot(int index) {
        return slots[index];
    }

    public List<Slot> slots() {
        return List.of(slots);
    }

    public int count(ShopStackKey key) {
        int total = 0;
        for (Slot slot : slots) if (key.equals(slot.key())) total += slot.count();
        return total;
    }

    public boolean empty() {
        for (Slot slot : slots) if (!slot.empty()) return false;
        return true;
    }

    /** How many of {@code key} would fit. */
    public int room(ShopStackKey key, int limit) {
        int room = 0;
        for (Slot slot : slots) {
            if (slot.empty()) room += limit;
            else if (key.equals(slot.key())) room += slot.limit() - slot.count();
        }
        return room;
    }

    /** Adds all of {@code count} (topping up matching slots first) or nothing. */
    public boolean insert(ShopStackKey key, int count, int limit) {
        if (count < 1 || limit < 1 || limit > 64 || room(key, limit) < count) return false;
        int left = count;
        for (int i = 0; i < SLOTS && left > 0; i++) {
            Slot slot = slots[i];
            if (!slot.empty() && key.equals(slot.key()) && slot.count() < slot.limit()) {
                int moved = Math.min(left, slot.limit() - slot.count());
                slots[i] = new Slot(key, slot.count() + moved, slot.limit());
                left -= moved;
            }
        }
        for (int i = 0; i < SLOTS && left > 0; i++) {
            if (slots[i].empty()) {
                int moved = Math.min(left, limit);
                slots[i] = new Slot(key, moved, limit);
                left -= moved;
            }
        }
        return true;
    }

    /** Removes exactly {@code count} of {@code key} (lowest slots first) or nothing. */
    public boolean remove(ShopStackKey key, int count) {
        if (count < 1 || count(key) < count) return false;
        int left = count;
        for (int i = 0; i < SLOTS && left > 0; i++) {
            Slot slot = slots[i];
            if (!slot.empty() && key.equals(slot.key())) {
                int moved = Math.min(left, slot.count());
                slots[i] = slot.count() == moved ? Slot.EMPTY : new Slot(key, slot.count() - moved, slot.limit());
                left -= moved;
            }
        }
        return true;
    }

    /** Removes {@code count} from one slot, or nothing. */
    public boolean removeFrom(int index, int count) {
        if (index < 0 || index >= SLOTS) return false;
        Slot slot = slots[index];
        if (slot.empty() || count < 1 || count > slot.count()) return false;
        slots[index] = slot.count() == count ? Slot.EMPTY : new Slot(slot.key(), slot.count() - count, slot.limit());
        return true;
    }

    public ListTag save() {
        ListTag list = new ListTag();
        for (int i = 0; i < SLOTS; i++) {
            Slot slot = slots[i];
            if (slot.empty()) continue;
            CompoundTag entry = slot.key().save();
            entry.putByte("slot", (byte) i);
            entry.putInt("count", slot.count());
            entry.putInt("limit", slot.limit());
            list.add(entry);
        }
        return list;
    }

    public static ShopStock load(ListTag list) {
        if (list.size() > SLOTS) throw new IllegalArgumentException("Too many stock slots");
        ShopStock out = new ShopStock();
        for (Tag tag : list) {
            if (!(tag instanceof CompoundTag entry)) throw new IllegalArgumentException("Malformed stock slot");
            int index = entry.getByte("slot");
            if (index < 0 || index >= SLOTS || !out.slots[index].empty()) throw new IllegalArgumentException("Duplicate or invalid stock slot");
            CompoundTag key = entry.copy();
            key.remove("slot");
            key.remove("count");
            key.remove("limit");
            out.slots[index] = new Slot(ShopStackKey.load(key), entry.getInt("count"), entry.getInt("limit"));
        }
        return out;
    }

    /** Distinct products in slot order, for summaries. */
    public List<ShopStackKey> products() {
        List<ShopStackKey> out = new ArrayList<>();
        for (Slot slot : slots) if (!slot.empty() && !out.contains(slot.key())) out.add(slot.key());
        return out;
    }
}
