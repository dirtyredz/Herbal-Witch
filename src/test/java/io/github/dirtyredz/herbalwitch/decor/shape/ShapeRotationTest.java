package io.github.dirtyredz.herbalwitch.decor.shape;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * The box turn must match a blockstate y rotation (clockwise from above), or collision and model
 * would disagree for east, south and west facings.
 */
class ShapeRotationTest {
    /** A box touching the north (front) edge, left of centre. */
    private static final double[] FRONT_LEFT = {1, 0, 0, 5, 4, 2};

    @Test
    void oneTurnMovesTheNorthEdgeToTheEast() {
        assertArrayEquals(new double[] {14, 0, 1, 16, 4, 5}, ShapeRotation.rotate(FRONT_LEFT, 1));
    }

    @Test
    void twoTurnsMirrorBothAxes() {
        assertArrayEquals(new double[] {11, 0, 14, 15, 4, 16}, ShapeRotation.rotate(FRONT_LEFT, 2));
    }

    @Test
    void threeTurnsPutTheFrontOnTheWest() {
        assertArrayEquals(new double[] {0, 0, 11, 2, 4, 15}, ShapeRotation.rotate(FRONT_LEFT, 3));
    }

    @Test
    void fourTurnsAreTheIdentity() {
        assertArrayEquals(FRONT_LEFT, ShapeRotation.rotate(FRONT_LEFT, 4));
        assertArrayEquals(FRONT_LEFT, ShapeRotation.rotate(FRONT_LEFT, 0));
    }
}
