package io.github.dirtyredz.herbalwitch.companions.familiars.catalog;

import java.util.List;
import java.util.Optional;

public final class FamiliarCatalog {
    public static final List<FamiliarSpeciesSpec> ALL = List.of(
            new FamiliarSpeciesSpec("cat", "minecraft:cat", "fish", 100, 5, false, 1.1, 0, "cat_bell", false),
            new FamiliarSpeciesSpec("raven", "herbalwitch:familiars/entity/raven", "seed", 80, 4, true, 1, 0, "raven_satchel", false),
            new FamiliarSpeciesSpec("owl", "herbalwitch:familiars/entity/owl", "meat", 80, 4, true, 1, 0, "owl_lens", false),
            new FamiliarSpeciesSpec("fox", "minecraft:fox", "berry", 80, 4, false, 1.1, 0, "fox_pouch", true),
            new FamiliarSpeciesSpec("wolf", "minecraft:wolf", "meat", 100, 5, false, 1.1, 4, "wolf_harness", false),
            new FamiliarSpeciesSpec("snake", "herbalwitch:familiars/entity/snake", "meat", 80, 4, false, 1, 2, "snake_ring", false),
            new FamiliarSpeciesSpec("rabbit", "minecraft:rabbit", "root", 80, 4, false, 1, 0, "rabbit_sachet", false),
            new FamiliarSpeciesSpec("bat", "minecraft:bat", "berry", 60, 3, true, 1, 0, "bat_chime", false));
    private FamiliarCatalog() {}
    private static final java.util.Map<String, FamiliarSpeciesSpec> BY_KEY = ALL.stream().collect(java.util.stream.Collectors.toUnmodifiableMap(FamiliarSpeciesSpec::key, s -> s));
    public static Optional<FamiliarSpeciesSpec> find(String key) { return Optional.ofNullable(key == null ? null : BY_KEY.get(key)); }
    public static Optional<FamiliarSpeciesSpec> type(String id) { return ALL.stream().filter(s -> s.originalType().equals(id) || s.entityId().equals(id)).findFirst(); }
}
