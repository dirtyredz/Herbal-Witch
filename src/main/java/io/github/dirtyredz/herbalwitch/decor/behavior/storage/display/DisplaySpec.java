package io.github.dirtyredz.herbalwitch.decor.behavior.storage.display;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Where each display slot's item stands, authored against the Blockbench project: one anchor per
 * slot, the centre of the item's base on its shelf board, in the structure's north-facing pixels
 * (cell 0 at x 0..16, a step to the viewer's right at x - 16, up at y + 16). Slots fill shelf by
 * shelf from the bottom, left to right as the viewer sees them. Plain data.
 */
public final class DisplaySpec {
    /** Shelf-board top heights and the x positions along each board, per display piece. */
    private record Shelves(double[] heights, double[] xs, double z) {
    }

    private static final double[] THREE = {12, 8, 4};

    private static final Map<String, Shelves> SHELVES = Map.ofEntries(
            Map.entry("small_wall_shelf", new Shelves(new double[] {9.5}, THREE, 12.5)),
            Map.entry("open_shelves", new Shelves(new double[] {3, 13, 23}, THREE, 8)),
            Map.entry("potion_shelf", new Shelves(new double[] {3, 12.5, 22}, THREE, 8)));

    private DisplaySpec() {
    }

    /** Whether a display piece has authored anchors. */
    public static boolean authored(FurnitureSpec spec) {
        return SHELVES.containsKey(spec.id());
    }

    /** The anchors, one per slot in slot order: {x, y, z} pixels. */
    public static List<double[]> anchors(FurnitureSpec spec) {
        Shelves shelves = SHELVES.get(spec.id());
        if (shelves == null) {
            throw new IllegalArgumentException("no display anchors for " + spec.path());
        }
        List<double[]> out = new ArrayList<>();
        for (double y : shelves.heights()) {
            for (double x : shelves.xs()) {
                out.add(new double[] {x, y, shelves.z()});
            }
        }
        return out;
    }
}
