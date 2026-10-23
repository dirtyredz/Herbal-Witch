package io.github.dirtyredz.herbalwitch.data.server.familiars;

import io.github.dirtyredz.herbalwitch.companions.familiars.ability.retrieval.FamiliarRetrievalRules;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyTags;
import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchItemTagsProvider;
import net.minecraft.world.item.Items;

public final class FamiliarRetrievalTags {
    private FamiliarRetrievalTags() {}
    public static void addTo(HerbalWitchItemTagsProvider.Sink sink) {
        sink.tag(FamiliarRetrievalRules.SMALL_OBJECTS).addTag(BotanyTags.FRESH_HERBS).addTag(BotanyTags.SEEDS)
                .add(Items.STICK, Items.STRING, Items.FEATHER, Items.FLINT, Items.WHEAT_SEEDS, Items.PUMPKIN_SEEDS,
                        Items.MELON_SEEDS, Items.BEETROOT_SEEDS, Items.IRON_NUGGET, Items.COPPER_INGOT, Items.COAL, Items.CLAY_BALL);
    }
}
