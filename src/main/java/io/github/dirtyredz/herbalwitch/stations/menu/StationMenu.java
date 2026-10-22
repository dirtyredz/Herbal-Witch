package io.github.dirtyredz.herbalwitch.stations.menu;

import io.github.dirtyredz.herbalwitch.stations.process.ProcessInventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Menu behaviour every station shares: station slots over the authoritative inventory (so
 * reservations hold in the menu too), the player inventory, vanilla data-slot synchronisation,
 * the vanilla distance/block check, and a quick-move that never moves reserved items. No custom
 * packets: all mutation is the server's normal menu handling. Closing the menu leaves the
 * station running.
 */
public abstract class StationMenu extends AbstractContainerMenu {
    /** Where a station slot is drawn. */
    public record SlotSpec(int slot, int x, int y) {
    }

    /** What a server-side menu does with a button click (Start, Resume, Cancel); absent on clients and ordinary stations. */
    @FunctionalInterface
    public interface Operations {
        boolean operate(ServerPlayer player, int button);
    }

    protected final ContainerData data;
    private final ContainerLevelAccess access;
    private final Block block;
    private final int stationSlots;
    private Operations operations;

    protected StationMenu(MenuType<?> type, int id, Inventory player, ProcessInventory inventory, ContainerData data,
                          ContainerLevelAccess access, Block block, SlotSpec... slots) {
        super(type, id);
        this.data = data;
        this.access = access;
        this.block = block;
        this.stationSlots = slots.length;
        for (SlotSpec spec : slots) {
            addSlot(new SlotItemHandler(inventory, spec.slot(), spec.x(), spec.y()));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(player, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(player, column, 8 + column * 18, 142));
        }
        addDataSlots(data);
    }

    public int data(int index) {
        return data.get(index);
    }

    /** Server side: lets this menu's buttons operate the station (magical stations only). */
    public StationMenu withOperations(Operations handler) {
        this.operations = handler;
        return this;
    }

    /** Vanilla's menu-button click, already checked against the open menu and its validity. */
    @Override
    public boolean clickMenuButton(Player player, int id) {
        return operations != null && player instanceof ServerPlayer serverPlayer && operations.operate(serverPlayer, id);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, block);
    }

    /**
     * Station slot -> player inventory (only the unreserved part leaves the slot); player inventory
     * -> the first station slots that accept the item.
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        int total = slots.size();
        if (index < stationSlots) {
            ItemStack taken = slot.remove(slot.getItem().getCount()); // the station's extract rule caps this
            if (taken.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack original = taken.copy();
            moveItemStackTo(taken, stationSlots, total, true);
            if (!taken.isEmpty()) { // put back what did not fit
                ItemStack back = slot.getItem().isEmpty() ? taken : slot.getItem().copyWithCount(slot.getItem().getCount() + taken.getCount());
                slot.set(back);
            }
            slot.onTake(player, original);
            return taken.isEmpty() ? original : ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem().copy();
        ItemStack original = stack.copy();
        // Vanilla moveItemStackTo merges without asking mayPlace (it would fill output slots), so
        // insert through each station slot's own rules: same-item slots first, then empty ones.
        for (boolean merging : new boolean[] {true, false}) {
            for (int i = 0; i < stationSlots && !stack.isEmpty(); i++) {
                Slot target = slots.get(i);
                if (target.getItem().isEmpty() != merging) {
                    stack = target.safeInsert(stack);
                }
            }
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.setByPlayer(stack);
        return ItemStack.EMPTY; // one pass per click: vanilla repeats while this returns a stack
    }
}
