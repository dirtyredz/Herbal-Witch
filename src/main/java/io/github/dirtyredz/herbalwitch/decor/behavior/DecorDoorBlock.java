package io.github.dirtyredz.herbalwitch.decor.behavior;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.structure.FurnitureLayouts;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.platform.placement.StructurePlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * The cottage door: vanilla's {@link DoorBlock} in every behaviour (facing, hinge chosen from where
 * the player aims, open by hand or redstone, both halves together, vanilla collision, one drop from
 * the lower half), with authored art. Placement goes through the checked structure placement, so
 * the preview shows the hinge the door will have and both halves appear together or not at all.
 */
public class DecorDoorBlock extends DoorBlock implements StructurePlacement {
    private final MultiblockLayout layout;

    public DecorDoorBlock(FurnitureSpec spec, Properties properties) {
        super(properties, BlockSetType.DARK_OAK);
        this.layout = FurnitureLayouts.of(spec);
    }

    @Override
    public MultiblockLayout placementLayout() {
        return layout; // two cells, lower then upper; the halves are vanilla's HALF, not a cell index
    }

    @Override
    public Direction layoutFacing(BlockState originState) {
        return originState.getValue(FACING);
    }

    @Override
    public BlockState cellState(BlockState originState, int index) {
        return originState.setValue(HALF, index == 0 ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER);
    }

    @Override
    public boolean supported(LevelReader level, BlockPos pos, BlockState cellState) {
        if (cellState.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return true;
        }
        BlockPos below = pos.below();
        return level.hasChunkAt(below) && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        // both halves are already placed by the checked placement
    }
}
