package io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block;

import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceSpec;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link.ShopFixtureHooks;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.platform.placement.StructurePlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

/**
 * A commerce piece's physical adapter: facing, the support its mount needs (the apothecary counter
 * below a register, a sturdy wall behind a shelf or board, a surface below a jar), authored shapes,
 * checked single-cell placement through {@link StructurePlacement}, and removal on lost support.
 * Interaction and removal are forwarded to the shop services ({@link ShopFixtureHooks}); the block
 * itself holds no commerce policy. Pistons cannot move it and its drop and pick-block are plain items.
 */
public abstract class ShopFixtureBlock extends BaseEntityBlock implements StructurePlacement {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final ResourceLocation COUNTER = ResourceLocation.fromNamespaceAndPath("herbalwitch", ShopPieceCatalog.COUNTER_PATH);
    private final ShopPieceSpec spec;
    private final Map<Direction, VoxelShape> shapes;

    protected ShopFixtureBlock(ShopPieceSpec spec, Properties properties) {
        super(properties);
        this.spec = spec;
        this.shapes = ShopShapes.of(spec);
    }

    public ShopPieceSpec spec() {
        return spec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        Direction facing = spec.mount() == ShopPieceSpec.Mount.WALL && face.getAxis().isHorizontal()
                ? face : context.getHorizontalDirection().getOpposite();
        return defaultBlockState().setValue(FACING, facing);
    }

    /** Whether {@code state} is the apothecary counter (looked up by id, so decor never depends on commerce). */
    public static boolean isCounter(BlockState state) {
        return COUNTER.equals(ForgeRegistries.BLOCKS.getKey(state.getBlock()));
    }

    @Override
    public MultiblockLayout placementLayout() {
        return null;
    }

    @Override
    public Direction layoutFacing(BlockState originState) {
        return originState.getValue(FACING);
    }

    @Override
    public BlockState cellState(BlockState originState, int index) {
        return originState;
    }

    @Override
    public boolean supported(LevelReader level, BlockPos pos, BlockState state) {
        return switch (spec.mount()) {
            case COUNTER_TOP -> level.hasChunkAt(pos.below()) && isCounter(level.getBlockState(pos.below()));
            case SURFACE -> level.hasChunkAt(pos.below()) && Block.canSupportCenter(level, pos.below(), Direction.UP);
            case WALL -> {
                Direction facing = state.getValue(FACING);
                BlockPos wall = pos.relative(facing.getOpposite());
                yield level.hasChunkAt(wall) && level.getBlockState(wall).isFaceSturdy(level, wall, facing);
            }
        };
    }

    private Direction supportDirection(BlockState state) {
        return spec.mount() == ShopPieceSpec.Mount.WALL ? state.getValue(FACING).getOpposite() : Direction.DOWN;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return supported(level, pos, state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction face, BlockState neighbour, LevelAccessor level, BlockPos pos, BlockPos other) {
        if (face == supportDirection(state) && !supported(level, pos, state)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, face, neighbour, level, pos, other);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(state.getValue(FACING));
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return ShopFixtureHooks.use(this, state, level, pos, player, hand);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && !level.isClientSide && level.getBlockEntity(pos) instanceof ShopFixtureBlockEntity fixture) {
            ShopFixtureHooks.removed(this, level, pos, fixture);
        }
        super.onRemove(state, level, pos, next, moving);
    }

    /** Creative pick-block never copies a binding. */
    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(this);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
