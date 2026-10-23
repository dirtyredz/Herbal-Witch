package io.github.dirtyredz.herbalwitch.grimoire.content;

import java.util.ArrayList;
import java.util.List;

/** Explicit M9 additions; preserve the independent pre-M8 and M8 order contracts. */
final class ContentOrderFamiliars {
    static final List<String> RECIPES = List.of("bat_chime", "cat_bell", "fox_pouch", "owl_lens", "rabbit_sachet", "raven_satchel", "snake_ring", "wolf_harness")
            .stream().map(id -> "recipe|herbalwitch:processing/charm_work/familiars/equipment/" + id).toList();
    static final List<String> GUIDES = List.of("species", "commands", "homes", "equipment", "abilities", "preparations", "progression", "recovery")
            .stream().map(id -> "guide|herbalwitch:familiars/" + id).toList();
    static final List<String> ADDITIONS = java.util.stream.Stream.concat(GUIDES.stream(), RECIPES.stream()).toList();
    private ContentOrderFamiliars() {}
    static List<String> expected() {
        var expected = new ArrayList<>(ContentOrderAnimals.expected());
        expected.addAll(expected.indexOf("recipe|herbalwitch:processing/charm_work/protective_sachet"), RECIPES);
        expected.addAll(expected.indexOf("guide|herbalwitch:witchcraft/familiar") + 1, GUIDES);
        return List.copyOf(expected);
    }
}
