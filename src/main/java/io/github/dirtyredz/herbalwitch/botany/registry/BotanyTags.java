package io.github.dirtyredz.herbalwitch.botany.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbHabitat;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

/** Herbal Witch's own botany tags. */
public final class BotanyTags {
    /** Every herb plant. */
    public static final TagKey<Block> PLANTS = BlockTags.create(id("botany/plants"));
    /** Blocks whose player-harvest may turn up herb seeds (grass and ferns by default). */
    public static final TagKey<Block> FORAGE_SOURCES = BlockTags.create(id("botany/forage_sources"));
    public static final TagKey<Item> SEEDS = ItemTags.create(id("botany/seeds"));
    public static final TagKey<Item> FRESH_HERBS = ItemTags.create(id("botany/fresh_herbs"));
    /** Forge's common seeds tag. */
    public static final TagKey<Item> FORGE_SEEDS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", "seeds"));

    private BotanyTags() {
    }

    /** {@code herbalwitch:botany/habitat/<group>}: biomes where the group grows wild and is discovered. */
    public static TagKey<Biome> habitat(HerbHabitat habitat) {
        return TagKey.create(Registries.BIOME, id("botany/habitat/" + habitat.directory()));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, path);
    }
}
