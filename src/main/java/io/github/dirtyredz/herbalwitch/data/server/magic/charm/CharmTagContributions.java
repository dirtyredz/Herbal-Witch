package io.github.dirtyredz.herbalwitch.data.server.magic.charm;

import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchItemTagsProvider;
import io.github.dirtyredz.herbalwitch.magic.charm.registry.CharmItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Charms may sit in Curios' {@code charm} slot type: the {@code curios:charm} item tag lists them (a
 * tag Curios reads when installed and that nothing reads otherwise; data/herbalwitch/curios/entities
 * gives players that slot).
 */
public final class CharmTagContributions {
    public static final TagKey<Item> CURIOS_CHARM = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("curios", "charm"));

    private CharmTagContributions() {
    }

    public static void items(HerbalWitchItemTagsProvider.Sink tags) {
        CharmItems.all().forEach(item -> tags.tag(CURIOS_CHARM).add(item));
    }
}
