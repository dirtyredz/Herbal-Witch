package io.github.dirtyredz.herbalwitch.data.server.compat;

import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchBlockTagsProvider;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;

/**
 * Reciprocal heat for Farmer's Delight 1.3.4: its HeatableBlockEntity treats anything in
 * {@code farmersdelight:heat_sources} as heat, honouring {@code lit}, so a lit hearth heats its
 * cooking pot and skillet. The tag file is inert when Farmer's Delight is absent.
 */
public final class CompatibilityTagContributions {
    public static final ResourceLocation FD_HEAT_SOURCES = ResourceLocation.fromNamespaceAndPath("farmersdelight", "heat_sources");

    private CompatibilityTagContributions() {
    }

    public static void blocks(HerbalWitchBlockTagsProvider.Sink tags) {
        tags.tag(BlockTags.create(FD_HEAT_SOURCES)).add(StationBlocks.HEARTH.get());
    }
}
