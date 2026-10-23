package io.github.dirtyredz.herbalwitch.companions.familiars.equipment;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Whole-stack deposit into the 36 carried slots. A full inventory is left byte-for-byte unchanged. */
public final class FamiliarInventoryDeposit {
    private record Change(int slot, ItemStack before, ItemStack after) {}
    private final List<Change> changes;
    private FamiliarInventoryDeposit(List<Change> changes) { this.changes = List.copyOf(changes); }
    public static Optional<FamiliarInventoryDeposit> plan(Inventory inventory, ItemStack stack) {
        int remaining = stack.getCount(); var changes = new ArrayList<Change>();
        for (int pass = 0; pass < 2 && remaining > 0; pass++) for (int slot = 0; slot < 36 && remaining > 0; slot++) {
            var current = inventory.getItem(slot);
            if (pass == 0 && (current.isEmpty() || !ItemStack.isSameItemSameTags(current, stack)) || pass == 1 && !current.isEmpty()) continue;
            int added = Math.min(remaining, Math.min(stack.getMaxStackSize(), inventory.getMaxStackSize()) - current.getCount());
            if (added <= 0) continue;
            var after = stack.copy(); after.setCount(current.getCount() + added); changes.add(new Change(slot, current.copy(), after)); remaining -= added;
        }
        return remaining == 0 ? Optional.of(new FamiliarInventoryDeposit(changes)) : Optional.empty();
    }
    public boolean apply(Inventory inventory) {
        if (changes.stream().anyMatch(c -> !ItemStack.matches(inventory.getItem(c.slot()), c.before()))) return false;
        changes.forEach(c -> inventory.setItem(c.slot(), c.after().copy())); inventory.setChanged(); return true;
    }
}
