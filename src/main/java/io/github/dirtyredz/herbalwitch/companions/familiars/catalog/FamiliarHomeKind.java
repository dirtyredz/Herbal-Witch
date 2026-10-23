package io.github.dirtyredz.herbalwitch.companions.familiars.catalog;

import java.util.*;

/** Three floor-supported homes, with collision following their authored silhouettes. */
public enum FamiliarHomeKind {
    BED("bed", Set.of("cat", "fox", "wolf", "snake", "rabbit")),
    NEST("nest", Set.of("raven", "owl")),
    PERCH("perch", Set.of("raven", "owl", "bat"));
    private final String key;
    private final Set<String> species;
    FamiliarHomeKind(String key, Set<String> species) { this.key = key; this.species = species; }
    public String key() { return key; }
    public String path() { return "familiars/home/" + key; }
    public boolean accepts(String species) { return this.species.contains(species); }
}
