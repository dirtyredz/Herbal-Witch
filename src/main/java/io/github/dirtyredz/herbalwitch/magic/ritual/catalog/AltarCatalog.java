package io.github.dirtyredz.herbalwitch.magic.ritual.catalog;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;

/**
 * The functional altar's permanent identity and footprint, plain data: block, item, block entity
 * and menu {@code herbalwitch:magic/ritual/altar}; two cells wide, one deep and two high, cells in
 * the canonical order (by height, then depth, then to the viewer's right) the Blockbench compiler
 * cuts the model into. Distinct from the decorative {@code decor/fixtures/altar_table}.
 */
public final class AltarCatalog {
    public static final String PATH = "magic/ritual/altar";
    public static final String NAME = "Witch's Altar";
    /** 2 wide x 1 deep x 2 high; part 0 is the origin (the block entity, the loot, the session). */
    public static final MultiblockLayout LAYOUT = MultiblockLayout.box(2, 1, 2);
    /** Block light while a ritual runs. */
    public static final int ACTIVE_LIGHT = 7;
    /** How close (blocks, to the occupied footprint) a participant must stay. */
    public static final double ACCESS_DISTANCE = 4.0;
    /** The showcase section id. */
    public static final String SHOWCASE = "altar";

    private AltarCatalog() {
    }

    public static String id() {
        return HerbalWitch.MOD_ID + ":" + PATH;
    }
}
