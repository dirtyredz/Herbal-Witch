package io.github.dirtyredz.herbalwitch.brewing.product.catalog;

/**
 * One finished everyday product: its permanent id, English name and what kind of item it is.
 * Plain data; registration chooses the item class from {@link Kind}.
 *
 * @param category    id segment: drinks, salves, oils, garden or ingredients
 * @param id          stable product segment
 * @param displayName English name
 * @param kind        how the item is used
 * @param description one-line English tooltip
 */
public record ProductSpec(String category, String id, String displayName, Kind kind, String description) {
    public enum Kind {
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
