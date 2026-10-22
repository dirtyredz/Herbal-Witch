package io.github.dirtyredz.herbalwitch.client.placement;

import io.github.dirtyredz.herbalwitch.platform.placement.PlacementPlan;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Camera-relative geometry of a preview: each planned cell's offset from the camera and the outline
 * box of each cell (slightly inflated so it never z-fights the world). Pure arithmetic over the
 * resolved plan; the preview draws nothing the plan does not contain.
 */
public final class PlacementPreviewGeometry {
    private static final double INFLATE = 0.002;

    private PlacementPreviewGeometry() {
    }

    /** One cell to draw: where its block's minimum corner sits relative to the camera. */
    public record Cell(PlacementPlan.Cell cell, Vec3 offset, AABB outline) {
    }

    public static List<Cell> cells(PlacementPlan plan, Vec3 camera) {
        return plan.cells().stream().map(c -> {
            BlockPos pos = c.pos();
            Vec3 offset = new Vec3(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
            return new Cell(c, offset, new AABB(0, 0, 0, 1, 1, 1).move(offset).inflate(INFLATE));
        }).toList();
    }
}
