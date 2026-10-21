package io.github.dirtyredz.herbalwitch.data.art;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * What the Blockbench pipeline must produce, written out independently of
 * src/art/blockbench/catalog.json and the compiler: twelve projects, the 32 model files they compile
 * to, and the textures they own.
 */
public final class BlockbenchManifest {
    /** Project directories under src/art/blockbench, in catalogue order. */
    public static final List<String> PROJECTS = List.of("stations/kettle", "stations/infusion_pot", "stations/mortar",
            "stations/drying_rack", "stations/hearth", "stations/stove", "decor/armchair", "decor/apothecary_counter",
            "decor/wall_shelf", "decor/table_lamp", "decor/rug", "preparations/oil_lantern");
    /** Model directories (under models/) the compiler owns outright. */
    public static final List<String> MODEL_DIRS = List.of("block/stations/kettle", "block/stations/infusion_pot",
            "block/stations/mortar", "block/stations/drying_rack", "block/stations/hearth", "block/stations/stove",
            "block/decor/templates", "block/preparations/templates");
    /** Textures (under textures/) every piece owns, painted in its project: block/pieces/<project>/<texture>.png. */
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
            "block/pieces/decor/wall_shelf/glass.png",
            "block/pieces/decor/wall_shelf/gold.png",
            "block/pieces/decor/wall_shelf/iron.png",
            "block/pieces/decor/wall_shelf/ivy.png",
            "block/pieces/decor/wall_shelf/pink.png",
            "block/pieces/decor/wall_shelf/plum.png",
            "block/pieces/decor/wall_shelf/tincture.png",
            "block/pieces/decor/wall_shelf/wood.png",
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

    /** Every compiled model file, relative to models/, sorted. */
    public static List<String> models() {
        List<String> out = new ArrayList<>();
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
        out.sort(null);
        return out;
    }
}
