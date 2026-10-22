package io.github.dirtyredz.herbalwitch.decor.structure;

import io.github.dirtyredz.herbalwitch.platform.placement.PlacementChecks;
import io.github.dirtyredz.herbalwitch.platform.placement.PlacementCommit;
import io.github.dirtyredz.herbalwitch.platform.placement.PlacementPlan;
import io.github.dirtyredz.herbalwitch.platform.placement.PlacementResolver;
import io.github.dirtyredz.herbalwitch.platform.placement.StructurePlacement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Optional;

/**
 * The item of every furniture piece. Placing it resolves one plan from the aimed cell and the
 * player's facing (the same resolution the client preview draws), checks every cell, then places the
 * whole piece or nothing: never somewhere else, never part of it, and the item is used only after
 * the placement stands. The block decides its origin state (facing, a door's hinge, a bed's
 * direction) and its cells' states ({@link StructurePlacement}).
 */
public class FurniturePlacementItem extends BlockItem {
    public FurniturePlacementItem(Block block, Properties properties) {
        super(block, properties);
    }

    /** The plan this item would place for a context, or empty when the block offers no state there. */
    public Optional<PlacementPlan> plan(BlockPlaceContext context) {
        if (!(getBlock() instanceof StructurePlacement structure)) {
            return Optional.empty();
        }
        BlockState origin = getBlock().getStateForPlacement(context);
        return origin == null ? Optional.empty() : Optional.of(PlacementResolver.resolve(context.getClickedPos(), origin, structure));
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        if (!context.canPlace()) {
            return InteractionResult.FAIL;
        }
        BlockPlaceContext placing = updatePlacementContext(context);
        if (placing == null) {
            return InteractionResult.FAIL;
        }
        Optional<PlacementPlan> plan = plan(placing);
        if (plan.isEmpty()) {
            return InteractionResult.FAIL;
        }
        StructurePlacement structure = (StructurePlacement) getBlock();
        Level level = placing.getLevel();
        Player player = placing.getPlayer();
        Optional<PlacementChecks.Refusal> refusal = PlacementChecks.check(placing, plan.get(), structure);
        if (refusal.isPresent()) {
            if (player != null && !level.isClientSide) {
                player.displayClientMessage(refusal.get().problem().message(), true);
            }
            return InteractionResult.FAIL;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS; // the server places; its block updates reach the client
        }
        if (!PlacementCommit.commit(level, plan.get(), player, placing.getClickedFace())) {
            return InteractionResult.FAIL;
        }
        BlockState origin = plan.get().originState();
        ItemStack stack = placing.getItemInHand();
        origin.getBlock().setPlacedBy(level, plan.get().origin(), level.getBlockState(plan.get().origin()), player, stack);
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, plan.get().origin(), stack);
        }
        SoundType sound = origin.getSoundType(level, plan.get().origin(), player);
        level.playSound(null, plan.get().origin(), sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        level.gameEvent(GameEvent.BLOCK_PLACE, plan.get().origin(), GameEvent.Context.of(player, origin));
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
