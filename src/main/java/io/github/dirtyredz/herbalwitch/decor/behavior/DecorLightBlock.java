package io.github.dirtyredz.herbalwitch.decor.behavior;

import io.github.dirtyredz.herbalwitch.decor.structure.DecorAssemblyBlock;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A lamp (and the firepit): unlit when placed, switched on and off by hand on the server, light
 * {@value #LIGHT} on every cell while lit. The state is an ordinary block property, saved with the
 * chunk and sent to every client by the normal block update; a multiblock switches all its cells
 * together. No fuel, no heat-source tag, no damage, no fire spread.
 */
public class DecorLightBlock extends DecorAssemblyBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final int LIGHT = 14;

    public DecorLightBlock(FurnitureSpec spec, BlockBehaviour.Properties properties) {
        super(spec, properties.lightLevel(DecorLightBlock::lightEmission));
        registerDefaultState(defaultBlockState().setValue(LIT, false));
    }

    /** Light emitted in a state. */
    public static int lightEmission(BlockState state) {
        return state.getValue(LIT) ? LIGHT : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }

    @Override
    public BlockState cellState(BlockState originState, int index) {
        return super.cellState(originState, index).setValue(LIT, originState.getValue(LIT));
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling, the vanilla use hook
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            boolean lit = !state.getValue(LIT);
            if (layout() == null) {
                level.setBlock(pos, state.setValue(LIT, lit), Block.UPDATE_ALL);
            } else {
                MultiblockStructure.setOnAll(level, origin(state, pos), layout(), LIT, lit, Block.UPDATE_ALL);
            }
            level.playSound(null, pos, lit ? SoundEvents.WOODEN_BUTTON_CLICK_ON : SoundEvents.WOODEN_BUTTON_CLICK_OFF,
                    SoundSource.BLOCKS, 0.6F, 1.2F);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
