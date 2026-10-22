package io.github.dirtyredz.herbalwitch.magic.charm.catalog;

import java.util.List;

/**
 * The protective charms of Milestone 7 (docs/catalogue/products.md); capture charms belong to
 * Milestone 8. Ids {@code magic/charms/<id>}, permanent once a world uses them.
 */
public final class CharmCatalog {
    public static final CharmSpec PROTECTIVE_SACHET = new CharmSpec("protective_sachet", "Protective Sachet", "", 15, 2.0F,
            "Mob blows land 15% softer (at most 1 heart)");
    public static final CharmSpec UNDEAD_AMULET = new CharmSpec("undead_amulet", "Undead Amulet", "undead", 30, 3.0F,
            "Undead blows land 30% softer (at most 1.5 hearts)");
    public static final CharmSpec ARTHROPOD_AMULET = new CharmSpec("arthropod_amulet", "Arthropod Amulet", "arthropods", 30, 3.0F,
            "Spider and pest bites land 30% softer (at most 1.5 hearts)");

    public static final List<CharmSpec> ALL = List.of(PROTECTIVE_SACHET, UNDEAD_AMULET, ARTHROPOD_AMULET);

    private CharmCatalog() {
    }
}
