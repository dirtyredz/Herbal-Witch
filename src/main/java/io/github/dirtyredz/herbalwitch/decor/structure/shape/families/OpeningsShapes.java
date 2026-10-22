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
    public static final Map<String, double[][]> BOXES = Map.ofEntries(
            Map.entry("window", new double[][] {
                    {3, 1, 12, 13, 3, 16}, {4, 3, 12, 12, 5, 16}, {4, 5, 12, 6, 9, 16}, {9, 5, 12, 11, 9, 16},
                    {6, 5, 13, 9, 10, 16}, {11, 5, 13, 12, 32, 16}, {3, 7, 12, 4, 9, 16}, {6, 7, 12, 9, 9, 13},
                    {11, 7, 12, 13, 9, 13}, {12, 7, 13, 13, 9, 16}, {4, 9, 13, 6, 32, 16}, {9, 9, 13, 11, 10, 16},
                    {0, 9, 14, 4, 29, 16}, {12, 9, 14, 16, 29, 16}, {10, 10, 13, 11, 32, 16}, {6, 16, 14, 10, 18, 15},
                    {6, 22, 14, 10, 24, 15}, {6, 28, 13, 10, 32, 16}, {3, 30, 12, 13, 32, 13}, {3, 30, 13, 4, 32, 16},
                    {12, 30, 13, 13, 32, 16}}),
            Map.entry("archway", new double[][] {
                    {-30, 0, 2, -16, 3, 14}, {0, 0, 2, 14, 3, 14}, {-29, 3, 3, -16, 48, 13}, {0, 3, 3, 13, 48, 13},
                    {-16, 32, 3, -14, 48, 13}, {-2, 32, 3, 0, 48, 13}, {-14, 35, 3, -12, 48, 13}, {-4, 35, 3, -2, 48, 13},
                    {-12, 37, 3, -10, 48, 13}, {-6, 37, 3, -4, 48, 13}, {-10, 40, 3, -9, 48, 13}, {-7, 40, 3, -6, 48, 13},
                    {-9, 42, 3, -7, 48, 13}, {-31, 44, 2, 15, 48, 3}, {-31, 44, 3, -29, 48, 14}, {13, 44, 3, 15, 48, 14},
                    {-29, 44, 13, 13, 48, 14}}));

    private OpeningsShapes() {
    }
}
