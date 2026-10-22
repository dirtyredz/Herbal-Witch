package io.github.dirtyredz.herbalwitch.decor.structure.shape.families;

import java.util.Map;

/**
 * Window and archway collision boxes. The door uses vanilla's door shapes; the archway's passage is open.
 * Boxes are {@code minX, minY, minZ, maxX, maxY, maxZ} in the structure's north-facing pixels (see
 * FurnitureShapes). The Milestone 5 pieces' boxes were derived once from their first-draft Blockbench
 * projects (solid parts merged at a one-pixel grid, ivy and trims left out) and are now edited by hand
 * when a piece's silhouette changes.
 */
public final class OpeningsShapes {
    public static final Map<String, double[][]> BOXES = Map.ofEntries();

    private OpeningsShapes() {
    }
}
