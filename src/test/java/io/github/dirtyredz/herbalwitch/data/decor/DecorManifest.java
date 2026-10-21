package io.github.dirtyredz.herbalwitch.data.decor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The decor feature's expected generated files and English names, written out independently of the
 * mod's catalogue so a catalogue slip shows up as a diff.
 */
public final class DecorManifest {
    public static final List<String> NATURAL = List.of("dark_oak", "walnut", "cherry", "rosewood", "pale_oak", "spruce", "birch");
    public static final List<String> PAINTED = List.of("black", "stone", "moss", "sage_green", "forest_green", "plum",
            "burgundy", "dusty_pink", "blush", "cream", "lilac");
    /** piece -> family directory, in tab order. */
    public static final Map<String, String> PIECES = linked("armchair", "seating", "apothecary_counter", "surfaces",
            "small_wall_shelf", "storage", "table_lamp", "lighting", "small_rug", "textiles");
    public static final Map<String, String> NAMES = Map.of("armchair", "Armchair", "apothecary_counter", "Apothecary Counter",
            "small_wall_shelf", "Small Wall Shelf", "table_lamp", "Table Lamp", "small_rug", "Small Rug");

    private DecorManifest() {
    }

    private static Map<String, String> linked(String... pairs) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }

    /** {@code decor/<family>/<piece>/<palette>/<finish>} for every piece and finish: 90 ids. */
    public static List<String> ids() {
        List<String> ids = new ArrayList<>();
        PIECES.forEach((piece, family) -> {
            NATURAL.forEach(f -> ids.add("decor/" + family + "/" + piece + "/natural/" + f));
            PAINTED.forEach(f -> ids.add("decor/" + family + "/" + piece + "/painted/" + f));
        });
        return ids;
    }

    public static String finishOf(String id) {
        return id.substring(id.lastIndexOf('/') + 1);
    }

    public static String pieceOf(String id) {
        return id.split("/")[2];
    }

    static String display(String finish) {
        StringBuilder out = new StringBuilder();
        for (String word : finish.split("_")) {
            out.append(out.isEmpty() ? "" : " ").append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    public static Set<String> files() {
        Set<String> expected = new TreeSet<>();
        for (String id : ids()) {
            expected.add("assets/herbalwitch/blockstates/" + id + ".json");
            expected.add("assets/herbalwitch/models/block/" + id + ".json");
            expected.add("assets/herbalwitch/models/item/" + id + ".json");
            expected.add("data/herbalwitch/loot_tables/blocks/" + id + ".json");
            expected.add("data/herbalwitch/recipes/" + id + "_from_restyling.json");
            expected.add("data/herbalwitch/advancements/recipes/decorations/" + id + "_from_restyling.json");
            if (pieceOf(id).equals("table_lamp")) {
                String palette = id.contains("/natural/") ? "natural" : "painted";
                expected.add("assets/herbalwitch/models/block/decor/lighting/table_lamp/lit/" + palette + "/" + finishOf(id) + ".json");
            }
            if (finishOf(id).equals("dark_oak")) {
                expected.add("data/herbalwitch/recipes/" + id + ".json");
                expected.add("data/herbalwitch/advancements/recipes/decorations/" + id + ".json");
            }
        }
        expected.add("assets/herbalwitch/lang/en_us.json");
        expected.add("data/herbalwitch/tags/blocks/furniture.json");
        expected.add("data/herbalwitch/tags/items/furniture.json");
        PIECES.keySet().forEach(piece -> expected.add("data/herbalwitch/tags/items/decor/" + piece + ".json"));
        expected.add("data/minecraft/tags/blocks/mineable/axe.json");
        return expected;
    }

    /** Every decor language key and its English text. */
    public static Map<String, String> lang() {
        Map<String, String> lang = new LinkedHashMap<>();
        lang.put("itemGroup.herbalwitch.decor", "The Herbal Witch: Cottage Decor");
        for (String id : ids()) {
            lang.put("block.herbalwitch." + id.replace('/', '.'), display(finishOf(id)) + " " + NAMES.get(pieceOf(id)));
        }
        return lang;
    }
}
