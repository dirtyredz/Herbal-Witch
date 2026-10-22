package io.github.dirtyredz.herbalwitch.magic.ritual.catalog;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * The ritual circle's permanent identity and shape, plain data: block and item
 * {@code herbalwitch:magic/ritual/circle} (no block entity, no menu, no inventory), a 5 x 5 one-layer
 * ring around a hollow 3 x 3 interior. Its 16 cells are in the canonical order (by depth, then to the
 * viewer's right) that runtime, compiler, tests and previews share. The altar's origin stands at
 * {@link #ALTAR} relative to the circle's front-left origin, facing the same way, its 2 x 1 x 2
 * footprint inside the ring.
 */
public final class CircleCatalog {
    public static final String PATH = "magic/ritual/circle";
    public static final String NAME = "Ritual Circle";
    public static final String SHOWCASE = "ritual_circle";
    /** Width and depth of the square ring. */
    public static final int SIZE = 5;
    public static final MultiblockLayout LAYOUT = MultiblockLayout.of("part", ring());
    /** Where the altar's origin stands, relative to the circle's origin. */
    public static final MultiblockLayout.Cell ALTAR = new MultiblockLayout.Cell(1, 0, 2);
    /** Block light while a circle ritual runs. */
    public static final int ACTIVE_LIGHT = 5;
    /** How often (progress ticks) a running circle ritual rechecks the whole circle. */
    public static final int CHECK_INTERVAL = 20;

    private CircleCatalog() {
    }

    /** The ring's cells: the border of a {@link #SIZE} square, by depth then to the right. */
    public static List<MultiblockLayout.Cell> ring() {
        List<MultiblockLayout.Cell> cells = new ArrayList<>();
        for (int depth = 0; depth < SIZE; depth++) {
            for (int right = 0; right < SIZE; right++) {
                if (depth == 0 || depth == SIZE - 1 || right == 0 || right == SIZE - 1) {
                    cells.add(new MultiblockLayout.Cell(right, 0, depth));
                }
            }
        }
        return cells;
    }

    public static String id() {
        return HerbalWitch.MOD_ID + ":" + PATH;
    }
}
