package io.github.dirtyredz.herbalwitch.stations.process;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.BiPredicate;
import java.util.stream.IntStream;

/**
 * A station's authoritative inventory. Every slot has a role; ingredients reserved by a running
 * job stay in their own slots and the reserved quantity can be neither extracted (menus and
 * automation both go through {@link #extractItem}) nor swapped for another item. Unreserved excess
 * stays accessible. Output and return slots never accept insertion from outside; the station fills
 * them through {@link #insertInternal}. A revision counter lets the station re-match recipes only
 * when contents actually change.
 */
public class ProcessInventory extends ItemStackHandler {
    /** What a slot is for, which also decides its automation side. */
    public enum Role {
        /** Recipe ingredients; inserted from above. */
        INPUT,
        /** A base liquid such as a water bottle; inserted from the sides. */
        BASE,
        /** A package such as a bowl or bottle; inserted from the sides. */
        PACKAGING,
        /** Fuel; inserted from the sides. */
        FUEL,
        /** The product; extracted from below. */
        OUTPUT,
        /** Returned containers; extracted from below. */
        RETURN;

        public boolean extractable() {
            return this == OUTPUT || this == RETURN;
        }
    }

    private final Role[] roles;
    private final int[] reserved;
    private long revision;
    private Runnable onChanged = () -> {
    };
    private BiPredicate<Role, ItemStack> accepts = (role, stack) -> true;

    public ProcessInventory(Role... roles) {
        super(roles.length);
        this.roles = roles.clone();
        this.reserved = new int[roles.length];
    }

    public void onChanged(Runnable listener) {
        this.onChanged = listener;
    }

    /** Which items a role accepts from players and automation (e.g. only recipe ingredients). */
    public void accepts(BiPredicate<Role, ItemStack> filter) {
        this.accepts = filter;
    }

    public Role role(int slot) {
        return roles[slot];
    }

    public int[] slots(Role role) {
        return IntStream.range(0, roles.length).filter(i -> roles[i] == role).toArray();
    }

    public long revision() {
        return revision;
    }

    public int reserved(int slot) {
        return reserved[slot];
    }

    /** Reserves {@code counts[slot]} items in each slot (0 = none). */
    public void reserve(int[] counts) {
        for (int i = 0; i < reserved.length; i++) {
            reserved[i] = counts == null || i >= counts.length ? 0 : counts[i];
        }
    }

    public void clearReservation() {
        reserve(null);
    }

    @Override
    protected void onContentsChanged(int slot) {
        revision++;
        onChanged.run();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (roles[slot].extractable()) {
            return false;
        }
        ItemStack current = getStackInSlot(slot);
        if (reserved[slot] > 0 && !ItemStack.isSameItemSameTags(current, stack)) {
            return false;
        }
        return accepts.test(roles[slot], stack);
    }

    /** At most the unreserved part of the slot. */
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        int available = getStackInSlot(slot).getCount() - reserved[slot];
        if (available <= 0) {
            return ItemStack.EMPTY;
        }
        return super.extractItem(slot, Math.min(amount, available), simulate);
    }

    /** Station-side insertion, ignoring roles and filters; returns what did not fit. */
    public ItemStack insertInternal(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack current = getStackInSlot(slot);
        if (current.isEmpty()) {
            int moved = Math.min(stack.getCount(), stack.getMaxStackSize());
            ItemStack placed = stack.copy();
            placed.setCount(moved);
            setStackInSlot(slot, placed);
            ItemStack rest = stack.copy();
            rest.shrink(moved);
            return rest;
        }
        if (!ItemStack.isSameItemSameTags(current, stack)) {
            return stack;
        }
        int moved = Math.min(stack.getCount(), current.getMaxStackSize() - current.getCount());
        ItemStack grown = current.copy();
        grown.grow(moved);
        setStackInSlot(slot, grown);
        ItemStack rest = stack.copy();
        rest.shrink(moved);
        return rest;
    }

    /** Station-side removal of exactly {@code count} items, reservation or not. */
    public void consume(int slot, int count) {
        ItemStack current = getStackInSlot(slot).copy();
        current.shrink(count);
        setStackInSlot(slot, current.isEmpty() ? ItemStack.EMPTY : current);
    }

    /** The slot reduced to capacity arithmetic. */
    public ProcessCompletion.Amount amount(int slot) {
        return amountOf(getStackInSlot(slot));
    }

    public static ProcessCompletion.Amount amountOf(ItemStack stack) {
        if (stack.isEmpty()) {
            return ProcessCompletion.Amount.EMPTY;
        }
        String key = BuiltInRegistries.ITEM.getKey(stack.getItem()) + (stack.hasTag() ? stack.getTag().toString() : "");
        return new ProcessCompletion.Amount(key, stack.getCount(), stack.getMaxStackSize());
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        if (stacks.size() != roles.length) {
            setSize(roles.length); // a saved inventory of another size is discarded rather than misread
        }
    }

    /** The automation view of one side: ingredients from above, bases/packaging/fuel from the sides, products below. */
    public IItemHandler side(Direction side) {
        return new Sided(this, side);
    }

    private static final class Sided implements IItemHandler {
        private final ProcessInventory inventory;
        private final int[] slots;

        Sided(ProcessInventory inventory, Direction side) {
            this.inventory = inventory;
            this.slots = IntStream.range(0, inventory.roles.length).filter(i -> exposes(side, inventory.roles[i])).toArray();
        }

        private static boolean exposes(Direction side, Role role) {
            if (side == null) {
                return false;
            }
            return switch (side) {
                case UP -> role == Role.INPUT;
                case DOWN -> role.extractable();
                default -> role == Role.BASE || role == Role.PACKAGING || role == Role.FUEL;
            };
        }

        @Override
        public int getSlots() {
            return slots.length;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slots[slot]);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return inventory.roles[slots[slot]].extractable() ? stack : inventory.insertItem(slots[slot], stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return inventory.roles[slots[slot]].extractable() ? inventory.extractItem(slots[slot], amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(slots[slot]);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return inventory.isItemValid(slots[slot], stack);
        }
    }
}
