package io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block;

import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceSpec;
import io.github.dirtyredz.herbalwitch.platform.geometry.ShapeRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/**
 * Collision boxes of the four commerce pieces, authored facing north from their Blockbench drafts
 * (wall pieces hang on the south side of their cell). Small trim does not obstruct movement.
 */
public final class ShopShapes {
    private static final Map<String, double[][]> BOXES = Map.of(
            ShopPieceCatalog.REGISTER.key(), new double[][] {{3, 0, 4, 13, 6, 12}, {4, 6, 6, 12, 9, 11}},
            ShopPieceCatalog.STOCK_SHELF.key(), new double[][] {{0, 2, 14, 16, 14, 16}, {0, 4, 9, 16, 5.5, 16}},
            ShopPieceCatalog.STOCK_JAR.key(), new double[][] {{4, 0, 4, 12, 10, 12}},
            ShopPieceCatalog.ORDER_BOARD.key(), new double[][] {{1, 1, 14, 15, 15, 16}});

    private ShopShapes() {
    }

    public static Map<Direction, VoxelShape> of(ShopPieceSpec spec) {
        double[][] boxes = BOXES.get(spec.key());
        if (boxes == null) throw new IllegalArgumentException("no shape for " + spec.key());
        return ShapeRotation.horizontal(boxes);
    }
}
