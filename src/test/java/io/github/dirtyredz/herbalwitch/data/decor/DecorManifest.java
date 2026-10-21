package io.github.dirtyredz.herbalwitch.data.decor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The decor feature's expected generated files and English names, written out independently of the
 * mod's catalogue: five pieces, one design, ids {@code decor/<family>/<piece>}.
 */
public final class DecorManifest {
    /** piece -> family directory, in tab order. */
    public static final Map<String, String> PIECES = linked("armchair", "seating", "apothecary_counter", "surfaces",
            "small_wall_shelf", "storage", "table_lamp", "lighting", "small_rug", "textiles");
    public static final Map<String, String> NAMES = Map.of("armchair", "Armchair", "apothecary_counter", "Apothecary Counter",
            "small_wall_shelf", "Small Wall Shelf", "table_lamp", "Table Lamp", "small_rug", "Small Rug");
    /** piece -> authored template (lamps add _off / _on). */
    public static final Map<String, String> TEMPLATES = Map.of("armchair", "armchair", "apothecary_counter", "apothecary_counter",
            "small_wall_shelf", "wall_shelf", "table_lamp", "table_lamp", "small_rug", "rug");

    private DecorManifest() {
    }

    private static Map<String, String> linked(String... pairs) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }

    /** {@code decor/<family>/<piece>} for the five pieces. */
    public static List<String> ids() {
        return PIECES.entrySet().stream().map(e -> "decor/" + e.getValue() + "/" + e.getKey()).toList();
    }

    public static String pieceOf(String id) {
        return id.substring(id.lastIndexOf('/') + 1);
    }

    public static Set<String> files() {
        Set<String> expected = new TreeSet<>();
        for (String id : ids()) {
            expected.add("assets/herbalwitch/blockstates/" + id + ".json");
            expected.add("assets/herbalwitch/models/item/" + id + ".json");
            expected.add("data/herbalwitch/loot_tables/blocks/" + id + ".json");
            expected.add("data/herbalwitch/recipes/" + id + ".json");
            expected.add("data/herbalwitch/advancements/recipes/decorations/" + id + ".json");
        }
        expected.add("assets/herbalwitch/lang/en_us.json");
        expected.add("data/herbalwitch/tags/blocks/furniture.json");
        expected.add("data/herbalwitch/tags/items/furniture.json");
        expected.add("data/minecraft/tags/blocks/mineable/axe.json");
        return expected;
    }

    /** Every decor language key and its English text. */
    public static Map<String, String> lang() {
        Map<String, String> lang = new LinkedHashMap<>();
        lang.put("itemGroup.herbalwitch.decor", "The Herbal Witch: Cottage Decor");
        for (String id : ids()) {
            lang.put("block.herbalwitch." + id.replace('/', '.'), NAMES.get(pieceOf(id)));
        }
        return lang;
    }
}
