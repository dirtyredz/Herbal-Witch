package io.github.dirtyredz.herbalwitch.platform.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;

/**
 * Where a player aims a placement: the cell the structure's anchor goes in (the clicked block when it
 * is replaceable, otherwise the cell on the aimed face, exactly as vanilla's
 * {@link BlockPlaceContext#getClickedPos()}), the aimed face and the player's horizontal look.
 *
 * @param cell the targeted cell
 * @param face the aimed face of the clicked block
 * @param look the player's horizontal facing
 */
public record PlacementTarget(BlockPos cell, Direction face, Direction look) {
    public PlacementTarget {
        cell = cell.immutable();
    }

    public static PlacementTarget of(BlockPlaceContext context) {
        return new PlacementTarget(context.getClickedPos(), context.getClickedFace(), context.getHorizontalDirection());
    }

    /** The targeted cell: {@code clicked} itself when replaceable, else its neighbour on {@code face}. */
    public static BlockPos cellFor(BlockPos clicked, Direction face, boolean clickedReplaceable) {
        return clickedReplaceable ? clicked.immutable() : clicked.relative(face);
    }
}
