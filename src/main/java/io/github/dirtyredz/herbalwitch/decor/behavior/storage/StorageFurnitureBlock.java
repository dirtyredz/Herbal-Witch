package io.github.dirtyredz.herbalwitch.decor.behavior.storage;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.structure.DecorAssemblyBlock;
import io.github.dirtyredz.herbalwitch.decor.structure.FurnitureAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

/**
 * Cabinets and display shelves: using any cell opens the piece's one inventory, kept by the origin
 * cell ({@link FurnitureStorageEntity}). A cabinet's other cells carry an entity only to hand
 * automation to the origin; a display's do not. Contents drop once, from the origin, when the piece
 * is broken or loses its support.
 */
public class StorageFurnitureBlock extends DecorAssemblyBlock implements EntityBlock {
    public StorageFurnitureBlock(FurnitureSpec spec, Properties properties) {
        super(spec, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        boolean cabinet = furniture().behavior() == FurnitureBehavior.STORAGE;
        return isOrigin(state) || cabinet ? new FurnitureStorageEntity(pos, state) : null;
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling, the vanilla use hook
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos origin = origin(state, pos);
        if (!FurnitureAccess.mayUse(player, level, origin)
                || !(level.getBlockEntity(origin) instanceof FurnitureStorageEntity storage) || !storage.isOrigin()) {
            return InteractionResult.FAIL;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, storage, buf -> FurnitureStorageMenu.write(buf, origin, storage.storage()));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling, the vanilla removal hook
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moved) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof FurnitureStorageEntity storage) {
            storage.dropContents();
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, next, moved);
    }
}
