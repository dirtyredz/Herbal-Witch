package io.github.dirtyredz.herbalwitch.brewing.application;

import io.github.dirtyredz.herbalwitch.botany.crop.HerbCropBlock;
import io.github.dirtyredz.herbalwitch.botany.garden.HerbBedBlock;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionContext;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionPermissionService;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.List;

/**
 * Garden products used on one clicked block. Each action is bounded to that block (or the block
 * above it), goes through the permission boundary, and reports success only when it changed the
 * world, so a failed use consumes nothing.
 */
public final class GardenApplication {
    /** Flowers the flower dust may grow. */
    public static final List<Block> FLOWERS = List.of(Blocks.DANDELION, Blocks.POPPY, Blocks.CORNFLOWER, Blocks.OXEYE_DAISY,
            Blocks.AZURE_BLUET, Blocks.ALLIUM, Blocks.PINK_TULIP, Blocks.LILY_OF_THE_VALLEY);

    /** What the product does. */
    public enum Garden {
        /** One bone meal application to a crop. */
        FERTILIZER,
        /** A flower on top of a clicked grass block. */
        FLOWER,
        /** A brown or red mushroom on top of the clicked block where it can survive. */
        MUSHROOM,
        /** One immature herb straight to maturity. */
        GROWTH,
        /** Enrich one herb bed for its next harvest. */
        SOIL
    }

    private GardenApplication() {
    }

    /** Server side. True when the world changed. */
    public static boolean apply(UseOnContext context, Garden kind) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (player == null) {
            return false;
        }
        ActionContext action = ActionContext.using(player, level, pos, context.getClickedFace(), context.getItemInHand());
        return switch (kind) {
            case FERTILIZER -> state.is(BlockTags.CROPS) && ActionPermissionService.mayModify(action)
                    && BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), level, pos) && changed(level, player, pos);
            case FLOWER -> state.is(Blocks.GRASS_BLOCK)
                    && place(level, player, action, FLOWERS.get(level.random.nextInt(FLOWERS.size())).defaultBlockState());
            case MUSHROOM -> place(level, player, action,
                    (level.random.nextBoolean() ? Blocks.BROWN_MUSHROOM : Blocks.RED_MUSHROOM).defaultBlockState());
            case GROWTH -> state.getBlock() instanceof HerbCropBlock crop && !crop.isMaxAge(state)
                    && ActionPermissionService.mayModify(action)
                    && set(level, player, pos, crop.getStateForAge(crop.getMaxAge()));
            case SOIL -> state.getBlock() instanceof HerbBedBlock && !state.getValue(HerbBedBlock.ENRICHED)
                    && ActionPermissionService.mayModify(action)
                    && set(level, player, pos, state.setValue(HerbBedBlock.ENRICHED, true));
        };
    }

    private static boolean place(Level level, Player player, ActionContext action, BlockState plant) {
        BlockPos above = action.pos().above();
        ActionContext at = action.at(above);
        if (!level.getBlockState(above).isAir() || !plant.canSurvive(level, above) || !ActionPermissionService.mayPlace(at)) {
            return false;
        }
        return set(level, player, above, plant);
    }

    private static boolean set(Level level, Player player, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, Block.UPDATE_ALL);
        return changed(level, player, pos);
    }

    private static boolean changed(Level level, Player player, BlockPos pos) {
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        return true;
    }
}
