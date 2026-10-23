package io.github.dirtyredz.herbalwitch.companions.familiars.catalog;

import java.util.List;
import java.util.Optional;

public final class FamiliarCatalog {
    public static final List<FamiliarSpeciesSpec> ALL = List.of(
            new FamiliarSpeciesSpec("cat", "minecraft:cat", "fish", 100, 5, false, 1.1, 0, "cat_bell"),
            new FamiliarSpeciesSpec("raven", "herbalwitch:familiars/entity/raven", "seed", 80, 4, true, 1, 0, "raven_satchel"),
            new FamiliarSpeciesSpec("owl", "herbalwitch:familiars/entity/owl", "meat", 80, 4, true, 1, 0, "owl_lens"),
            new FamiliarSpeciesSpec("fox", "minecraft:fox", "berry", 80, 4, false, 1.1, 0, "fox_pouch"),
            new FamiliarSpeciesSpec("wolf", "minecraft:wolf", "meat", 100, 5, false, 1.1, 4, "wolf_harness"),
            new FamiliarSpeciesSpec("snake", "herbalwitch:familiars/entity/snake", "meat", 80, 4, false, 1, 2, "snake_ring"),
            new FamiliarSpeciesSpec("rabbit", "minecraft:rabbit", "root", 80, 4, false, 1, 0, "rabbit_sachet"),
            new FamiliarSpeciesSpec("bat", "minecraft:bat", "berry", 60, 3, true, 1, 0, "bat_chime"));
    private FamiliarCatalog() {}
    public static Optional<FamiliarSpeciesSpec> find(String key) { return ALL.stream().filter(s -> s.key().equals(key)).findFirst(); }
    public static Optional<FamiliarSpeciesSpec> type(String id) { return ALL.stream().filter(s -> s.originalType().equals(id) || s.entityId().equals(id)).findFirst(); }
}
