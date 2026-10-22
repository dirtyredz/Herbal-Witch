package io.github.dirtyredz.herbalwitch.data.server.grimoire;

import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchBlockTagsProvider;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireBlocks;
import net.minecraft.tags.BlockTags;

/** The research blocks are wooden: axes mine them. */
public final class GrimoireTagContributions {
    private GrimoireTagContributions() {
    }

    public static void blocks(HerbalWitchBlockTagsProvider.Sink tags) {
        tags.tag(BlockTags.MINEABLE_WITH_AXE).add(GrimoireBlocks.STAND.get(), GrimoireBlocks.BOOKSHELF.get());
    }
}
