package io.github.dirtyredz.herbalwitch.data.server.familiars;

import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarEquipmentCatalog;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarItems;
import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchItemTagsProvider;
import io.github.dirtyredz.herbalwitch.magic.charm.registry.CharmItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;

/** Exact supported memberships; tags never invent a species or charge adapter. */
public final class FamiliarEquipmentTags {
    private FamiliarEquipmentTags() {}
    public static void addTo(HerbalWitchItemTagsProvider.Sink sink) {
        for (var gear : FamiliarEquipmentCatalog.ALL)
            sink.tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("herbalwitch", "familiars/equipment/" + gear.species())))
                    .add(FamiliarItems.gear(gear.key()));
        var charms = sink.tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("herbalwitch", "familiars/charms")));
        CharmItems.all().forEach(charms::add);
    }
}
