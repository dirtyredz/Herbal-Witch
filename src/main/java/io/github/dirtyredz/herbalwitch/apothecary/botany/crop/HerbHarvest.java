package io.github.dirtyredz.herbalwitch.apothecary.botany.crop;

import io.github.dirtyredz.herbalwitch.platform.permission.ActionContext;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionPermissionService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Harvest by hand: a server-side transaction on a mature plant. The plant is cut back first, then
 * the yield is dropped, so a second use (or a break) of the same plant in the same tick finds an
 * immature plant and cannot pay out again.
 */
public final class HerbHarvest {
    /** Fresh herbs per mature harvest, before any bonus. */
    public static final int BASE_FRESH = 2;
    /** Seeds per mature harvest. */
    public static final int BASE_SEEDS = 1;

    private HerbHarvest() {
    }

    public static InteractionResult harvestByUse(HerbCropBlock block, BlockState state, Level level, BlockPos pos,
                                                 Player player) {
        if (!block.isMaxAge(state)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!ActionPermissionService.mayModify(ActionContext.of(player, level, pos))) {
            return InteractionResult.FAIL;
        }
        var plan = io.github.dirtyredz.herbalwitch.apothecary.botany.harvest.HerbHarvestPlan.prepare((ServerLevel) level, pos).orElse(null);
        if (plan == null) return InteractionResult.PASS;
        var commit = new io.github.dirtyredz.herbalwitch.apothecary.botany.harvest.HerbHarvestCommit((ServerLevel) level, player, plan);
        if (!commit.apply()) { commit.rollback(); return InteractionResult.FAIL; }
        commit.emit();
        return InteractionResult.CONSUME;
    }
}
