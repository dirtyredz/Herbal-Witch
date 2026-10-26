package io.github.dirtyredz.herbalwitch.grimoire.guide.lessons;

/**
 * Names of the block properties the illustrated lessons set, as plain strings so scenes, structures and guide
 * markup need no gameplay import. {@code ScenePropertiesTest} ties each name to the real block's property, and
 * the scenes fail loudly on a name a block does not have.
 */
public final class SceneProperties {
    /** Crop growth stage ({@code HerbCropBlock.AGE}). */
    public static final String AGE = "age";
    /** Drying rack occupied ({@code DryingRackBlock.ACTIVE}). */
    public static final String ACTIVE = "active";
    /** Stove or hearth burning ({@code HearthBlock.LIT}, shared by the stove). */
    public static final String LIT = "lit";
    /** Multiblock cell index ({@code MultiblockLayout.part()}). */
    public static final String PART = "part";
    /** Horizontal facing of every station ({@code StationBlock.FACING}). */
    public static final String FACING = "facing";

    private SceneProperties() {
    }
}
