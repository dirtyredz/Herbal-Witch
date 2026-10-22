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
    public static final Map<String, double[][]> BOXES = Map.ofEntries(
            Map.entry("medium_rug", new double[][] {
                    {-16, 0, 1, 16, 1, 31}}),
            Map.entry("large_rug", new double[][] {
                    {-32, 0, 1, 16, 1, 47}}),
            Map.entry("curtain", new double[][] {
                    {-15, 1, 12, -8, 14, 15}, {8, 1, 12, 15, 14, 15}, {-16, 13, 11, -10, 16, 12}, {10, 13, 11, 16, 16, 12},
                    {-16, 13, 12, -15, 16, 16}, {15, 13, 12, 16, 16, 16}, {-15, 13, 15, -10, 16, 16}, {10, 13, 15, 15, 16, 16},
                    {-15, 14, 12, -10, 16, 15}, {10, 14, 12, 15, 16, 15}, {-15, 16, 12, -11, 32, 15}, {11, 16, 12, 15, 32, 15},
                    {-11, 17, 12, -8, 32, 15}, {8, 17, 12, 11, 32, 15}, {-8, 19, 11, -5, 21, 15}, {5, 19, 11, 8, 21, 15},
                    {-8, 21, 12, -5, 32, 15}, {5, 21, 12, 8, 32, 15}, {-5, 22, 12, -2, 32, 15}, {2, 22, 12, 5, 32, 15},
                    {-16, 29, 12, -15, 32, 15}, {15, 29, 12, 16, 32, 15}, {-2, 30, 12, 2, 32, 15}}),
            Map.entry("room_divider", new double[][] {
                    {-31, 0, 6, 15, 2, 10}, {-30, 2, 7, -16, 4, 9}, {-15, 2, 7, -1, 4, 9}, {0, 2, 7, 14, 4, 9},
                    {-16, 5, 4, -14, 9, 7}, {-2, 5, 9, 0, 9, 12}, {-16, 14, 4, -14, 18, 7}, {-2, 14, 9, 0, 18, 12},
                    {-16, 23, 4, -14, 27, 7}, {-2, 23, 9, 0, 27, 12}, {-30, 26, 7, -16, 30, 9}, {-15, 26, 7, -1, 30, 9},
                    {0, 26, 7, 14, 30, 9}, {-31, 30, 6, -29, 32, 10}, {-17, 30, 6, -14, 32, 10}, {-2, 30, 6, 1, 32, 10},
                    {13, 30, 6, 15, 32, 10}}),
            Map.entry("hanging_divider", new double[][] {
                    {-28, 1, 7, -26, 3, 9}, {-24, 1, 7, -22, 3, 9}, {-20, 1, 7, -18, 3, 9}, {-16, 1, 7, -14, 3, 9},
                    {-12, 1, 7, -10, 3, 9}, {-8, 1, 7, -6, 3, 9}, {-4, 1, 7, -2, 3, 9}, {0, 1, 7, 2, 3, 9},
                    {4, 1, 7, 6, 3, 9}, {8, 1, 7, 10, 3, 9}, {12, 1, 7, 14, 3, 9}, {-29, 4, 7, 13, 32, 9},
                    {-32, 29, 6, -30, 32, 10}, {14, 29, 6, 16, 32, 10}, {-30, 30, 7, -29, 32, 9}, {13, 30, 7, 14, 32, 9}}));

    private TextilesShapes() {
    }
}
