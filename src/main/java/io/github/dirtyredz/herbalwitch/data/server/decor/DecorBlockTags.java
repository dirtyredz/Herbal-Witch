package io.github.dirtyredz.herbalwitch.data.server.decor;

import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchBlockTagsProvider;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorTags;
import net.minecraft.tags.BlockTags;

/**
 * Every furniture block is in {@code herbalwitch:furniture}; everything with a frame is axe-mineable
 * (the stone finish too: finishes are appearance only). Rugs break instantly like carpet.
 */
public final class DecorBlockTags {
    private DecorBlockTags() {
    }

    public static void addTo(HerbalWitchBlockTagsProvider.Sink tags) {
        for (DecorBlocks.Entry entry : DecorBlocks.entries()) {
            tags.tag(DecorTags.FURNITURE).add(entry.block().get());
            if (entry.spec().behavior() != FurnitureSpec.Behavior.RUG) {
                tags.tag(BlockTags.MINEABLE_WITH_AXE).add(entry.block().get());
            }
        }
    }
}
