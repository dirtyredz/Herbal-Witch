package io.github.dirtyredz.herbalwitch.botany.crop;

import io.github.dirtyredz.herbalwitch.botany.garden.HerbBedBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Growth decisions shared by every herb, kept apart from harvesting. Random growth itself is
 * vanilla crop growth (one successful random tick advances one age, at ordinary crop light); this
 * class holds what differs: where a herb may stand and how far bone meal moves it.
 */
public final class HerbGrowth {
    private HerbGrowth() {
    }

    /** Where seeds may be planted: tilled farmland or a herb bed. */
    public static boolean isCultivatedSoil(BlockState below) {
        return below.is(Blocks.FARMLAND) || below.getBlock() instanceof HerbBedBlock;
    }

    /**
     * Where a herb survives: cultivated soil, or natural dirt-like ground for wild patches placed by
     * worldgen (seeds still need cultivated soil, see {@link HerbSeedItem}).
     */
    public static boolean canStandOn(BlockState below) {
        return isCultivatedSoil(below) || below.is(BlockTags.DIRT);
    }

    /** Bone meal advances one or two ages; the caller caps at maturity. */
    public static int bonemealIncrease(RandomSource random) {
        return 1 + random.nextInt(2);
    }
}
