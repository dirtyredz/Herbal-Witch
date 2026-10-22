package io.github.dirtyredz.herbalwitch.data.server.botany;

import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyBlocks;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyTags;
import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchBlockTagsProvider;
import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchItemTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

/**
 * Herb plants are crops (bees, farmland upkeep: {@code maintains_farmland} keeps dry farmland under
 * a herb from turning to dirt); grass and ferns are forage sources; the bed is axe-mineable.
 */
public final class BotanyTagContributions {
    private BotanyTagContributions() {
    }

    public static void blocks(HerbalWitchBlockTagsProvider.Sink tags) {
        for (HerbSpec herb : HerbCatalog.ALL) {
            tags.tag(BotanyTags.PLANTS).add(BotanyBlocks.plant(herb));
        }
        tags.tag(BlockTags.CROPS).addTag(BotanyTags.PLANTS);
        tags.tag(BlockTags.MAINTAINS_FARMLAND).addTag(BotanyTags.PLANTS);
        tags.tag(BotanyTags.FORAGE_SOURCES).add(Blocks.GRASS, Blocks.TALL_GRASS, Blocks.FERN, Blocks.LARGE_FERN);
        tags.tag(BlockTags.MINEABLE_WITH_AXE).add(BotanyBlocks.herbBed());
    }

    public static void items(HerbalWitchItemTagsProvider.Sink tags) {
        for (HerbSpec herb : HerbCatalog.ALL) {
            tags.tag(BotanyTags.SEEDS).add(BotanyItems.seeds(herb));
            tags.tag(BotanyTags.FRESH_HERBS).add(BotanyItems.fresh(herb));
        }
        tags.tag(BotanyTags.FORGE_SEEDS).addTag(BotanyTags.SEEDS);
    }
}
