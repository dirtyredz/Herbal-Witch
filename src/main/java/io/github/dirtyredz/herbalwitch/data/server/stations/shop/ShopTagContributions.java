package io.github.dirtyredz.herbalwitch.data.server.stations.shop;

import io.github.dirtyredz.herbalwitch.apothecary.shop.registry.ShopBlocks;
import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchBlockTagsProvider;
import net.minecraft.tags.BlockTags;

/** Commerce contributions to the sole block-tag writer: every piece is mined with an axe. */
public final class ShopTagContributions {
    private ShopTagContributions() {
    }

    public static void blocks(HerbalWitchBlockTagsProvider.Sink sink) {
        ShopBlocks.all().forEach(block -> sink.tag(BlockTags.MINEABLE_WITH_AXE).add(block));
    }
}
