package io.github.dirtyredz.herbalwitch.apothecary.stations.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Whether heat reaches a vessel: a tagged source directly below, or one tagged conductor below
 * that has a tagged source below it. A source with a {@code lit} property must be lit; a waterlogged
 * source gives no heat. Reads loaded positions only (never loads a chunk); two block reads at most.
 */
public final class HeatSources {
    private HeatSources() {
    }

    public static boolean isHeated(Level level, BlockPos vessel) {
        BlockPos below = vessel.below();
        if (!level.isLoaded(below)) {
            return false;
        }
        BlockState state = level.getBlockState(below);
        if (state.is(HeatSourceTags.HEAT_SOURCES)) {
            return gives(state);
        }
        if (state.is(HeatSourceTags.HEAT_CONDUCTORS)) {
            BlockPos source = below.below();
            return level.isLoaded(source) && level.getBlockState(source).is(HeatSourceTags.HEAT_SOURCES)
                    && gives(level.getBlockState(source));
        }
        return false;
    }

    static boolean gives(BlockState source) {
        if (source.hasProperty(BlockStateProperties.WATERLOGGED) && source.getValue(BlockStateProperties.WATERLOGGED)) {
            return false;
        }
        return !source.hasProperty(BlockStateProperties.LIT) || source.getValue(BlockStateProperties.LIT);
    }

    /** What a vessel may stand on: a sturdy surface, a heat source or a conductor. */
    public static boolean supports(LevelReader level, BlockPos below) {
        BlockState state = level.getBlockState(below);
        return state.is(HeatSourceTags.HEAT_SOURCES) || state.is(HeatSourceTags.HEAT_CONDUCTORS)
                || state.isFaceSturdy(level, below, Direction.UP, SupportType.CENTER);
    }
}
