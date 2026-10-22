package io.github.dirtyredz.herbalwitch.magic.ward.service;

import io.github.dirtyredz.herbalwitch.magic.ward.catalog.WardCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;


import static io.github.dirtyredz.herbalwitch.magic.ward.service.WardService.rules;

/** Loaded functional hearth/stove detection and the resulting maximum radius; light state is irrelevant. */
public final class WardFocus {
    private WardFocus() { }

    /** Whether a functional hearth or stove stands within {@link WardCatalog#FOCUS_REACH} (loaded cells only). */
    public static boolean focused(ServerLevel level, BlockPos stone) {
        int reach = WardCatalog.FOCUS_REACH;
        for (BlockPos p : BlockPos.betweenClosed(stone.offset(-reach, -2, -reach), stone.offset(reach, 2, reach))) {
            if (!level.isLoaded(p)) {
                continue;
            }
            Block block = level.getBlockState(p).getBlock();
            if (block == StationBlocks.HEARTH.get() || block == StationBlocks.STOVE.get()) {
                return true;
            }
        }
        return false;
    }

    /** The largest radius the stone may be set to now. */
    public static int maxRadius(ServerLevel level, BlockPos stone) {
        return focused(level, stone) ? rules().wardFocusRadius() : rules().wardRadius();
    }


}
