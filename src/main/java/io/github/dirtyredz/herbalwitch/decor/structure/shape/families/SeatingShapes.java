package io.github.dirtyredz.herbalwitch.decor.structure.shape.families;

import java.util.Map;

/**
 * Seating collision boxes. Seats stay low enough to sit on, backs and arms solid.
 * Boxes are {@code minX, minY, minZ, maxX, maxY, maxZ} in the structure's north-facing pixels (see
 * FurnitureShapes). The Milestone 5 pieces' boxes were derived once from their first-draft Blockbench
 * projects (solid parts merged at a one-pixel grid, ivy and trims left out) and are now edited by hand
 * when a piece's silhouette changes.
 */
public final class SeatingShapes {
    public static final Map<String, double[][]> BOXES = Map.ofEntries(
            // legs and seat block, raised back, two arms
            Map.entry("armchair", new double[][] {{1, 0, 1, 15, 8, 15}, {1, 8, 12, 15, 16, 15}, {1, 8, 1, 3, 12, 12}, {13, 8, 1, 15, 12, 12}}),
            Map.entry("dining_chair", new double[][] {
                    {2, 0, 2, 5, 7, 5}, {11, 0, 2, 14, 7, 5}, {2, 0, 11, 5, 7, 14}, {11, 0, 11, 14, 7, 14},
                    {5, 6, 2, 11, 7, 14}, {2, 6, 5, 5, 7, 11}, {11, 6, 5, 14, 7, 11}, {3, 7, 2, 13, 9, 13},
                    {2, 7, 12, 3, 16, 15}, {13, 7, 12, 14, 16, 15}, {3, 7, 13, 5, 16, 15}, {11, 7, 13, 13, 16, 15},
                    {5, 8, 13, 11, 10, 14}, {3, 9, 12, 5, 16, 13}, {11, 9, 12, 13, 16, 13}, {5, 13, 12, 11, 15, 15}}),
            Map.entry("bench", new double[][] {
                    {-14, 0, 2, -11, 1, 6}, {-2, 0, 2, 2, 1, 6}, {11, 0, 2, 14, 1, 6}, {-14, 0, 10, -11, 1, 14},
                    {-2, 0, 10, 2, 1, 14}, {11, 0, 10, 14, 1, 14}, {-14, 1, 3, -11, 7, 5}, {-1, 1, 3, 1, 9, 5},
                    {11, 1, 3, 14, 7, 5}, {-14, 1, 11, -11, 7, 13}, {-1, 1, 11, 1, 9, 13}, {11, 1, 11, 14, 7, 13},
                    {-14, 6, 2, 14, 7, 3}, {-11, 6, 3, -1, 7, 14}, {1, 6, 3, 11, 7, 14}, {-14, 6, 5, -11, 7, 11},
                    {-1, 6, 5, 1, 9, 11}, {11, 6, 5, 14, 7, 11}, {-14, 6, 13, -11, 7, 14}, {-1, 6, 13, 1, 7, 14},
                    {11, 6, 13, 14, 7, 14}, {-13, 7, 3, -1, 9, 13}, {1, 7, 3, 13, 9, 13}, {-14, 7, 12, -13, 15, 15},
                    {13, 7, 12, 14, 15, 15}, {-13, 7, 13, -12, 15, 15}, {12, 7, 13, 13, 15, 15}, {-13, 9, 12, -12, 15, 13},
                    {12, 9, 12, 13, 15, 13}, {-12, 12, 12, 12, 14, 15}}));

    private SeatingShapes() {
    }
}
