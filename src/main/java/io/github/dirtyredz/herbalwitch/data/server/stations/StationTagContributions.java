package io.github.dirtyredz.herbalwitch.data.server.stations;

import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchBlockTagsProvider;
import io.github.dirtyredz.herbalwitch.stations.heat.HeatSourceTags;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

/**
 * Tools for the stations, and the default heat contract: lit hearth and stove, lit campfires and magma heat a
 * vessel; a hopper may conduct. Farmer's Delight's stove and conductors are optional entries, so the
 * tags load without it.
 */
public final class StationTagContributions {
    private StationTagContributions() {
    }

    public static void blocks(HerbalWitchBlockTagsProvider.Sink tags) {
        tags.tag(BlockTags.MINEABLE_WITH_AXE).add(StationBlocks.DRYING_RACK.get(), StationBlocks.BOTTLING_BENCH.get(),
                StationBlocks.CHARM_WORKBENCH.get());
        tags.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(StationBlocks.MORTAR.get(), StationBlocks.KETTLE.get(),
                StationBlocks.HEARTH.get(), StationBlocks.STOVE.get(), StationBlocks.INFUSION_POT.get(), StationBlocks.CAULDRON.get(),
                StationBlocks.STILL.get());
        tags.tag(HeatSourceTags.HEAT_SOURCES).add(StationBlocks.HEARTH.get(), StationBlocks.STOVE.get(), Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.MAGMA_BLOCK)
                .addOptional(ResourceLocation.fromNamespaceAndPath("farmersdelight", "stove"));
        tags.tag(HeatSourceTags.HEAT_CONDUCTORS).add(Blocks.HOPPER)
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("farmersdelight", "heat_conductors"));
    }
}
