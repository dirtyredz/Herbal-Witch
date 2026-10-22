package io.github.dirtyredz.herbalwitch.apothecary.brewing.product.lighting;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.ContainerReturns;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item.HerbalOilItem;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.OilApplication;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A small brass oil lantern standing on a surface. One bottle of lantern oil fills it for
 * {@link OilLanternBlockEntity#FUEL_TICKS} loaded ticks at light 14; it refuses more oil until it
 * runs dry (nothing is consumed), and the bottle comes back.
 */
public class OilLanternBlock extends BaseEntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final int LIGHT = 14;
    private static final VoxelShape SHAPE = Block.box(5, 0, 5, 11, 10, 11);

    public OilLanternBlock(Properties properties) {
        super(properties.lightLevel(state -> state.getValue(LIT) ? LIGHT : 0));
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor level,
                                  BlockPos pos, BlockPos neighbourPos) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighbour, level, pos, neighbourPos);
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof HerbalOilItem oil) || oil.oil() != OilApplication.Oil.LANTERN) {
            return InteractionResult.PASS;
        }
        if (!(level.getBlockEntity(pos) instanceof OilLanternBlockEntity lantern) || lantern.fuel() > 0) {
            return InteractionResult.FAIL; // still burning: refuse, consume nothing
        }
        if (!level.isClientSide) {
            lantern.refuel();
            level.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            player.setItemInHand(hand, ContainerReturns.finish(held, player, new ItemStack(Items.GLASS_BOTTLE)));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OilLanternBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != PreparationBlockEntities.OIL_LANTERN.get() || !state.getValue(LIT)) {
            return null;
        }
        return (tickLevel, tickPos, tickState, entity) -> ((OilLanternBlockEntity) entity).serverTick();
    }
}
