package io.github.dirtyredz.herbalwitch.decor.behavior.storage;

import io.github.dirtyredz.herbalwitch.decor.behavior.storage.display.DisplaySnapshot;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * The block entity of a storage or display piece. Only the origin cell's entity holds the
 * inventory ({@link FurnitureInventory}); a cabinet's other cells carry an empty entity that hands
 * automation to the origin's inventory, so a hopper on any part reaches the same contents. A display
 * keeps its contents manual (no item handler outside the menu) and sends what it shows to clients
 * only when that changes ({@link DisplaySnapshot}). Contents drop exactly once, from the origin,
 * when the piece goes.
 */
public class FurnitureStorageEntity extends BlockEntity implements MenuProvider {
    private static final String INVENTORY = "Inventory";

    private FurnitureInventory inventory;
    private LazyOptional<IItemHandler> handler = LazyOptional.empty();

    public FurnitureStorageEntity(BlockPos pos, BlockState state) {
        super(DecorBlockEntities.STORAGE.get(), pos, state);
    }

    private StorageFurnitureBlock block() {
        return (StorageFurnitureBlock) getBlockState().getBlock();
    }

    public boolean isOrigin() {
        return getBlockState().getBlock() instanceof StorageFurnitureBlock block && block.isOrigin(getBlockState());
    }

    /** The inventory, created on first use at the origin (a pre-0.4.0 shelf gets an empty one). */
    public FurnitureInventory inventory() {
        if (inventory == null) {
            inventory = new FurnitureInventory(StorageSpec.of(block().furniture()), this::contentsChanged);
        }
        return inventory;
    }

    public StorageSpec storage() {
        return StorageSpec.of(block().furniture());
    }

    private void contentsChanged() {
        setChanged();
        if (level != null && !level.isClientSide && storage().display()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    /** Drops the contents at the origin, once: the inventory is emptied as it drops. */
    public void dropContents() {
        if (level != null && isOrigin() && inventory != null) {
            for (ItemStack stack : inventory.takeAll()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
            }
        }
    }

    // ---------------------------------------------------------------- persistence and sync

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (inventory != null) {
            tag.put(INVENTORY, inventory.save());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(INVENTORY)) {
            inventory().load(tag.getCompound(INVENTORY));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        if (storage().display() && isOrigin()) {
            DisplaySnapshot.of(inventory()).write(tag);
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (storage().display()) {
            DisplaySnapshot.read(tag).applyTo(inventory());
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return storage().display() && isOrigin() ? ClientboundBlockEntityDataPacket.create(this) : null;
    }

    /** What a display shows, in slot order (client side: the last snapshot). */
    public List<ItemStack> shown() {
        List<ItemStack> out = new ArrayList<>();
        for (int i = 0; i < inventory().getSlots(); i++) {
            out.add(inventory().getStackInSlot(i));
        }
        return out;
    }

    // ---------------------------------------------------------------- automation

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability != ForgeCapabilities.ITEM_HANDLER || level == null || storage().display()) {
            return super.getCapability(capability, side);
        }
        if (isOrigin()) {
            if (!handler.isPresent()) {
                handler = LazyOptional.of(this::inventory);
            }
            return handler.cast();
        }
        BlockPos origin = block().origin(getBlockState(), worldPosition);
        if (!level.isLoaded(origin) || !(level.getBlockEntity(origin) instanceof FurnitureStorageEntity owner) || !owner.isOrigin()) {
            return LazyOptional.empty();
        }
        return owner.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handler.invalidate();
        handler = LazyOptional.empty();
    }

    // ---------------------------------------------------------------- menu

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new FurnitureStorageMenu(id, playerInventory, this);
    }
}
