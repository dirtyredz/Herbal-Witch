package io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.block;

import io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.menu.DryingRackMenu;
import io.github.dirtyredz.herbalwitch.stations.block.StationEntity;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessJob;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessLayout;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipeIndex;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessStation;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlockEntities;
import io.github.dirtyredz.herbalwitch.stations.registry.StationRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

/** Drying rack contents: one fresh herb slot and one bundle slot; drying needs no heat. */
public class DryingRackBlockEntity extends BlockEntity implements StationEntity {
    public static final int INPUT = 0;
    public static final int OUTPUT = 1;

    private final ProcessStation station;

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(StationBlockEntities.DRYING_RACK.get(), pos, state);
        ProcessInventory inventory = newInventory();
        inventory.onChanged(this::setChanged);
        inventory.accepts((role, stack) -> level == null || ProcessRecipeIndex.isUsed(level, StationRecipes.DRYING.get(), role, stack));
        station = new ProcessStation(inventory, new ProcessJob(StationRecipes.DRYING.get(), ProcessLayout.of(inventory), false));
    }

    public static ProcessInventory newInventory() {
        return new ProcessInventory(ProcessInventory.Role.INPUT, ProcessInventory.Role.OUTPUT);
    }

    @Override
    public ProcessInventory inventory() {
        return station.inventory();
    }

    public ProcessStation station() {
        return station;
    }

    @Override
    public void serverTick() {
        station.tick(level, false);
        boolean active = !station.inventory().getStackInSlot(INPUT).isEmpty() || !station.inventory().getStackInSlot(OUTPUT).isEmpty();
        BlockState state = getBlockState();
        if (state.hasProperty(DryingRackBlock.ACTIVE) && state.getValue(DryingRackBlock.ACTIVE) != active
                && state.getBlock() instanceof DryingRackBlock rack) {
            rack.setOnStation(level, worldPosition, DryingRackBlock.ACTIVE, active);
        }
    }

    @Override
    public void dropContents() {
        station.dropContents(level, worldPosition);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        station.load(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        station.save(tag);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && !remove) {
            return station.handler(side).cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        station.invalidateHandlers();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        station.reviveHandlers();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player, Player who) {
        return new DryingRackMenu(id, player, station.inventory(), station.data(), ContainerLevelAccess.create(level, worldPosition));
    }
}
