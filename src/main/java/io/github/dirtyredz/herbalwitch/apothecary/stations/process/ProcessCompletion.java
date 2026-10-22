package io.github.dirtyredz.herbalwitch.apothecary.stations.process;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Capacity arithmetic for completing a job, independent of Minecraft so it is unit-testable: does
 * the result fit the output slot, and do all returned containers fit the return slot together?
 * Nothing is consumed unless both fit; then the station commits everything at once.
 */
public final class ProcessCompletion {
    private ProcessCompletion() {
    }

    /**
     * A stack reduced to what capacity needs: an identity key (item and data; "" when empty), a
     * count and the stack's maximum size.
     */
    public record Amount(String key, int count, int max) {
        public static final Amount EMPTY = new Amount("", 0, 64);

        public boolean empty() {
            return count <= 0 || key.isEmpty();
        }
    }

    /** Whether {@code add} can be merged into {@code slot}. */
    public static boolean fits(Amount slot, Amount add) {
        if (add.empty()) {
            return true;
        }
        if (slot.empty()) {
            return add.count() <= add.max();
        }
        return slot.key().equals(add.key()) && slot.count() + add.count() <= Math.min(slot.max(), add.max());
    }

    /** Adds of the same key combined, in first-seen order. */
    public static List<Amount> merge(List<Amount> adds) {
        Map<String, Amount> merged = new LinkedHashMap<>();
        for (Amount a : adds) {
            if (!a.empty()) {
                merged.merge(a.key(), a, (x, y) -> new Amount(x.key(), x.count() + y.count(), Math.min(x.max(), y.max())));
            }
        }
        return new ArrayList<>(merged.values());
    }

    /** Whether every add fits one slot together: at most one kind, and its total fits. */
    public static boolean fitsAll(Amount slot, List<Amount> adds) {
        List<Amount> merged = merge(adds);
        if (merged.isEmpty()) {
            return true;
        }
        return merged.size() == 1 && fits(slot, merged.get(0));
    }

    /** Result into the output slot and returns into the return slot, all or nothing. */
    public static boolean canComplete(Amount output, Amount result, Amount returnSlot, List<Amount> returns) {
        return fits(output, result) && fitsAll(returnSlot, returns);
    }
}
