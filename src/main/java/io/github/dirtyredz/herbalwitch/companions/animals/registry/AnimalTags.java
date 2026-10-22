package io.github.dirtyredz.herbalwitch.companions.animals.registry;

import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public final class AnimalTags {
    public static final TagKey<EntityType<?>> ELIGIBLE = entity("eligible");
    public static final TagKey<EntityType<?>> CAPTURABLE = entity("capturable");
    public static final TagKey<EntityType<?>> EXCLUDED = entity("excluded");
    public static final TagKey<EntityType<?>> ATTRACTABLE = entity("ai/attractable");
    public static final TagKey<EntityType<?>> CALMABLE = entity("ai/calmable");

    private AnimalTags() {}
    public static TagKey<EntityType<?>> bait(Bait bait) { return entity("bait/" + bait.id()); }
    public static TagKey<Item> food(Bait bait) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("herbalwitch", "animals/food/" + bait.id()));
    }
    private static TagKey<EntityType<?>> entity(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("herbalwitch", "animals/" + path));
    }
}
