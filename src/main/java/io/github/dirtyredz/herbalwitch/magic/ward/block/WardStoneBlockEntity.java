package io.github.dirtyredz.herbalwitch.magic.ward.block;

import io.github.dirtyredz.herbalwitch.magic.ward.menu.WardMenu;
import io.github.dirtyredz.herbalwitch.magic.ward.registry.WardRegistration;
import io.github.dirtyredz.herbalwitch.magic.ward.service.WardLifecycle;
import io.github.dirtyredz.herbalwitch.magic.ward.service.WardService;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * A ward stone's entity: only the id of its record in {@code WardSavedData} (0 before the first
 * activation creates one). No inventory, no charge here: the saved data owns that. It reconciles its
 * record when it loads (a copied or mismatched id stays inert), ticks a charged ward, and leaves the
 * index when it unloads.
 */
public class WardStoneBlockEntity extends BlockEntity implements MenuProvider {
    private long recordId;

    public WardStoneBlockEntity(BlockPos pos, BlockState state) {
        super(WardRegistration.STONE_ENTITY.get(), pos, state);
    }

    /** The stone at {@code pos}, if it is one and loaded. */
    public static Optional<WardStoneBlockEntity> at(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos) || !(level.getBlockEntity(pos) instanceof WardStoneBlockEntity stone)) {
            return Optional.empty();
        }
        return Optional.of(stone);
    }

    public long recordId() {
        return recordId;
    }

    public void setRecordId(long id) {
        if (recordId != id) {
            recordId = id;
            setChanged();
        }
    }

    public void serverTick() {
        if (level instanceof ServerLevel server) {
            WardService.tick(server, worldPosition, this);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) {
            WardLifecycle.loaded(server, worldPosition, this);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level instanceof ServerLevel server) {
            WardLifecycle.unloaded(server, worldPosition, recordId);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        recordId = tag.getLong("Record");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (recordId > 0) {
            tag.putLong("Record", recordId);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return WardMenu.server(id, inventory, player, worldPosition);
    }
}
