package io.github.dirtyredz.herbalwitch.data.magic.charm;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The protective charms' expected generated files, recipes and language keys (Milestone 7), written
 * out independently of the charm catalogue and providers. Charm ids are {@code magic/charms/<id>}.
 */
public final class CharmManifest {
    /** Charm id -> English name, in catalogue order. */
    public static final Map<String, String> CHARMS = linked("protective_sachet", "Protective Sachet",
            "undead_amulet", "Undead Amulet", "arthropod_amulet", "Arthropod Amulet");
    /** Charm id -> energy of its workbench batch. */
    public static final Map<String, Integer> ENERGY = Map.of("protective_sachet", 8, "undead_amulet", 10, "arthropod_amulet", 10);
    /** The optional-mod item tag the charms join (Curios), in catalogue order. */
    public static final String CURIOS_TAG = "data/curios/tags/items/charm.json";

    private CharmManifest() {
    }

    private static Map<String, String> linked(String... pairs) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }

    public static String item(String id) {
        return "magic/charms/" + id;
    }

    /** The workbench's recipes under recipes/processing/. */
    public static Set<String> processingRecipes() {
        Set<String> recipes = new TreeSet<>();
        CHARMS.keySet().forEach(id -> recipes.add("charm_work/" + id));
        return recipes;
    }

    public static Set<String> files() {
        Set<String> files = new TreeSet<>();
        CHARMS.keySet().forEach(id -> files.add("assets/herbalwitch/models/item/" + item(id) + ".json"));
        files.add(CURIOS_TAG);
        return files;
    }

    public static Map<String, String> lang() {
        Map<String, String> lang = new LinkedHashMap<>();
        CHARMS.forEach((id, name) -> {
            lang.put("item.herbalwitch." + item(id).replace('/', '.'), name);
            lang.put("item.herbalwitch." + item(id).replace('/', '.') + ".tooltip", null);
        });
        lang.put("item.herbalwitch.magic.charms.wear", null);
        for (String p : List.of("title", "0", "amulets", "charge", "limits", "sachet", "wear")) {
            lang.put("grimoire.herbalwitch.witchcraft.charms." + p, null);
        }
        lang.put("showcase.herbalwitch.section.charms", null);
        for (String l : List.of("samples", "soften", "soften.detail", "wear", "wear.detail")) {
            lang.put("showcase.herbalwitch.label.charms." + l, null);
        }
        return lang;
    }
}
