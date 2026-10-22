package io.github.dirtyredz.herbalwitch.decor.structure.shape.families;

import java.util.Map;

/**
 * Fixture collision boxes. Frames (the canopy bed, the well, the swing) leave their insides open.
 * Boxes are {@code minX, minY, minZ, maxX, maxY, maxZ} in the structure's north-facing pixels (see
 * FurnitureShapes). The Milestone 5 pieces' boxes were derived once from their first-draft Blockbench
 * projects (solid parts merged at a one-pixel grid, ivy and trims left out) and are now edited by hand
 * when a piece's silhouette changes.
 */
public final class FixturesShapes {
    public static final Map<String, double[][]> BOXES = Map.ofEntries(
            Map.entry("kitchen_sink", new double[][] {
                    {0, 0, 2, 16, 13, 16}, {0, 2, 1, 16, 14, 2}, {0, 12, 0, 16, 14, 1}, {0, 13, 2, 16, 14, 3},
                    {0, 13, 3, 3, 14, 16}, {13, 13, 3, 16, 14, 16}, {3, 13, 12, 13, 14, 16}, {7, 14, 13, 9, 16, 16},
                    {0, 14, 14, 7, 16, 16}, {9, 14, 14, 16, 16, 16}}),
            Map.entry("stove_oven", new double[][] {
                    {0, 0, 1, 3, 14, 4}, {13, 0, 1, 16, 14, 4}, {0, 0, 11, 3, 14, 13}, {13, 0, 11, 16, 14, 13},
                    {3, 1, 1, 13, 14, 16}, {0, 1, 4, 3, 14, 11}, {13, 1, 4, 16, 14, 11}, {0, 1, 13, 3, 14, 16},
                    {13, 1, 13, 16, 14, 16}, {10, 14, 11, 14, 16, 14}}),
            Map.entry("fridge", new double[][] {
                    {0, 0, 0, 16, 2, 16}, {0, 2, 1, 16, 32, 3}, {1, 2, 3, 15, 32, 15}, {0, 2, 14, 1, 32, 16},
                    {15, 2, 14, 16, 32, 16}, {1, 2, 15, 3, 32, 16}, {13, 2, 15, 15, 32, 16}, {0, 29, 0, 16, 32, 1},
                    {0, 29, 3, 1, 32, 14}, {15, 29, 3, 16, 32, 14}, {3, 29, 15, 13, 32, 16}}),
            Map.entry("drying_rack_decor", new double[][] {
                    {-15, 0, 1, -11, 2, 15}, {11, 0, 1, 15, 2, 15}, {-14, 2, 4, -12, 5, 12}, {12, 2, 4, 14, 5, 12},
                    {-12, 3, 7, 12, 4, 9}, {-14, 5, 7, -12, 32, 9}, {12, 5, 7, 14, 32, 9}, {-15, 15, 7, -14, 16, 9},
                    {-12, 15, 7, 12, 16, 9}, {14, 15, 7, 15, 16, 9}, {-15, 26, 7, -14, 27, 9}, {-12, 26, 7, 12, 27, 9},
                    {14, 26, 7, 15, 27, 9}, {-15, 30, 6, 15, 32, 7}, {-15, 30, 7, -14, 32, 10}, {-12, 30, 7, 12, 32, 10},
                    {14, 30, 7, 15, 32, 10}, {-14, 30, 9, -12, 32, 10}, {12, 30, 9, 14, 32, 10}}),
            Map.entry("altar_table", new double[][] {
                    {-15, 0, 2, -12, 13, 5}, {12, 0, 2, 15, 13, 5}, {-15, 0, 11, -12, 13, 14}, {12, 0, 11, 15, 13, 14},
                    {-12, 8, 3, 12, 13, 4}, {-12, 8, 12, 12, 13, 13}, {-16, 11, 1, 16, 13, 2}, {-16, 11, 2, -15, 13, 15},
                    {-12, 11, 2, 12, 13, 3}, {15, 11, 2, 16, 13, 15}, {-12, 11, 4, 12, 13, 12}, {-15, 11, 5, -12, 13, 11},
                    {12, 11, 5, 15, 13, 11}, {-12, 11, 13, 12, 13, 15}, {-15, 11, 14, -12, 13, 15}, {12, 11, 14, 15, 13, 15},
                    {-10, 13, 5, -8, 16, 7}, {8, 13, 6, 9, 16, 7}, {4, 13, 7, 6, 16, 9}, {5, 13, 9, 7, 15, 10},
                    {-7, 13, 10, -6, 15, 11}}));

    private FixturesShapes() {
    }
}
