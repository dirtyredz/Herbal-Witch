package io.github.dirtyredz.herbalwitch.data.server.magic.ritual;

import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchBlockTagsProvider;
import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualBlocks;
import net.minecraft.tags.BlockTags;

/** The altar is mostly dark wood (axes); the circle is stone (pickaxes). */
public final class AltarTagContributions {
    private AltarTagContributions() {
    }

    public static void blocks(HerbalWitchBlockTagsProvider.Sink tags) {
        tags.tag(BlockTags.MINEABLE_WITH_AXE).add(RitualBlocks.ALTAR.get());
        tags.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(RitualBlocks.CIRCLE.get());
    }
}
