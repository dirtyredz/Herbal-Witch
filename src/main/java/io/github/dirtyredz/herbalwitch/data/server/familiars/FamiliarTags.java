package io.github.dirtyredz.herbalwitch.data.server.familiars;

import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalTags;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarCatalog;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarEntities;
import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchEntityTagsProvider;
import java.util.Locale;

public final class FamiliarTags {
    private FamiliarTags() {}
    public static void entities(HerbalWitchEntityTagsProvider.Sink sink) {
        for (var s : FamiliarCatalog.ALL) {
            var type = FamiliarEntities.type(s.key());
            sink.tag(AnimalTags.ELIGIBLE).add(type); sink.tag(AnimalTags.bait(Bait.valueOf(s.bait().toUpperCase(Locale.ROOT)))).add(type);
            if (s.key().equals("raven") || s.key().equals("owl") || s.key().equals("snake")) {
                sink.tag(AnimalTags.ATTRACTABLE).add(type); sink.tag(AnimalTags.CALMABLE).add(type);
            }
        }
    }
}
