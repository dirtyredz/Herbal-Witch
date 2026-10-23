package io.github.dirtyredz.herbalwitch.companions.familiars.config;

import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarCatalog;
import java.util.Set;

/** Validated complete snapshot. Switches affect acquisition, never erase existing identities. */
public record FamiliarRules(boolean binding, Set<String> species, int mobile, int recordBytes, int transactionInterval,
                            int scans, int paths, int guardRadius, int senseRadius, int offlineGuards, boolean publicRetrieval) {
    public static final FamiliarRules DEFAULTS = new FamiliarRules(true,
            Set.of("cat", "raven", "owl", "fox", "wolf", "snake", "rabbit", "bat"), 3, 128 * 1024, 5, 8, 4, 16, 32, 8, false);
    public FamiliarRules {
        species = Set.copyOf(species);
        if (species.stream().anyMatch(s -> FamiliarCatalog.find(s).isEmpty())) throw new IllegalArgumentException("Unknown familiar species switch");
        bound(mobile, 1, 8); bound(recordBytes, 4096, 128 * 1024); bound(transactionInterval, 5, 1200);
        bound(scans, 1, 32); bound(paths, 1, 16); bound(guardRadius, 1, 24); bound(senseRadius, 1, 48); bound(offlineGuards, 1, 8);
    }
    private static void bound(int value, int minimum, int maximum) {
        if (value < minimum || value > maximum) throw new IllegalArgumentException("Familiar rule outside hard bounds");
    }
}
