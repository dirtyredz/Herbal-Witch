package io.github.dirtyredz.herbalwitch.decor.structure.shape.families;

import java.util.Map;

/**
 * Lamp collision boxes. Hanging lamps keep their chains thin.
 * Boxes are {@code minX, minY, minZ, maxX, maxY, maxZ} in the structure's north-facing pixels (see
 * FurnitureShapes). The Milestone 5 pieces' boxes were derived once from their first-draft Blockbench
 * projects (solid parts merged at a one-pixel grid, ivy and trims left out) and are now edited by hand
 * when a piece's silhouette changes.
 */
public final class LightingShapes {
    public static final Map<String, double[][]> BOXES = Map.ofEntries(
            // base, stem and shade; symmetric, so every facing agrees
            Map.entry("table_lamp", new double[][] {{5, 0, 5, 11, 2, 11}, {7, 2, 7, 9, 8, 9}, {4, 8, 4, 12, 15, 12}}));

    private LightingShapes() {
    }
}
