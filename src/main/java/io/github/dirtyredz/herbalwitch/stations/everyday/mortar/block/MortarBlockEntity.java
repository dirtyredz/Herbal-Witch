package io.github.dirtyredz.herbalwitch.stations.everyday.mortar.block;

import io.github.dirtyredz.herbalwitch.stations.everyday.mortar.menu.MortarMenu;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

/** Mortar contents: three ingredient slots, the product and a returned-container slot. Grinding starts by itself once a recipe is assembled (100 ticks); no heat. */
public class MortarBlockEntity extends BlockEntity implements StationEntity {
    public static final int INPUT_0 = 0;
    public static final int INPUT_1 = 1;
    public static final int INPUT_2 = 2;
    public static final int OUTPUT = 3;
    public static final int RETURN = 4;
    public static final boolean NEEDS_HEAT = false;

    private final ProcessStation station;

    public MortarBlockEntity(BlockPos pos, BlockState state) {
        super(StationBlockEntities.MORTAR.get(), pos, state);
        ProcessInventory inventory = newInventory();
        inventory.onChanged(this::setChanged);
        inventory.accepts((role, stack) -> level == null || ProcessRecipeIndex.isUsed(level, StationRecipes.GRINDING.get(), role, stack));
        station = new ProcessStation(inventory, new ProcessJob(StationRecipes.GRINDING.get(), ProcessLayout.of(inventory), NEEDS_HEAT));
    }

    public static ProcessInventory newInventory() {
        return new ProcessInventory(ProcessInventory.Role.INPUT, ProcessInventory.Role.INPUT, ProcessInventory.Role.INPUT, ProcessInventory.Role.OUTPUT, ProcessInventory.Role.RETURN);
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
        return new MortarMenu(id, player, station.inventory(), station.data(), ContainerLevelAccess.create(level, worldPosition));
    }
}
