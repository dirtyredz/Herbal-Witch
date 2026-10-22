package io.github.dirtyredz.herbalwitch.decor.structure.shape.families;

import java.util.Map;

/**
 * Rug, curtain and screen collision boxes. Rugs stay carpet-thin; the small rug uses vanilla carpet's own shape.
 * Boxes are {@code minX, minY, minZ, maxX, maxY, maxZ} in the structure's north-facing pixels (see
 * FurnitureShapes). The Milestone 5 pieces' boxes were derived once from their first-draft Blockbench
 * projects (solid parts merged at a one-pixel grid, ivy and trims left out) and are now edited by hand
 * when a piece's silhouette changes.
 */
public final class TextilesShapes {
    public static final Map<String, double[][]> BOXES = Map.ofEntries();

    private TextilesShapes() {
    }
}
