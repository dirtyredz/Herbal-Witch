package io.github.dirtyredz.herbalwitch.stations.process;

import io.github.dirtyredz.herbalwitch.stations.process.recipe.ProcessRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * The part every processing station's block entity has in common, as a component (not a base
 * class): the inventory, the job, per-side automation handlers with their lifecycle, menu data,
 * persistence and dropping the actual stored stacks. The owning block entity decides heat and
 * forwards its lifecycle calls here.
 */
public final class ProcessStation {
    /** Menu data indices. */
    public static final int DATA_PROGRESS = 0;
    public static final int DATA_DURATION = 1;
    public static final int DATA_STATUS = 2;
    public static final int DATA_HEATED = 3;
    /** Energy one batch of the matched recipe costs here now (0 for ordinary recipes). */
    public static final int DATA_ENERGY = 4;
    /** Whose batch it is, seen by this menu's viewer: 0 nobody's, 1 the viewer's, 2 someone else's. */
    public static final int DATA_OPERATOR = 5;
    /** 1 when the matched recipe needs a witch. */
    public static final int DATA_MAGICAL = 6;
    public static final int DATA_COUNT = 7;

    private final ProcessInventory inventory;
    private final ProcessJob job;
    private boolean hot;
    private final Map<Direction, LazyOptional<IItemHandler>> sides = new EnumMap<>(Direction.class);

    public ProcessStation(ProcessInventory inventory, ProcessJob job) {
        this.inventory = inventory;
        this.job = job;
        createHandlers();
    }

    public ProcessInventory inventory() {
        return inventory;
    }

    public ProcessJob job() {
        return job;
    }

    /** Menu data for a viewer-less menu (nothing is anybody's). */
    public ContainerData data() {
        return data(null);
    }

    /** Menu data as {@code viewer} sees it (each open menu has its own view: the operator field is personal). */
    public ContainerData data(Player viewer) {
        UUID id = viewer == null ? null : viewer.getUUID();
        return new ContainerData() {
            @Override
            public int get(int index) {
                ProcessRecipe recipe = job.recipe();
                return switch (index) {
                    case DATA_PROGRESS -> job.progress();
                    case DATA_DURATION -> job.duration();
                    case DATA_STATUS -> job.status().ordinal();
                    case DATA_HEATED -> hot ? 1 : 0;
                    case DATA_ENERGY -> recipe == null ? 0 : job.policy().energyCost(recipe);
                    case DATA_OPERATOR -> id == null ? 0 : job.policy().relation(id);
                    case DATA_MAGICAL -> recipe != null && recipe.description().requirements().magical() ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }

    /** One loaded server tick of the station whose origin is {@code pos}. */
    public void tick(Level level, BlockPos pos, boolean heated) {
        this.hot = heated;
        job.tick(level, pos, inventory, heated);
    }

    /** The automation handler for a side; empty for an unknown side. */
    public LazyOptional<IItemHandler> handler(Direction side) {
        return side == null ? LazyOptional.empty() : sides.get(side);
    }

    /** Call from {@code invalidateCaps}; {@link #reviveHandlers} from {@code reviveCaps}. */
    public void invalidateHandlers() {
        sides.values().forEach(LazyOptional::invalidate);
    }

    public void reviveHandlers() {
        createHandlers();
    }

    private void createHandlers() {
        for (Direction side : Direction.values()) {
            IItemHandler view = inventory.side(side);
            sides.put(side, LazyOptional.of(() -> view));
        }
    }

    public void save(CompoundTag tag) {
        tag.put("Inventory", inventory.serializeNBT());
        tag.put("Job", job.save());
    }

    public void load(CompoundTag tag) {
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        job.load(tag.getCompound("Job"));
    }

    /** Drops every stored stack once (reservations are not extra items). */
    public void dropContents(Level level, BlockPos pos) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Block.popResource(level, pos, stack.copy());
            }
        }
        inventory.clearReservation();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }
}
