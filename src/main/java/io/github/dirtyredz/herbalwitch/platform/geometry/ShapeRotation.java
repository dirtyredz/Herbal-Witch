package io.github.dirtyredz.herbalwitch.platform.geometry;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/**
 * Turns pixel boxes authored facing north into collision shapes for every horizontal facing. The
 * turn matches a blockstate {@code y} rotation (clockwise seen from above), so a shape and its model
 * always agree. Boxes are {@code {minX, minY, minZ, maxX, maxY, maxZ}} in pixels (0..16).
 */
public final class ShapeRotation {
    private ShapeRotation() {
    }

    /** Quarter turns from north: north 0, east 1, south 2, west 3 (the blockstate y rotation / 90). */
    public static int quarterTurns(Direction facing) {
        return (facing.get2DDataValue() + 2) % 4;
    }

    /** One box turned clockwise (seen from above) by {@code quarterTurns}: (x, z) becomes (16 - z, x) per turn. */
    public static double[] rotate(double[] box, int quarterTurns) {
        double[] b = box.clone();
        for (int i = 0; i < Math.floorMod(quarterTurns, 4); i++) {
            b = new double[] {16 - b[5], b[1], b[0], 16 - b[2], b[4], b[3]};
        }
        return b;
    }

    /** The union of the boxes, unrotated. */
    public static VoxelShape shape(double[]... boxes) {
        VoxelShape shape = Shapes.empty();
        for (double[] b : boxes) {
            shape = Shapes.or(shape, Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
        }
        return shape.optimize();
    }

    /** The union of the boxes for each horizontal facing. */
    public static Map<Direction, VoxelShape> horizontal(double[]... northBoxes) {
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            double[][] turned = new double[northBoxes.length][];
            for (int i = 0; i < northBoxes.length; i++) {
                turned[i] = rotate(northBoxes[i], quarterTurns(facing));
            }
            shapes.put(facing, shape(turned));
        }
        return shapes;
    }
}
