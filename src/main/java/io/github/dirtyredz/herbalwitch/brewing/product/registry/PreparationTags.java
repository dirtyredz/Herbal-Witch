package io.github.dirtyredz.herbalwitch.brewing.product.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Preparation tags: bundles, powders, the pests the repellent keeps off, and resin sources. */
public final class PreparationTags {
    public static final TagKey<Item> BUNDLES = ItemTags.create(id("preparations/bundles"));
    public static final TagKey<Item> POWDERS = ItemTags.create(id("preparations/powders"));
    public static final TagKey<Block> BUNDLE_BLOCKS = BlockTags.create(id("preparations/bundles"));
    /** Silverfish and endermites by default. */
    public static final TagKey<EntityType<?>> REPELLED_PESTS = TagKey.create(Registries.ENTITY_TYPE, id("repelled_pests"));
    /** Leaves that may drop resin when a player breaks them (spruce by default). */
    public static final TagKey<Block> RESIN_SOURCES = BlockTags.create(id("preparations/resin_sources"));

    private PreparationTags() {
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, path);
    }
}
