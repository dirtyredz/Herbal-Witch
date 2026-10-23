package io.github.dirtyredz.herbalwitch.companions.familiars.home;

import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarHomeKind;
import io.github.dirtyredz.herbalwitch.companions.familiars.menu.FamiliarRosterMenu;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.platform.placement.StructurePlacement;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class FamiliarHomeBlock extends BaseEntityBlock implements StructurePlacement {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private final FamiliarHomeKind kind;
    public FamiliarHomeBlock(FamiliarHomeKind kind, Properties properties) { super(properties); this.kind = kind; registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)); }
    public FamiliarHomeKind kind() { return kind; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
    @Override public MultiblockLayout placementLayout() { return null; }
    @Override public Direction layoutFacing(BlockState state) { return state.getValue(FACING); }
    @Override public BlockState cellState(BlockState origin, int index) { return origin; }
    @Override public boolean supported(LevelReader level, BlockPos pos, BlockState state) {
        var below = pos.below(); return level.hasChunkAt(below) && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }
    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) { return supported(level, pos, state); }
    @Override public BlockState updateShape(BlockState state, Direction face, BlockState neighbour, LevelAccessor level, BlockPos pos, BlockPos other) {
        return face == Direction.DOWN && !supported(level, pos, state) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, face, neighbour, level, pos, other);
    }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return FamiliarHomeShapes.SHAPES.get(kind).get(state.getValue(FACING)); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FamiliarHomeBlockEntity(pos, state); }
    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide && placer instanceof Player && level.getBlockEntity(pos) instanceof FamiliarHomeBlockEntity home) home.owner(placer.getUUID());
    }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!GrimoireItems.isGrimoire(player.getItemInHand(hand))) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer owner) || !(level.getBlockEntity(pos) instanceof FamiliarHomeBlockEntity home)
                || !FamiliarHomeService.mayUse(owner, home, true)) return InteractionResult.FAIL;
        home.owner(owner.getUUID()); home.repair(); FamiliarRosterMenu.openHome(owner, home); return InteractionResult.CONSUME;
    }
    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && !level.isClientSide && level.getBlockEntity(pos) instanceof FamiliarHomeBlockEntity home) FamiliarHomeService.removed(home);
        super.onRemove(state, level, pos, next, moving);
    }
    @Override public BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override public BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
}
