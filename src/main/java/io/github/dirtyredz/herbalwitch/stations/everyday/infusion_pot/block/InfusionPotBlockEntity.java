package io.github.dirtyredz.herbalwitch.stations.everyday.infusion_pot.block;

import io.github.dirtyredz.herbalwitch.stations.everyday.infusion_pot.menu.InfusionPotMenu;
import io.github.dirtyredz.herbalwitch.stations.block.StationEntity;
import io.github.dirtyredz.herbalwitch.stations.heat.HeatSources;
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

/** Infusion pot contents: two ingredients, a base liquid, a package, the product and returned containers. Infusion needs heat from below (600 ticks); the bottle of the base comes back. */
public class InfusionPotBlockEntity extends BlockEntity implements StationEntity {
    public static final int INPUT_0 = 0;
    public static final int INPUT_1 = 1;
    public static final int BASE = 2;
    public static final int PACKAGING = 3;
    public static final int OUTPUT = 4;
    public static final int RETURN = 5;
    public static final boolean NEEDS_HEAT = true;

    private final ProcessStation station;

    public InfusionPotBlockEntity(BlockPos pos, BlockState state) {
        super(StationBlockEntities.INFUSION_POT.get(), pos, state);
        ProcessInventory inventory = newInventory();
        inventory.onChanged(this::setChanged);
        inventory.accepts((role, stack) -> level == null || ProcessRecipeIndex.isUsed(level, StationRecipes.INFUSION.get(), role, stack));
        station = new ProcessStation(inventory, new ProcessJob(StationRecipes.INFUSION.get(), ProcessLayout.of(inventory), NEEDS_HEAT));
    }

    public static ProcessInventory newInventory() {
        return new ProcessInventory(ProcessInventory.Role.INPUT, ProcessInventory.Role.INPUT, ProcessInventory.Role.BASE, ProcessInventory.Role.PACKAGING, ProcessInventory.Role.OUTPUT, ProcessInventory.Role.RETURN);
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
        station.tick(level, NEEDS_HEAT && HeatSources.isHeated(level, worldPosition));
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
        return new InfusionPotMenu(id, player, station.inventory(), station.data(), ContainerLevelAccess.create(level, worldPosition));
    }
}
