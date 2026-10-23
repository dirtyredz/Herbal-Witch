package io.github.dirtyredz.herbalwitch.companions.familiars.home;

import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarHomeKind;
import io.github.dirtyredz.herbalwitch.platform.geometry.ShapeRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.Map;

/** Main solid surfaces from the Blockbench drafts; tiny ribbons and foliage do not obstruct motion. */
public final class FamiliarHomeShapes {
    private FamiliarHomeShapes() {}
    public static final Map<FamiliarHomeKind, Map<Direction, VoxelShape>> SHAPES = Map.of(
        FamiliarHomeKind.BED, ShapeRotation.horizontal(new double[][]{
            {1,0,1,15,1.5,15}, {1,1.5,2.5,2.5,5,13.5}, {13.5,1.5,2.5,15,5,13.5},
            {1,1.5,1,2.5,6,2.5}, {13.5,1.5,1,15,6,2.5}, {1,1.5,13.5,2.5,6,15}, {13.5,1.5,13.5,15,6,15},
            {2.5,1.5,13.5,13.5,7,15}, {2.5,1.5,1,13.5,2.5,2.5}, {2.5,1.5,2.5,13.5,3.75,13.5}, {4,3.75,10,12,5,13.25}}),
        FamiliarHomeKind.NEST, ShapeRotation.horizontal(new double[][]{
            {3,0,3,13,1.25,13}, {4,1.25,4,12,2.1,12}, {2,1,2,14,3.5,3.8},
            {2,1,12.2,14,3.5,14}, {2,1,3.8,3.8,3.65,12.2}, {12.2,1,3.8,14,3.65,12.2}}),
        FamiliarHomeKind.PERCH, ShapeRotation.horizontal(new double[][]{
            {2,0,6.5,14,1.5,9.5}, {6.5,0,2,9.5,1.5,14}, {6.75,1.5,6.75,9.25,11.5,9.25}, {2,11.5,6.75,14,13,9.25}}));
}
