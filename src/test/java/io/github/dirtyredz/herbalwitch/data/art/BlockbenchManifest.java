package io.github.dirtyredz.herbalwitch.data.art;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * What the Blockbench pipeline must produce, written out independently of
 * src/art/blockbench/catalog.json and the compiler: twelve projects, the 32 model files they compile
 * to, and the two textures they own.
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
    /** Textures (under textures/) painted in a project rather than generated. */
    public static final List<String> TEXTURES = List.of("block/material/rug.png", "block/material/lamp_shade.png");

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
