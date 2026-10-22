package io.github.dirtyredz.herbalwitch.magic.ritual.altar;

import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualBlockEntities;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessionService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The altar's origin anchor, holding nothing durable: ritual progress is transient and never resumes
 * after an unload or a restart. Unloading cancels the altar's ritual; an origin found glowing without
 * a session (a crash mid-ritual) clears its glow.
 */
public class AltarBlockEntity extends BlockEntity {
    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(RitualBlockEntities.ALTAR.get(), pos, state);
    }

    void serverTick() {
        if (level instanceof ServerLevel server && !AltarSessionService.busy(server, worldPosition)) {
            AltarBlock.setActive(server, worldPosition, false);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level instanceof ServerLevel server) {
            AltarSessionService.altarGone(server, worldPosition);
        }
    }
}
