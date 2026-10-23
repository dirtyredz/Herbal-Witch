package io.github.dirtyredz.herbalwitch.data.art;

import io.github.dirtyredz.herbalwitch.data.decor.manifest.DecorPieces;
import io.github.dirtyredz.herbalwitch.data.decor.manifest.DecorPieces.Piece;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What the Blockbench pipeline must produce, written out independently of
 * src/art/blockbench/catalog.json and the compiler: the fourteen projects before Milestone 5 (with
 * their exact 34 model files and owned textures), then one project per Milestone 5 decor piece of
 * every wave whose art is drawn, with the models its cells and states compile to, the altar (7
 * models, Milestone 6) and the six Milestone 7 pieces (50 models).
 */
public final class BlockbenchManifest {
    /** The Milestone 5 waves whose pieces have Blockbench projects. */
    public static final List<String> ART_WAVES = List.of("A", "B", "C");
    /** Lights (an off/on pair, or lit cells) and the door (mirrored cells), independently of the catalogue. */
    public static final Set<String> LIGHTS = Set.of("table_lamp", "wall_sconce", "hanging_lantern", "chandelier", "ceiling_light",
            "floor_lamp", "post_lantern", "pendant_light", "firepit");
    public static final Set<String> DOORS = Set.of("door");
    /** Project directories before Milestone 5, in catalogue order. */
    public static final List<String> LEGACY_PROJECTS = List.of("stations/kettle", "stations/infusion_pot", "stations/mortar",
            "stations/drying_rack", "stations/hearth", "stations/stove", "decor/armchair", "decor/apothecary_counter",
            "decor/wall_shelf", "decor/table_lamp", "decor/rug", "preparations/oil_lantern", "grimoire/stand",
            "grimoire/bookshelf");
    /** Milestone 6: the functional altar (its active overlay reaches the upper cells only). */
    public static final List<String> MILESTONE_6_PROJECTS = List.of("ritual/altar");
    public static final List<String> MILESTONE_6_MODELS = List.of("block/ritual/altar/active_2.json", "block/ritual/altar/active_3.json",
            "block/ritual/altar/assembly.json", "block/ritual/altar/part_0.json", "block/ritual/altar/part_1.json",
            "block/ritual/altar/part_2.json", "block/ritual/altar/part_3.json");
    /**
     * Milestone 7: the cauldron and the ward stone (a base and an active model each), the still (two
     * cells stacked, an overlay on the lower one), the bottling bench and the charm workbench (two cells
     * side by side, an overlay on each) and the ritual circle (sixteen ring cells, each with an overlay
     * and no assembly): 2 + 4 + 5 + 5 + 32 + 2 = 50 compiled models.
     */
    public static final List<String> MILESTONE_7_PROJECTS = List.of("stations/cauldron", "stations/still", "stations/bottling_bench",
            "stations/charm_workbench", "ritual/circle", "ward/ward_stone");
    public static final List<String> MILESTONE_7_MODELS = milestone7Models();
    /** Project directories under src/art/blockbench, in catalogue order. */
    public static final List<String> PROJECTS = projects();
    /** Model directories (under models/) the compiler owns outright. */
    public static final List<String> MODEL_DIRS = List.of("block/stations/kettle", "block/stations/infusion_pot",
            "block/stations/mortar", "block/stations/drying_rack", "block/stations/hearth", "block/stations/stove",
            "block/decor/templates", "block/preparations/templates", "block/grimoire/stand", "block/grimoire/bookshelf",
            "block/decor/pieces", "block/ritual/altar", "block/stations/cauldron", "block/stations/still",
            "block/stations/bottling_bench", "block/stations/charm_workbench", "block/ritual/circle", "block/ward/ward_stone", "block/animals", "block/familiars/home", "block/familiars/equipment");
    /** Textures (under textures/) the fourteen pre-M5 pieces own, painted in their projects: block/pieces/<project>/<texture>.png. */
    public static final List<String> TEXTURES = List.of(
            "block/pieces/decor/apothecary_counter/gold.png",
            "block/pieces/decor/apothecary_counter/iron.png",
            "block/pieces/decor/apothecary_counter/ivy.png",
            "block/pieces/decor/apothecary_counter/paint.png",
            "block/pieces/decor/apothecary_counter/runner_magenta.png",
            "block/pieces/decor/apothecary_counter/wood.png",
            "block/pieces/decor/apothecary_counter/wood_v.png",
            "block/pieces/decor/armchair/gold.png",
            "block/pieces/decor/armchair/ivy.png",
            "block/pieces/decor/armchair/pink.png",
            "block/pieces/decor/armchair/plum.png",
            "block/pieces/decor/armchair/runner.png",
            "block/pieces/decor/armchair/velvet.png",
            "block/pieces/decor/armchair/wood.png",
            "block/pieces/decor/armchair/wood_v.png",
            "block/pieces/decor/rug/rug.png",
            "block/pieces/decor/table_lamp/gold.png",
            "block/pieces/decor/table_lamp/ivy.png",
            "block/pieces/decor/table_lamp/lamp_shade.png",
            "block/pieces/decor/table_lamp/plum.png",
            "block/pieces/decor/table_lamp/wood.png",
            "block/pieces/decor/table_lamp/wood_v.png",
            "block/pieces/decor/wall_shelf/gold.png",
            "block/pieces/decor/wall_shelf/iron.png",
            "block/pieces/decor/wall_shelf/ivy.png",
            "block/pieces/decor/wall_shelf/pink.png",
            "block/pieces/decor/wall_shelf/plum.png",
            "block/pieces/decor/wall_shelf/wood.png",
            "block/pieces/grimoire/bookshelf/books.png",
            "block/pieces/grimoire/bookshelf/gold.png",
            "block/pieces/grimoire/bookshelf/ivy.png",
            "block/pieces/grimoire/bookshelf/pages.png",
            "block/pieces/grimoire/bookshelf/plum.png",
            "block/pieces/grimoire/bookshelf/wood.png",
            "block/pieces/grimoire/bookshelf/wood_v.png",
            "block/pieces/grimoire/stand/gold.png",
            "block/pieces/grimoire/stand/ivy.png",
            "block/pieces/grimoire/stand/pages.png",
            "block/pieces/grimoire/stand/pink.png",
            "block/pieces/grimoire/stand/plum.png",
            "block/pieces/grimoire/stand/wood.png",
            "block/pieces/grimoire/stand/wood_v.png",
            "block/pieces/preparations/oil_lantern/brass.png",
            "block/pieces/preparations/oil_lantern/ember.png",
            "block/pieces/preparations/oil_lantern/flame.png",
            "block/pieces/preparations/oil_lantern/glass.png",
            "block/pieces/preparations/oil_lantern/iron.png",
            "block/pieces/stations/drying_rack/gold.png",
            "block/pieces/stations/drying_rack/iron.png",
            "block/pieces/stations/drying_rack/post.png",
            "block/pieces/stations/drying_rack/wood.png",
            "block/pieces/stations/drying_rack/wood_z.png",
            "block/pieces/stations/hearth/coals.png",
            "block/pieces/stations/hearth/flame.png",
            "block/pieces/stations/hearth/glow.png",
            "block/pieces/stations/hearth/gold.png",
            "block/pieces/stations/hearth/iron.png",
            "block/pieces/stations/hearth/ivy.png",
            "block/pieces/stations/hearth/log.png",
            "block/pieces/stations/hearth/plaque.png",
            "block/pieces/stations/hearth/post.png",
            "block/pieces/stations/hearth/stone.png",
            "block/pieces/stations/hearth/wood.png",
            "block/pieces/stations/infusion_pot/copper.png",
            "block/pieces/stations/infusion_pot/gold.png",
            "block/pieces/stations/infusion_pot/lid.png",
            "block/pieces/stations/infusion_pot/painted.png",
            "block/pieces/stations/infusion_pot/plum.png",
            "block/pieces/stations/kettle/copper.png",
            "block/pieces/stations/kettle/gold.png",
            "block/pieces/stations/kettle/painted.png",
            "block/pieces/stations/kettle/plum.png",
            "block/pieces/stations/mortar/herbs.png",
            "block/pieces/stations/mortar/pestle.png",
            "block/pieces/stations/mortar/plinth.png",
            "block/pieces/stations/mortar/stone.png",
            "block/pieces/stations/stove/cloth.png",
            "block/pieces/stations/stove/coals.png",
            "block/pieces/stations/stove/glow.png",
            "block/pieces/stations/stove/gold.png",
            "block/pieces/stations/stove/iron.png",
            "block/pieces/stations/stove/iron_top.png",
            "block/pieces/stations/stove/post.png");

    private BlockbenchManifest() {
    }

    /** The Milestone 5 decor pieces with projects. */
    public static List<Piece> pieces() {
        return DecorPieces.ALL.stream().filter(p -> ART_WAVES.contains(p.wave())).toList();
    }

    /** A Milestone 5 piece's project directory under src/art/blockbench (and its owned texture directory under block/pieces/). */
    public static String project(Piece piece) {
        return "decor/pieces/" + piece.family() + "/" + piece.group() + "/" + piece.id();
    }

    private static List<String> projects() {
        List<String> out = new ArrayList<>(LEGACY_PROJECTS);
        pieces().forEach(p -> out.add(project(p)));
        out.addAll(MILESTONE_6_PROJECTS);
        out.addAll(MILESTONE_7_PROJECTS);
        out.addAll(io.github.dirtyredz.herbalwitch.data.animals.AnimalManifest.PROJECTS);
        out.addAll(List.of("familiars/homes/bed", "familiars/homes/nest", "familiars/homes/perch"));
        for (String gear : io.github.dirtyredz.herbalwitch.data.familiars.FamiliarManifest.EQUIPMENT.stream().sorted().toList()) out.add("familiars/equipment/" + gear);
        return List.copyOf(out);
    }

    /** The Milestone 7 compiled models, written out per piece (the circle's sixteen cells in a loop), sorted. */
    private static List<String> milestone7Models() {
        List<String> out = new ArrayList<>(List.of(
                "block/stations/cauldron/base.json", "block/stations/cauldron/active.json",
                "block/stations/still/part_0.json", "block/stations/still/part_1.json", "block/stations/still/active_0.json",
                "block/stations/still/assembly.json",
                "block/stations/bottling_bench/part_0.json", "block/stations/bottling_bench/part_1.json",
                "block/stations/bottling_bench/active_0.json", "block/stations/bottling_bench/active_1.json",
                "block/stations/bottling_bench/assembly.json",
                "block/stations/charm_workbench/part_0.json", "block/stations/charm_workbench/part_1.json",
                "block/stations/charm_workbench/active_0.json", "block/stations/charm_workbench/active_1.json",
                "block/stations/charm_workbench/assembly.json",
                "block/ward/ward_stone/base.json", "block/ward/ward_stone/active.json"));
        for (int i = 0; i < 16; i++) {
            out.add("block/ritual/circle/part_" + i + ".json");
            out.add("block/ritual/circle/active_" + i + ".json");
        }
        out.sort(null);
        return List.copyOf(out);
    }

    /** The models a Milestone 5 piece compiles to, relative to models/. */
    public static List<String> models(Piece piece) {
        String dir = "block/decor/pieces/" + piece.family() + "/" + piece.group() + "/" + piece.id() + "/";
        List<String> out = new ArrayList<>();
        if (piece.cells() == 1) {
            if (LIGHTS.contains(piece.id())) {
                out.add(dir + "off.json");
                out.add(dir + "on.json");
            } else {
                out.add(dir + "base.json");
            }
            return out;
        }
        for (int i = 0; i < piece.cells(); i++) {
            out.add(dir + "part_" + i + ".json");
            if (LIGHTS.contains(piece.id())) {
                out.add(dir + "lit_" + i + ".json");
            }
            if (DOORS.contains(piece.id())) {
                out.add(dir + "mirrored_" + i + ".json");
            }
        }
        out.add(dir + "assembly.json");
        return out;
    }

    /** Every compiled model file, relative to models/, sorted. */
    public static List<String> models() {
        List<String> out = new ArrayList<>();
        pieces().forEach(p -> out.addAll(models(p)));
        for (String vessel : List.of("kettle", "infusion_pot", "mortar")) {
            out.add("block/stations/" + vessel + "/base.json");
        }
        Map<String, Object[]> multiblocks = Map.of(
                "drying_rack", new Object[] {4, "bundles", 4},
                "hearth", new Object[] {4, "fire", 2},
                "stove", new Object[] {2, "fire", 2});
        multiblocks.forEach((station, spec) -> {
            for (int i = 0; i < (int) spec[0]; i++) {
                out.add("block/stations/" + station + "/part_" + i + ".json");
            }
            for (int i = 0; i < (int) spec[2]; i++) {
                out.add("block/stations/" + station + "/" + spec[1] + "_" + i + ".json");
            }
            out.add("block/stations/" + station + "/assembly.json");
        });
        for (String template : List.of("armchair", "apothecary_counter", "wall_shelf", "table_lamp_off", "table_lamp_on", "rug")) {
            out.add("block/decor/templates/" + template + ".json");
        }
        out.add("block/preparations/templates/oil_lantern_off.json");
        out.add("block/preparations/templates/oil_lantern_on.json");
        out.add("block/grimoire/stand/base.json");
        out.add("block/grimoire/bookshelf/base.json");
        out.addAll(MILESTONE_6_MODELS);
        out.addAll(MILESTONE_7_MODELS);
        io.github.dirtyredz.herbalwitch.data.animals.AnimalManifest.PROJECTS.forEach(p -> out.add("block/" + p + "/base.json"));
        for (String gear : io.github.dirtyredz.herbalwitch.data.familiars.FamiliarManifest.EQUIPMENT) out.add("block/familiars/equipment/" + gear + "/base.json");
        for (String key : List.of("bed", "nest", "perch")) out.add("block/familiars/home/" + key + "/base.json");
        out.sort(null);
        return out;
    }
}
