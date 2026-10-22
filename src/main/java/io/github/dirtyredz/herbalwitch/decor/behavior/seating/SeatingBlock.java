package io.github.dirtyredz.herbalwitch.decor.behavior.seating;

import io.github.dirtyredz.herbalwitch.decor.structure.DecorAssemblyBlock;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Chairs, benches, sofas, the chaise and the swing: using any cell sits the player on the seat
 * nearest where they aimed ({@link SeatingService}). Stationary: no rocking, no swivel.
 */
public class SeatingBlock extends DecorAssemblyBlock {
    public SeatingBlock(FurnitureSpec spec, Properties properties) {
        super(spec, properties);
    }

    @Override
    @SuppressWarnings("deprecation") // overriding, not calling, the vanilla use hook
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return SeatingService.sit(this, state, level, pos, player, hit.getLocation());
    }
}
