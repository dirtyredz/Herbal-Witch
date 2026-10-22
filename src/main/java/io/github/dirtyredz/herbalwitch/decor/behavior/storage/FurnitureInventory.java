package io.github.dirtyredz.herbalwitch.decor.behavior.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * A piece's one inventory: {@link StorageSpec#slots()} slots of at most {@link StorageSpec#slotLimit()}
 * items. Saved under a schema version so a later layout change can migrate it; data with an unknown
 * version, or none at all (a shelf placed before 0.4.0), loads as an empty inventory, never as
 * free items. Contents changes are reported to the owner (save, display sync).
 */
public class FurnitureInventory extends ItemStackHandler {
    public static final int SCHEMA = 1;
    static final String SCHEMA_TAG = "Schema";
    static final String ITEMS_TAG = "Items";

    private final StorageSpec spec;
    private final Runnable changed;

    public FurnitureInventory(StorageSpec spec, Runnable changed) {
        super(spec.slots());
        this.spec = spec;
        this.changed = changed;
    }

    public StorageSpec spec() {
        return spec;
    }

    @Override
    public int getSlotLimit(int slot) {
        return spec.slotLimit();
    }

    @Override
    protected void onContentsChanged(int slot) {
        changed.run();
    }

    /** The versioned save. */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(SCHEMA_TAG, SCHEMA);
        tag.put(ITEMS_TAG, serializeNBT());
        return tag;
    }

    /** Loads a versioned save; anything else leaves the inventory empty. */
    public void load(CompoundTag tag) {
        for (int i = 0; i < getSlots(); i++) {
            stacks.set(i, ItemStack.EMPTY);
        }
        if (tag.getInt(SCHEMA_TAG) != SCHEMA || !tag.contains(ITEMS_TAG)) {
            return;
        }
        CompoundTag items = tag.getCompound(ITEMS_TAG);
        int saved = items.getInt("Size");
        items.putInt("Size", getSlots()); // never resize: extra saved slots are ignored
        deserializeNBT(items);
        items.putInt("Size", saved);
        for (int i = 0; i < getSlots(); i++) {
            ItemStack stack = stacks.get(i);
            if (stack.getCount() > getSlotLimit(i)) {
                stack.setCount(getSlotLimit(i));
            }
        }
    }

    /** Every non-empty stack, copied. */
    public List<ItemStack> contents() {
        List<ItemStack> out = new ArrayList<>();
        for (int i = 0; i < getSlots(); i++) {
            if (!getStackInSlot(i).isEmpty()) {
                out.add(getStackInSlot(i).copy());
            }
        }
        return out;
    }

    /** Empties the inventory, returning what it held (for dropping it exactly once). */
    public List<ItemStack> takeAll() {
        List<ItemStack> out = contents();
        for (int i = 0; i < getSlots(); i++) {
            stacks.set(i, ItemStack.EMPTY);
        }
        return out;
    }
}
