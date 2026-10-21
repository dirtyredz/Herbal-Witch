package io.github.dirtyredz.herbalwitch.decor.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Herbal Witch's own furniture tags, for packs, scripts and recipe viewers. */
public final class DecorTags {
    /** Every furniture block in every finish. */
    public static final TagKey<Block> FURNITURE = BlockTags.create(id("furniture"));
    public static final TagKey<Item> FURNITURE_ITEMS = ItemTags.create(id("furniture"));

    private DecorTags() {
    }

    /** {@code herbalwitch:decor/<piece>}: every finish of one piece, e.g. all 18 armchairs. */
    public static TagKey<Item> pieceItems(FurnitureSpec spec) {
        return ItemTags.create(id("decor/" + spec.id()));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, path);
    }
}
