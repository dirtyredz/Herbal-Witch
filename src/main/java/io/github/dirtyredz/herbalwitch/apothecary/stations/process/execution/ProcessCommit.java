package io.github.dirtyredz.herbalwitch.apothecary.stations.process.execution;

import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessCompletion;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessLayout;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * One batch's final inventory change, prepared but not yet applied: consume the reserved counts and
 * add the result and every returned container, all or nothing. {@link #fits} is pure capacity
 * arithmetic; {@link #apply} keeps a copy of every slot so {@link #rollback} can put the inventory
 * back exactly when something after it (an energy debit) fails synchronously. Owns no witch state.
 */
public final class ProcessCommit {
    private final ProcessInventory inventory;
    private final ProcessLayout layout;
    private final int[] counts;
    private final ItemStack result;
    private final List<ItemStack> returns;
    private ItemStack[] backup;

    public ProcessCommit(ProcessInventory inventory, ProcessLayout layout, int[] counts, ItemStack result, List<ItemStack> returns) {
        this.inventory = inventory;
        this.layout = layout;
        this.counts = counts.clone();
        this.result = result.copy();
        this.returns = returns.stream().map(ItemStack::copy).toList();
    }

    /** Whether the result fits the output slot and all returns fit the return slot together. */
    public boolean fits() {
        List<ProcessCompletion.Amount> adds = returns.stream().map(ProcessInventory::amountOf).toList();
        if (layout.output() < 0 || (!adds.isEmpty() && layout.returns() < 0)) {
            return false;
        }
        ProcessCompletion.Amount returnSlot = layout.returns() < 0 ? ProcessCompletion.Amount.EMPTY : inventory.amount(layout.returns());
        return ProcessCompletion.canComplete(inventory.amount(layout.output()), ProcessInventory.amountOf(result), returnSlot, adds);
    }

    /** Consumes and adds everything, once; call only after {@link #fits}. */
    public void apply() {
        if (backup != null) {
            throw new IllegalStateException("a commit applies once");
        }
        backup = new ItemStack[inventory.getSlots()];
        for (int slot = 0; slot < backup.length; slot++) {
            backup[slot] = inventory.getStackInSlot(slot).copy();
        }
        for (int slot = 0; slot < counts.length; slot++) {
            if (counts[slot] > 0) {
                inventory.consume(slot, counts[slot]);
            }
        }
        inventory.insertInternal(layout.output(), result.copy());
        for (ItemStack r : returns) {
            inventory.insertInternal(layout.returns(), r.copy());
        }
    }

    /** Puts every slot back as it was before {@link #apply}; nothing if it never applied. */
    public void rollback() {
        if (backup == null) {
            return;
        }
        for (int slot = 0; slot < backup.length; slot++) {
            if (!ItemStack.matches(inventory.getStackInSlot(slot), backup[slot])) {
                inventory.setStackInSlot(slot, backup[slot].copy());
            }
        }
        backup = null;
    }
}
