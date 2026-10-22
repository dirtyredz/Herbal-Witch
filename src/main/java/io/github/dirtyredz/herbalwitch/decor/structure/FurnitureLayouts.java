package io.github.dirtyredz.herbalwitch.decor.structure;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.MountSpec.Support;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout.Cell;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The cell layout of each multiblock piece, built once from its catalogue cells (the same cells and
 * order the Blockbench compiler cuts its model into), and the cell a placement anchors on the aimed
 * position: the origin, or for a hanging piece the top cell above it, so a pendant hangs from the
 * ceiling the player aimed at.
 */
public final class FurnitureLayouts {
    /** The cell-index property name; beds use {@link #BED_PROPERTY} because vanilla's bed already has {@code part}. */
    public static final String PROPERTY = "part";
    public static final String BED_PROPERTY = "cell";
    private static final Map<String, MultiblockLayout> LAYOUTS = new ConcurrentHashMap<>();

    private FurnitureLayouts() {
    }

    /** The layout of a multiblock piece, or null for a one-cell piece. */
    public static MultiblockLayout of(FurnitureSpec spec) {
        if (!spec.multiblock()) {
            return null;
        }
        String property = spec.behavior() == FurnitureBehavior.BED ? BED_PROPERTY : PROPERTY;
        return LAYOUTS.computeIfAbsent(spec.path(), k -> MultiblockLayout.of(property, spec.mount().cells()));
    }

    /** The cell placed at the aimed position: the origin, or the top cell above it for ceiling pieces. */
    public static int anchorCell(FurnitureSpec spec) {
        MultiblockLayout layout = of(spec);
        if (layout == null || spec.support() != Support.CEILING) {
            return 0;
        }
        int top = 0;
        while (layout.indexOf(new Cell(0, layout.cell(top).up() + 1, 0)) >= 0) {
            top = layout.indexOf(new Cell(0, layout.cell(top).up() + 1, 0));
        }
        return top;
    }
}
