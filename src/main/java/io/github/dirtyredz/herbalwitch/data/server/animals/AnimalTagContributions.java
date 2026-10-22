package io.github.dirtyredz.herbalwitch.data.server.animals;

import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalTags;
import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalProfiles;
import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchEntityTagsProvider;
import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public final class AnimalTagContributions {
    private AnimalTagContributions() {}
    public static void entities(HerbalWitchEntityTagsProvider.Sink sink) {
        sink.tag(AnimalTags.EXCLUDED);
        for (var profile : AnimalProfiles.ALL) {
            var type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(profile.id()));
            sink.tag(AnimalTags.ELIGIBLE).add(type);
            sink.tag(AnimalTags.bait(profile.bait())).add(type);
            if (profile.capturable()) sink.tag(AnimalTags.CAPTURABLE).add(type);
            if (profile.goalAi()) {
                sink.tag(AnimalTags.ATTRACTABLE).add(type);
                sink.tag(AnimalTags.CALMABLE).add(type);
            }
        }
    }

    public static void items(HerbalWitchItemTagsProvider.Sink sink) {
        sink.tag(AnimalTags.food(Bait.GRAIN)).add(Items.WHEAT);
        sink.tag(AnimalTags.food(Bait.ROOT)).add(Items.CARROT, Items.BEETROOT);
        sink.tag(AnimalTags.food(Bait.SEED)).add(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);
        sink.tag(AnimalTags.food(Bait.FISH)).add(Items.COD, Items.SALMON, Items.TROPICAL_FISH);
        sink.tag(AnimalTags.food(Bait.MEAT)).add(Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.MUTTON, Items.RABBIT);
        sink.tag(AnimalTags.food(Bait.BERRY)).add(Items.SWEET_BERRIES, Items.GLOW_BERRIES, Items.APPLE);
    }
}
