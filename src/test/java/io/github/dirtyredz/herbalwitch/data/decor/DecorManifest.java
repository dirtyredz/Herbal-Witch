package io.github.dirtyredz.herbalwitch.data.decor;

import io.github.dirtyredz.herbalwitch.data.decor.manifest.DecorPieces;
import io.github.dirtyredz.herbalwitch.data.decor.manifest.DecorPieces.Piece;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The decor feature's expected generated files and English, written out independently of the mod's
 * catalogue: the pieces of every delivered wave ({@link DecorPieces}), one design, ids
 * {@code decor/<family>/<piece>}.
 */
public final class DecorManifest {
    /** Delivery order of the waves. */
    public static final List<String> WAVES = List.of("M1", "A", "B", "C");
    /** The last delivered wave (FurnitureCatalog.DELIVERED). */
    public static final String DELIVERED = "C";
    /** The five 0.1.0 pieces' authored templates (lamps add _off / _on). */
    public static final Map<String, String> TEMPLATES = Map.of("armchair", "armchair", "apothecary_counter", "apothecary_counter",
            "small_wall_shelf", "wall_shelf", "table_lamp", "table_lamp", "small_rug", "rug");
    /** Placement refusals (platform/placement/PlacementChecks). */
    public static final List<String> REFUSALS = List.of("unloaded", "outside_world", "occupied", "obstructed", "unsupported", "denied");

    /**
     * The only generated directories allowed twelve entries or more (docs/DECISIONS.md, 0.4.0): the
     * blockstates, item models and block loot of the four furniture families with more than eleven
     * pieces, whose file names are permanent block ids. Recipes and advancements are grouped instead.
     */
    public static final Set<String> REGISTRY_DIRECTORIES = registryDirectories();

    private DecorManifest() {
    }

    private static Set<String> registryDirectories() {
        Set<String> dirs = new TreeSet<>();
        for (String family : List.of("seating", "storage", "surfaces", "fixtures")) {
            dirs.add("assets/herbalwitch/blockstates/decor/" + family);
            dirs.add("assets/herbalwitch/models/item/decor/" + family);
            dirs.add("data/herbalwitch/loot_tables/blocks/decor/" + family);
        }
        return dirs;
    }

    /** The file names a registry directory must hold: one per registered piece of its family. */
    public static Set<String> entries(String directory) {
        String family = directory.substring(directory.lastIndexOf('/') + 1);
        Set<String> names = new TreeSet<>();
        pieces().stream().filter(p -> p.family().equals(family)).forEach(p -> names.add(p.id() + ".json"));
        return names;
    }

    /** The registered pieces, in tab order. */
    public static List<Piece> pieces() {
        int delivered = WAVES.indexOf(DELIVERED);
        return DecorPieces.ALL.stream().filter(p -> WAVES.indexOf(p.wave()) <= delivered).toList();
    }

    /** {@code decor/<family>/<piece>} for the registered pieces. */
    public static List<String> ids() {
        return pieces().stream().map(Piece::path).toList();
    }

    public static String pieceOf(String id) {
        return id.substring(id.lastIndexOf('/') + 1);
    }

    public static Set<String> files() {
        Set<String> expected = new TreeSet<>();
        for (Piece piece : pieces()) {
            String id = piece.path();
            expected.add("assets/herbalwitch/blockstates/" + id + ".json");
            expected.add("assets/herbalwitch/models/item/" + id + ".json");
            expected.add("data/herbalwitch/loot_tables/blocks/" + id + ".json");
            expected.add("data/herbalwitch/recipes/" + piece.recipePath() + ".json");
            expected.add("data/herbalwitch/advancements/recipes/decorations/" + piece.recipePath() + ".json");
        }
        expected.add("assets/herbalwitch/lang/en_us.json");
        expected.add("data/herbalwitch/tags/blocks/furniture.json");
        expected.add("data/herbalwitch/tags/items/furniture.json");
        expected.add("data/minecraft/tags/blocks/mineable/axe.json");
        return expected;
    }

    /** Every decor language key and its English text (null: any non-empty text). */
    public static Map<String, String> lang() {
        Map<String, String> lang = new LinkedHashMap<>();
        lang.put("itemGroup.herbalwitch.decor", "The Herbal Witch: Cottage Decor");
        for (Piece piece : pieces()) {
            lang.put("block.herbalwitch." + piece.path().replace('/', '.'), piece.name());
        }
        REFUSALS.forEach(r -> lang.put("message.herbalwitch.placement." + r, null));
        lang.put("message.herbalwitch.seat.occupied", null);
        lang.put("message.herbalwitch.seat.no_room", null);
        for (String door : List.of("hinge_right", "open_left", "open_right")) {
            lang.put("showcase.herbalwitch.label.decor." + door, null);
        }
        // the furniture reference pages: one line per piece and the shared facts
        for (Piece piece : pieces()) {
            lang.put("grimoire.herbalwitch.decor." + piece.family() + "." + piece.id(), null);
        }
        for (String fact : List.of("size", "crafted", "lookalike")) {
            lang.put("grimoire.herbalwitch.decor." + fact, null);
        }
        for (String mount : List.of("free", "floor", "wall", "ceiling", "corner", "carpet")) {
            lang.put("grimoire.herbalwitch.decor.mount." + mount, null);
        }
        for (String use : List.of("static", "seating", "storage", "display", "light", "bed", "door", "rug")) {
            lang.put("grimoire.herbalwitch.decor.use." + use, null);
        }
        return lang;
    }
}
