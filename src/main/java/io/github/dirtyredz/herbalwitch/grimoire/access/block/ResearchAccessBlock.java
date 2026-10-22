package io.github.dirtyredz.herbalwitch.grimoire.access.block;

import io.github.dirtyredz.herbalwitch.grimoire.catalog.GrimoireCatalog;
import io.github.dirtyredz.herbalwitch.grimoire.access.menu.GrimoireAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/**
 * A research block (the grimoire stand and the witch's bookshelf): one cell, facing whoever placed
 * it, standing on a surface that supports its centre, and opening the user's own grimoire reader
 * with research and teaching. It stores no items, holds no block entity and runs no job; the
 * modelled book is part of the piece and knowledge belongs to the player. Losing its support drops
 * it once, like any block broken by hand.
 */
public class ResearchAccessBlock extends HorizontalDirectionalBlock {
    private final GrimoireCatalog.ResearchPiece piece;
    private final Map<Direction, VoxelShape> shapes;

    public ResearchAccessBlock(Properties properties, GrimoireCatalog.ResearchPiece piece, Map<Direction, VoxelShape> shapes) {
        super(properties);
        this.piece = piece;
        this.shapes = Map.copyOf(shapes);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public GrimoireCatalog.ResearchPiece piece() {
        return piece;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor level,
                                  BlockPos pos, BlockPos neighbourPos) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState(); // the world drops the block through its loot table
        }
        return super.updateShape(state, direction, neighbour, level, pos, neighbourPos);
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(state.getValue(FACING));
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            GrimoireAccess.block(pos, piece.opens()).open(serverPlayer);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
