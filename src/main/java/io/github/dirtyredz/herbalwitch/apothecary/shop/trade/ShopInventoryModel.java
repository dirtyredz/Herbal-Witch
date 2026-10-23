package io.github.dirtyredz.herbalwitch.apothecary.shop.trade;

import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStackKey;

import java.util.ArrayList;
import java.util.List;

/**
 * A pure model of a player's 36 main-inventory and hotbar slots (armour, offhand, cursor and external
 * inventories never take part). Operations are deterministic, lowest slot first, and complete or
 * change nothing, so a plan can be simulated in full before anything real moves.
 */
public final class ShopInventoryModel {
    public static final int SLOTS = 36;
    public static final ShopStackKey EMERALD = ShopStackKey.of("minecraft:emerald");

    /** One slot; {@code key == null} is empty. */
    public record Held(ShopStackKey key, int count, int limit) {
        public static final Held EMPTY = new Held(null, 0, 0);

        public Held {
            if (key == null ? count != 0 : count < 1 || limit < 1 || count > Math.max(limit, 64)) throw new IllegalArgumentException("Invalid held stack");
        }

        public boolean empty() {
            return key == null;
        }
    }

    private final Held[] slots;

    public ShopInventoryModel(List<Held> slots) {
        if (slots.size() != SLOTS) throw new IllegalArgumentException("Expected 36 slots");
        this.slots = slots.toArray(new Held[0]);
    }

    public ShopInventoryModel copy() {
        return new ShopInventoryModel(List.of(slots));
    }

    public Held slot(int index) {
        return slots[index];
    }

    public int count(ShopStackKey key) {
        int total = 0;
        for (Held held : slots) if (key.equals(held.key())) total += held.count();
        return total;
    }

    /** Removes exactly {@code count} of {@code key}, lowest slot first, or nothing. */
    public boolean remove(ShopStackKey key, int count) {
        if (count < 1 || count(key) < count) return false;
        int left = count;
        for (int i = 0; i < SLOTS && left > 0; i++) {
            if (key.equals(slots[i].key())) {
                int moved = Math.min(left, slots[i].count());
                slots[i] = slots[i].count() == moved ? Held.EMPTY : new Held(key, slots[i].count() - moved, slots[i].limit());
                left -= moved;
            }
        }
        return true;
    }

    /** Removes {@code count} from one slot, or nothing. */
    public boolean removeFrom(int index, int count) {
        Held held = slots[index];
        if (held.empty() || count < 1 || count > held.count()) return false;
        slots[index] = held.count() == count ? Held.EMPTY : new Held(held.key(), held.count() - count, held.limit());
        return true;
    }

    /** Adds all {@code count} (matching partial stacks first, then empty slots) or nothing. Never drops. */
    public boolean insert(ShopStackKey key, int count, int limit) {
        if (count < 1 || limit < 1) return false;
        int room = 0;
        for (Held held : slots) {
            if (held.empty()) room += limit;
            else if (key.equals(held.key())) room += Math.max(0, held.limit() - held.count());
        }
        if (room < count) return false;
        int left = count;
        for (int i = 0; i < SLOTS && left > 0; i++) {
            Held held = slots[i];
            if (!held.empty() && key.equals(held.key()) && held.count() < held.limit()) {
                int moved = Math.min(left, held.limit() - held.count());
                slots[i] = new Held(key, held.count() + moved, held.limit());
                left -= moved;
            }
        }
        for (int i = 0; i < SLOTS && left > 0; i++) {
            if (slots[i].empty()) {
                int moved = Math.min(left, limit);
                slots[i] = new Held(key, moved, limit);
                left -= moved;
            }
        }
        return true;
    }

    /** Slots that differ from {@code before}, ascending. */
    public List<Integer> changed(ShopInventoryModel before) {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < SLOTS; i++) if (!slots[i].equals(before.slots[i])) out.add(i);
        return out;
    }
}
