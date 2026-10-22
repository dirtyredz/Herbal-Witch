package io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog;

/**
 * One finished everyday product: its permanent id, English name and what kind of item it is.
 * Plain data; registration chooses the item class from {@link Kind}.
 *
 * @param category    id segment(s): drinks, salves, oils, garden or ingredients; Milestone 7 adds
 *                    {@code advanced/<group>}, {@code familiar/<group>} and {@code mixtures/<group>}
 * @param id          stable product segment
 * @param displayName English name
 * @param kind        how the item is used
 * @param description one-line English tooltip
 */
public record ProductSpec(String category, String id, String displayName, Kind kind, String description) {
    public enum Kind {
        /** Thrown in a glass vial; bursts where it lands (Milestone 7). */
        VIAL,
        /** A dose of dust thrown by hand (Milestone 7). */
        THROWN_DUST,
        /** Four doses straight from the cauldron, bottled at the bottling bench (Milestone 7). */
        MIXTURE,
        /** For a bonded familiar; its use arrives with familiar bonding (Milestone 9). */
        FAMILIAR,
        /** Drunk like a potion; the bottle comes back. */
        DRINK,
        /** Applied to yourself; the bowl comes back. */
        SALVE,
        /** Applied to yourself (fishing blend, pest repellent) or poured into an oil lantern. */
        OIL,
        /** Used on a block: fertilizer and growth dusts. */
        GARDEN_DUST,
        /** Used on a herb or herb bed; the bottle comes back. */
        GARDEN_TONIC,
        /** A plain ingredient. */
        INGREDIENT
    }

    /** {@code preparations/<category>/<id>}. */
    public String path() {
        return "preparations/" + category + "/" + id;
    }
}
