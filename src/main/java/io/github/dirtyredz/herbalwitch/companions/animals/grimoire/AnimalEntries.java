package io.github.dirtyredz.herbalwitch.companions.animals.grimoire;

import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalTags;
import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalProfiles;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.TrustRules;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContentSource;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Public ordinary-animal guidance owned by the feature; processing pages remain live recipe projections. */
public final class AnimalEntries implements GrimoireContentSource {
    public static final int PRIORITY = 1100;
    public static final EntryKey KEY = EntryKey.of(EntryKey.Kind.GUIDE, "herbalwitch:animals/care");
    @Override public void contribute(GrimoireContent.Inputs inputs, Consumer<GrimoireEntry> out) {
        var body = new ArrayList<Component>();
        body.add(Component.translatable("animal.herbalwitch.guide.feeding", TrustRules.FEED_GAIN, TrustRules.FEED_INTERVAL / 20));
        body.add(Component.translatable("animal.herbalwitch.guide.adoption", 80, 4, 100, 5));
        body.add(Component.translatable("animal.herbalwitch.guide.damage", TrustRules.HURT_LOSS, TrustRules.HURT_LOCKOUT / 20));
        body.add(Component.translatable("animal.herbalwitch.guide.care", 4, TrustRules.HEAL_INTERVAL / 20, TrustRules.CALM_TICKS / 20));
        body.add(Component.translatable("animal.herbalwitch.guide.limits"));
        body.add(Component.translatable("animal.herbalwitch.guide.transport"));
        body.add(Component.translatable("animal.herbalwitch.guide.recovery"));
        var rules = io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalServerConfig.current();
        for (var profile : AnimalProfiles.ALL) {
            var type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(profile.id()));
            if (type != null && type.is(AnimalTags.ELIGIBLE) && !type.is(AnimalTags.EXCLUDED)) {
                var groups = io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalEligibility.baitGroups(type);
                if (groups.size() == 1) {
                    body.add(Component.translatable("animal.herbalwitch.guide.species", type.getDescription(),
                        AnimalItems.bait(groups.iterator().next()).getDescription(),
                        Component.translatable("animal.herbalwitch.guide." + (profile.nativeTaming() ? "native" : profile.adoptable() ? "keeper" : "bat"))));
                    body.add(Component.translatable("animal.herbalwitch.guide.actions",
                            available(rules.relationships() && rules.attraction() && profile.goalAi() && type.is(AnimalTags.ATTRACTABLE)),
                            available(rules.relationships() && rules.calming() && profile.goalAi() && type.is(AnimalTags.CALMABLE)),
                            available(rules.relationships() && rules.adoption() && profile.adoptable()),
                            available(rules.relationships() && rules.captures() && profile.capturable() && type.is(AnimalTags.CAPTURABLE))));
                }
            }
        }
        var links = new ArrayList<EntryKey>();
        for (Bait bait : Bait.values()) links.add(EntryKey.of(EntryKey.Kind.RECIPE, "herbalwitch:processing/animals/grinding/" + bait.id()));
        links.add(EntryKey.of(EntryKey.Kind.RECIPE, "herbalwitch:processing/animals/grinding/calming_powder"));
        links.add(EntryKey.of(EntryKey.Kind.RECIPE, "herbalwitch:processing/animals/charm_work/capture_charm"));
        out.accept(GrimoireEntry.page(KEY, GrimoireContent.GUIDE, Component.translatable("animal.herbalwitch.title"),
                AnimalItems.bait(Bait.GRAIN).getDefaultInstance(), body, links));
    }
    private static Component available(boolean value) { return Component.translatable("animal.herbalwitch.guide." + (value ? "enabled" : "disabled")); }
}
