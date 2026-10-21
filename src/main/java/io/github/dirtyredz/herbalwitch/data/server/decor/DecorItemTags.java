package io.github.dirtyredz.herbalwitch.data.server.decor;

import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchItemTagsProvider;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorTags;

/** {@code herbalwitch:furniture} copied from the block tag, plus one {@code herbalwitch:decor/<piece>} tag per piece. */
public final class DecorItemTags {
    private DecorItemTags() {
    }

    public static void addTo(HerbalWitchItemTagsProvider.Sink tags) {
        tags.copy(DecorTags.FURNITURE, DecorTags.FURNITURE_ITEMS);
        for (DecorBlocks.Entry entry : DecorBlocks.entries()) {
            tags.tag(DecorTags.pieceItems(entry.spec())).add(entry.block().get().asItem());
        }
    }
}
