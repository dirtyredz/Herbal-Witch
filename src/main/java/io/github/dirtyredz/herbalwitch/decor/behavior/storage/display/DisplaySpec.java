package io.github.dirtyredz.herbalwitch.decor.behavior.storage.display;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Where each display slot's item stands, authored against the Blockbench project: one anchor per
 * slot, the centre of the item's base on its shelf board, in the structure's north-facing pixels
 * (cell 0 at x 0..16, a step to the viewer's right at x - 16, up at y + 16). Slots fill shelf by
 * shelf from the bottom, left to right as the viewer sees them (from x 16 toward lower x). When a
 * project's boards move, these move with them. Plain data.
 */
public final class DisplaySpec {
    private static final double[] THREE = {12, 8, 4};
    private static final Map<String, List<double[]>> ANCHORS = Map.ofEntries(
            Map.entry("small_wall_shelf", rows(new double[] {9.5}, THREE, 12.5)),
            Map.entry("open_shelves", rows(new double[] {3, 13, 23}, THREE, 8)),
            Map.entry("potion_shelf", rows(new double[] {3, 12.5, 22}, THREE, 8)),
            Map.entry("bookshelf", rows(new double[] {3, 12.75, 22.5}, THREE, 8)),
            Map.entry("wall_shelf_medium", rows(new double[] {9}, new double[] {12, 7, 2, -2, -7, -12}, 12)),
            Map.entry("wall_shelf_large", rows(new double[] {5, 15, 25}, new double[] {10, 0, -10}, 12)),
            Map.entry("corner_shelf", List.of(
                    new double[] {14.5, 10, 9.25}, new double[] {13, 10, 11.5}, new double[] {10.5, 10, 14.75},
                    new double[] {14.5, 22, 9.25}, new double[] {13, 22, 11.5}, new double[] {10.5, 22, 14.75})),
            Map.entry("free_standing_shelf_short", rows(new double[] {3, 10}, THREE, 8)),
            Map.entry("free_standing_shelf_medium", rows(new double[] {2, 12, 22}, THREE, 8)),
            Map.entry("free_standing_shelf_large", rows(new double[] {10, 24, 38}, THREE, 8)),
            Map.entry("ladder_shelf", List.of(
                    new double[] {12, 3, 8.5}, new double[] {8, 3, 8.5}, new double[] {4, 3, 8.5},
                    new double[] {12, 13, 10.6}, new double[] {8, 13, 10.6}, new double[] {4, 13, 10.6},
                    new double[] {12, 23, 12.65}, new double[] {8, 23, 12.65}, new double[] {4, 23, 12.65})));

    private DisplaySpec() {
    }

    /** One row of anchors per board height, at the given x positions, all at depth {@code z}. */
    private static List<double[]> rows(double[] heights, double[] xs, double z) {
        List<double[]> out = new ArrayList<>();
        for (double y : heights) {
            for (double x : xs) {
                out.add(new double[] {x, y, z});
            }
        }
        return List.copyOf(out);
    }

    /** Whether a display piece has authored anchors. */
    public static boolean authored(FurnitureSpec spec) {
        return ANCHORS.containsKey(spec.id());
    }

    /** The anchors, one per slot in slot order: {x, y, z} pixels. */
    public static List<double[]> anchors(FurnitureSpec spec) {
        List<double[]> anchors = ANCHORS.get(spec.id());
        if (anchors == null) {
            throw new IllegalArgumentException("no display anchors for " + spec.path());
        }
        return anchors;
    }
}
