package io.github.dirtyredz.herbalwitch.companions.familiars.species;

import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import io.github.dirtyredz.herbalwitch.companions.animals.species.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarCatalog;
import java.util.Locale;

public final class FamiliarAnimalProfiles {
    private FamiliarAnimalProfiles() {}
    public static void register() {
        for (var s : FamiliarCatalog.ALL) AnimalProfiles.register(new AnimalProfile(s.entityId(), Bait.valueOf(s.bait().toUpperCase(Locale.ROOT)),
                s.key().equals("cat") || s.key().equals("wolf") ? AnimalProfile.Ownership.TAMEABLE
                        : s.key().equals("bat") ? AnimalProfile.Ownership.TRUST_ONLY : AnimalProfile.Ownership.KEEPER,
                s.key().equals("raven") || s.key().equals("owl") || s.key().equals("snake"), false));
    }
}
