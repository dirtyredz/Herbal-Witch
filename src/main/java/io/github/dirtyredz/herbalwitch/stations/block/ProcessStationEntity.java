package io.github.dirtyredz.herbalwitch.stations.block;

import io.github.dirtyredz.herbalwitch.stations.heat.HeatSources;
import io.github.dirtyredz.herbalwitch.stations.menu.StationMenu;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessJob;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessLayout;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessStation;
import io.github.dirtyredz.herbalwitch.stations.process.authorization.MagicalProcessPolicy;
import io.github.dirtyredz.herbalwitch.stations.process.authorization.StationOperations;
import io.github.dirtyredz.herbalwitch.stations.process.execution.ProcessExecutionPolicy;
import io.github.dirtyredz.herbalwitch.stations.process.recipe.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.stations.process.recipe.ProcessRecipeIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import java.util.function.Supplier;

/**
 * The block-entity boilerplate every processing station shares (lifecycle, not processing): it owns
 * a {@link ProcessStation} by composition and forwards ticks, persistence, automation capabilities,
 * drops and menus to it. A station that runs witch recipes gets a {@link MagicalProcessPolicy} and
 * menus whose Start, Resume and Cancel buttons reach it; others run Milestone 3's ordinary policy.
 * Heat, when needed, is read below the origin each tick. Subclasses name their slots and menu.
 */
public abstract class ProcessStationEntity extends BlockEntity implements StationEntity {
    private final ProcessStation station;
    private final MagicalProcessPolicy magic;
    private final boolean needsHeat;

    /**
     * @param inventory  the station's slots (its roles define the recipe layout)
     * @param recipeType the processing type it runs
     * @param needsHeat  whether progress needs heat below the origin
     * @param magical    whether it may run witch recipes (a magical policy and operation buttons)
     * @param decorator  pure result adjustment from the inputs (water metadata)
     */
    protected ProcessStationEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ProcessInventory inventory,
                                   Supplier<? extends RecipeType<? extends ProcessRecipe>> recipeType, boolean needsHeat, boolean magical,
                                   ProcessJob.ResultDecorator decorator) {
        super(type, pos, state);
        this.needsHeat = needsHeat;
        this.magic = magical ? new MagicalProcessPolicy() : null;
        inventory.onChanged(this::setChanged);
        inventory.accepts((role, stack) -> level == null || ProcessRecipeIndex.isUsed(level, recipeType.get(), role, stack));
        ProcessExecutionPolicy policy = magic == null ? ProcessExecutionPolicy.ORDINARY : magic;
        station = new ProcessStation(inventory, new ProcessJob(recipeType.get(), ProcessLayout.of(inventory), needsHeat, decorator, policy));
    }

    protected ProcessStationEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ProcessInventory inventory,
                                   Supplier<? extends RecipeType<? extends ProcessRecipe>> recipeType, boolean needsHeat, boolean magical) {
        this(type, pos, state, inventory, recipeType, needsHeat, magical, (result, inv, reservation) -> result);
    }

    /** The station's menu over the authoritative inventory and this viewer's data. */
    protected abstract StationMenu menu(int id, Inventory player, ProcessInventory inventory, ContainerData data, ContainerLevelAccess access);

    @Override
    public ProcessInventory inventory() {
        return station.inventory();
    }

    public ProcessStation station() {
        return station;
    }

    /** The magical policy, or null for an ordinary-only station. */
    public MagicalProcessPolicy magic() {
        return magic;
    }

    @Override
    public void serverTick() {
        station.tick(level, worldPosition, needsHeat && HeatSources.isHeated(level, worldPosition));
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
        StationMenu menu = menu(id, player, station.inventory(), station.data(who), ContainerLevelAccess.create(level, worldPosition));
        if (magic != null) {
            menu.withOperations((operator, button) -> StationOperations.handle(operator, button, station, magic, level, worldPosition));
        }
        return menu;
    }
}
