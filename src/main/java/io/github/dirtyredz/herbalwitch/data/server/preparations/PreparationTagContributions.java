package io.github.dirtyredz.herbalwitch.data.server.preparations;

import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationBlocks;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationTags;
import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchBlockTagsProvider;
import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchItemTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

/** Bundle and powder tags, resin sources (spruce leaves), and the lantern's pickaxe tag. */
public final class PreparationTagContributions {
    private PreparationTagContributions() {
    }

    public static void blocks(HerbalWitchBlockTagsProvider.Sink tags) {
        for (HerbSpec herb : HerbCatalog.ALL) {
            tags.tag(PreparationTags.BUNDLE_BLOCKS).add(PreparationBlocks.bundle(herb));
        }
        tags.tag(PreparationTags.RESIN_SOURCES).add(Blocks.SPRUCE_LEAVES);
        tags.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(PreparationBlocks.OIL_LANTERN.get());
    }

    public static void items(HerbalWitchItemTagsProvider.Sink tags) {
        tags.copy(PreparationTags.BUNDLE_BLOCKS, PreparationTags.BUNDLES);
        for (HerbSpec herb : HerbCatalog.ALL) {
            tags.tag(PreparationTags.POWDERS).add(PreparationItems.powder(herb));
        }
    }
}
