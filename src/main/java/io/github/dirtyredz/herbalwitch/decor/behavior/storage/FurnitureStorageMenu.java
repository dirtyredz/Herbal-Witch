package io.github.dirtyredz.herbalwitch.decor.behavior.storage;

import io.github.dirtyredz.herbalwitch.decor.registry.DecorMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * The one menu of every cabinet and display: the piece's slots in a grid of {@link StorageSpec#columns()}
 * columns over the player's inventory and hotbar, with chest-style quick-move (respecting each
 * slot's limit, so a display slot takes one item). It stays valid only while its block entity
 * exists and the player is within reach, so breaking the piece closes it. Not a station menu: no
 * reservations, no processing.
 */
public class FurnitureStorageMenu extends AbstractContainerMenu {
    /** Top-left of the piece's slot grid and of the player inventory, in GUI pixels. */
    public static final int GRID_TOP = 18;
    public static final int SLOT = 18;

    private final StorageSpec spec;
    private final FurnitureStorageEntity entity;

    /** Server side: over a real block entity. */
    public FurnitureStorageMenu(int id, Inventory player, FurnitureStorageEntity entity) {
        this(id, player, entity.storage(), entity.inventory(), entity);
    }

    /** Client side: a mirror of the server's slots, sized from the opening packet. */
    public static FurnitureStorageMenu client(int id, Inventory player, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        StorageSpec spec = new StorageSpec(buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
        FurnitureStorageEntity entity = player.player.level().getBlockEntity(pos) instanceof FurnitureStorageEntity e ? e : null;
        return new FurnitureStorageMenu(id, player, spec, new ItemStackHandler(spec.slots()) {
            @Override
            public int getSlotLimit(int slot) {
                return spec.slotLimit();
            }
        }, entity);
    }

    /** Writes what {@link #client} reads. */
    public static void write(FriendlyByteBuf buf, BlockPos pos, StorageSpec spec) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(spec.slots());
        buf.writeVarInt(spec.columns());
        buf.writeBoolean(spec.display());
    }

    private FurnitureStorageMenu(int id, Inventory player, StorageSpec spec, IItemHandler items, FurnitureStorageEntity entity) {
        super(DecorMenus.STORAGE.get(), id);
        this.spec = spec;
        this.entity = entity;
        int left = gridLeft(spec);
        for (int i = 0; i < spec.slots(); i++) {
            addSlot(new SlotItemHandler(items, i, left + (i % spec.columns()) * SLOT, GRID_TOP + (i / spec.columns()) * SLOT));
        }
        int top = playerTop(spec);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(player, col + row * 9 + 9, 8 + col * SLOT, top + row * SLOT));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(player, col, 8 + col * SLOT, top + 58));
        }
    }

    public StorageSpec spec() {
        return spec;
    }

    /** The grid's left edge: centred in the 176-wide panel. */
    public static int gridLeft(StorageSpec spec) {
        return 8 + (9 - spec.columns()) * SLOT / 2;
    }

    /** The player inventory's top: below the grid with a label row (vanilla's chest spacing). */
    public static int playerTop(StorageSpec spec) {
        return GRID_TOP + spec.rows() * SLOT + 13;
    }

    /** The panel height for a grid of this size (vanilla's chest: 114 plus a row's height per row). */
    public static int imageHeight(StorageSpec spec) {
        return 114 + spec.rows() * SLOT;
    }

    @Override
    public boolean stillValid(Player player) {
        return entity != null && !entity.isRemoved() && entity.getLevel() == player.level()
                && player.distanceToSqr(entity.getBlockPos().getX() + 0.5, entity.getBlockPos().getY() + 0.5,
                entity.getBlockPos().getZ() + 0.5) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int own = spec.slots();
        boolean moved = index < own ? moveItemStackTo(stack, own, slots.size(), true) : moveItemStackTo(stack, 0, own, false);
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }
}
