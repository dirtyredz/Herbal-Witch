package io.github.dirtyredz.herbalwitch.decor.behavior.storage.display;

import io.github.dirtyredz.herbalwitch.decor.behavior.storage.FurnitureInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * What a display shelf shows, as clients receive it: at most {@link #MAX_SLOTS} single items by slot,
 * sent in the block entity's update tag only when the contents change (never per tick). Bounded so
 * a shelf can never inflate a chunk packet.
 *
 * @param slots one entry per slot, {@link ItemStack#EMPTY} where nothing stands
 */
public record DisplaySnapshot(List<ItemStack> slots) {
    public static final int MAX_SLOTS = 9;
    private static final String TAG = "Shown";

    public DisplaySnapshot {
        if (slots.size() > MAX_SLOTS) {
            throw new IllegalArgumentException(slots.size() + " display slots, at most " + MAX_SLOTS);
        }
        slots = List.copyOf(slots);
    }

    public static DisplaySnapshot of(FurnitureInventory inventory) {
        List<ItemStack> out = new ArrayList<>();
        for (int i = 0; i < Math.min(MAX_SLOTS, inventory.getSlots()); i++) {
            out.add(inventory.getStackInSlot(i).copyWithCount(Math.min(1, inventory.getStackInSlot(i).getCount())));
        }
        return new DisplaySnapshot(out);
    }

    public void write(CompoundTag tag) {
        ListTag list = new ListTag();
        for (int i = 0; i < slots.size(); i++) {
            if (!slots.get(i).isEmpty()) {
                CompoundTag entry = slots.get(i).save(new CompoundTag());
                entry.putByte("Slot", (byte) i);
                list.add(entry);
            }
        }
        tag.put(TAG, list);
        tag.putByte("Size", (byte) slots.size());
    }

    public static DisplaySnapshot read(CompoundTag tag) {
        int size = Math.max(0, Math.min(MAX_SLOTS, tag.getByte("Size")));
        List<ItemStack> out = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            out.add(ItemStack.EMPTY);
        }
        for (Tag t : tag.getList(TAG, Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) t;
            int slot = entry.getByte("Slot");
            if (slot >= 0 && slot < size) {
                out.set(slot, ItemStack.of(entry));
            }
        }
        return new DisplaySnapshot(out);
    }

    /** Lays the snapshot onto a client-side inventory. */
    public void applyTo(FurnitureInventory inventory) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, i < slots.size() ? slots.get(i) : ItemStack.EMPTY);
        }
    }
}
