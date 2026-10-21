package io.github.dirtyredz.herbalwitch.botany.crop;

import io.github.dirtyredz.herbalwitch.botany.conditions.HerbConditionEvaluator;
import io.github.dirtyredz.herbalwitch.botany.garden.HerbBedBlock;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionContext;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionPermissionService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

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
        ServerLevel server = (ServerLevel) level;
        level.setBlock(pos, block.getStateForAge(block.harvestResetAge()), Block.UPDATE_CLIENTS);
        int fresh = BASE_FRESH + HerbConditionEvaluator.harvestBonus(server, pos, block.herb(), level.random)
                + HerbBedBlock.consumeEnrichment(level, pos.below());
        Block.popResource(level, pos, new ItemStack(BotanyItems.fresh(block.herb()), fresh));
        Block.popResource(level, pos, new ItemStack(BotanyItems.seeds(block.herb()), BASE_SEEDS));
        level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F,
                0.8F + level.random.nextFloat() * 0.4F);
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        return InteractionResult.CONSUME;
    }
}
