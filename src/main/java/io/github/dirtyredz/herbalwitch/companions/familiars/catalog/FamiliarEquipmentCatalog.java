package io.github.dirtyredz.herbalwitch.companions.familiars.catalog;

import java.util.List;
import java.util.Optional;

/** Permanent item identities and compatibility, independent of registry initialization. */
public final class FamiliarEquipmentCatalog {
    public record Gear(String key, String species, String name, String ingredient) {
        public String path() { return "familiars/equipment/" + key; }
        public boolean accepts(String candidate) { return species.equals(candidate); }
    }
    public static final List<Gear> ALL = List.of(
            new Gear("cat_bell", "cat", "Cat Bell", "minecraft:gold_nugget"),
            new Gear("raven_satchel", "raven", "Raven Satchel", "minecraft:rabbit_hide"),
            new Gear("owl_lens", "owl", "Owl Lens", "minecraft:glass_pane"),
            new Gear("fox_pouch", "fox", "Fox Pouch", "minecraft:sweet_berries"),
            new Gear("wolf_harness", "wolf", "Wolf Harness", "minecraft:iron_ingot"),
            new Gear("snake_ring", "snake", "Snake Ring", "minecraft:copper_ingot"),
            new Gear("rabbit_sachet", "rabbit", "Rabbit Sachet", "herbalwitch:preparations/herbs/temperate/chamomile/bundle"),
            new Gear("bat_chime", "bat", "Bat Chime", "minecraft:amethyst_shard"));
    private FamiliarEquipmentCatalog() {}
    public static Optional<Gear> find(String key) { return ALL.stream().filter(gear -> gear.key().equals(key)).findFirst(); }
}
