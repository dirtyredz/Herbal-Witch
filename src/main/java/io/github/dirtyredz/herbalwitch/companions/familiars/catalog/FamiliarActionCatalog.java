package io.github.dirtyredz.herbalwitch.companions.familiars.catalog;

import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import java.util.List;

public final class FamiliarActionCatalog {
    public static final AltarActionSpec BINDING = AltarActionSpec.register("ritual/familiar/binding", List.of(herb("temperate/sage"), item("minecraft:amethyst_shard")), 200, 25);
    public static final AltarActionSpec REVIVAL = AltarActionSpec.register("ritual/familiar/revival", List.of(item("herbalwitch:preparations/advanced/essences/moon"), herb("temperate/yarrow"), item("minecraft:amethyst_shard")), 400, 50);
    public static final AltarActionSpec HEARTWOOD = AltarActionSpec.register("ritual/familiar/heartwood", List.of(herb("temperate/yarrow"), herb("temperate/sage"), item("minecraft:gold_ingot")), 200, 25);
    public static final AltarActionSpec SPIRIT_WELL = AltarActionSpec.register("ritual/familiar/spirit_well", List.of(item("minecraft:amethyst_shard"), herb("shade/mugwort"), item("herbalwitch:preparations/advanced/essences/moon")), 200, 25);
    public static final AltarActionSpec HARMONY = AltarActionSpec.register("ritual/familiar/bond_harmony", List.of(herb("temperate/lavender")), 200, 10);
    public static final List<AltarActionSpec> ALL = List.of(BINDING, REVIVAL, HEARTWOOD, SPIRIT_WELL, HARMONY);
    private FamiliarActionCatalog() {}
    private static AltarActionSpec.Offering item(String id) { return new AltarActionSpec.Offering(id, 1); }
    private static AltarActionSpec.Offering herb(String path) { return item("herbalwitch:botany/" + path + "/fresh"); }
}
