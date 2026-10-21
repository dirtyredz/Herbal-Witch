package io.github.dirtyredz.herbalwitch.stations.everyday.hearth.block;

import io.github.dirtyredz.herbalwitch.stations.block.StationEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.hearth.menu.HearthMenu;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessCompletion;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import java.util.EnumMap;
import java.util.Map;

/**
 * Hearth fuel: burns any furnace fuel for its normal burn time, one item after another, while the
 * chunk is loaded (a vessel above is not required); unloading pauses it. A fuel's container (a lava
 * bucket's bucket) goes to the return slot; fuel is not taken while that slot cannot hold it.
 */
public class HearthBlockEntity extends BlockEntity implements StationEntity {
    public static final int FUEL = 0;
    public static final int RETURN = 1;
    public static final int DATA_BURN = 0;
    public static final int DATA_BURN_TOTAL = 1;
    public static final int DATA_COUNT = 2;

    private final ProcessInventory inventory = newInventory();
    private final Map<Direction, LazyOptional<IItemHandler>> sides = new EnumMap<>(Direction.class);
    private int burn;
    private int burnTotal;
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == DATA_BURN ? burn : index == DATA_BURN_TOTAL ? burnTotal : 0;
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public HearthBlockEntity(BlockPos pos, BlockState state) {
        super(StationBlockEntities.HEARTH.get(), pos, state);
        inventory.onChanged(this::setChanged);
        inventory.accepts((role, stack) -> role == ProcessInventory.Role.FUEL && ForgeHooks.getBurnTime(stack, null) > 0);
        createHandlers();
    }

    public static ProcessInventory newInventory() {
        return new ProcessInventory(ProcessInventory.Role.FUEL, ProcessInventory.Role.RETURN);
    }

    @Override
    public ProcessInventory inventory() {
        return inventory;
    }

    public int burn() {
        return burn;
    }

    @Override
    public void serverTick() {
        if (burn > 0) {
            burn--;
        }
        if (burn == 0) {
            ignite();
        }
        boolean lit = burn > 0;
        BlockState state = getBlockState();
        if (state.getValue(HearthBlock.LIT) != lit) {
            level.setBlock(worldPosition, state.setValue(HearthBlock.LIT, lit), Block.UPDATE_ALL);
        }
        if (lit) {
            setChanged();
        }
    }

    private void ignite() {
        ItemStack fuel = inventory.getStackInSlot(FUEL);
        int time = ForgeHooks.getBurnTime(fuel, null);
        if (time <= 0) {
            burnTotal = 0;
            return;
        }
        ItemStack container = fuel.getCraftingRemainingItem();
        if (!ProcessCompletion.fits(inventory.amount(RETURN), ProcessInventory.amountOf(container))) {
            return;
        }
        inventory.consume(FUEL, 1);
        inventory.insertInternal(RETURN, container);
        burn = time;
        burnTotal = time;
    }

    @Override
    public void dropContents() {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Block.popResource(level, worldPosition, stack.copy());
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        burn = tag.getInt("Burn");
        burnTotal = tag.getInt("BurnTotal");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("Burn", burn);
        tag.putInt("BurnTotal", burnTotal);
    }

    private void createHandlers() {
        for (Direction side : Direction.values()) {
            IItemHandler view = inventory.side(side);
            sides.put(side, LazyOptional.of(() -> view));
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && !remove && side != null) {
            return sides.get(side).cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        sides.values().forEach(LazyOptional::invalidate);
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        createHandlers();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player, Player who) {
        return new HearthMenu(id, player, inventory, data, ContainerLevelAccess.create(level, worldPosition));
    }
}
