package io.github.dirtyredz.herbalwitch.apothecary.stations.heat;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/** Data-driven heat: what heats a vessel directly, and what passes heat up from a source below it. */
public final class HeatSourceTags {
    /** Lit hearth, lit campfires, magma, and (optionally) a lit Farmer's Delight stove. */
    public static final TagKey<Block> HEAT_SOURCES = BlockTags.create(id("heat_sources"));
    /** At most one of these between a vessel and its source (a hopper by default). */
    public static final TagKey<Block> HEAT_CONDUCTORS = BlockTags.create(id("heat_conductors"));

    private HeatSourceTags() {
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, path);
    }
}
