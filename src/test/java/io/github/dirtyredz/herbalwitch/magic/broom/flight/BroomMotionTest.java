package io.github.dirtyredz.herbalwitch.magic.broom.flight;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BroomMotionTest {
    @Test void diagonalsNeverExceedSpeedAtAnyHeading() {
        for (int yaw = -360; yaw <= 360; yaw++) {
            for (int forward = -1; forward <= 1; forward++) {
                for (int side = -1; side <= 1; side++) {
                    var motion = BroomMotion.target(forward, side, yaw, 8);
                    assertTrue(Math.hypot(motion.x(), motion.z()) <= 0.400000001);
                }
            }
        }
    }

    @Test void accelerationIsVectorBoundedAndBrakingDoesNotOvershoot() {
        var motion = new BroomMotion.Horizontal(0, 0);
        var target = BroomMotion.target(1, 1, 0, 8);
        for (int tick = 0; tick < 30; tick++) {
            var next = BroomMotion.accelerate(motion, target, 8);
            assertTrue(Math.hypot(next.x() - motion.x(), next.z() - motion.z()) <= 0.020000001);
            motion = next;
        }
        assertEquals(target, motion);
        for (int tick = 0; tick < 30; tick++) {
            motion = BroomMotion.accelerate(motion, new BroomMotion.Horizontal(0, 0), 8);
        }
        assertEquals(new BroomMotion.Horizontal(0, 0), motion);
    }

    @Test void shortestTurnAndInvalidNumbers() {
        assertEquals(181, BroomMotion.turn(179, -179, 120));
        assertEquals(6, BroomMotion.turn(0, 90, 120));
        assertThrows(IllegalArgumentException.class, () -> BroomMotion.target(1, 0, Double.NaN, 8));
        assertThrows(IllegalArgumentException.class, () -> BroomMotion.target(Integer.MIN_VALUE, 0, 0, 8));
        assertThrows(IllegalArgumentException.class, () -> BroomMotion.target(1, 0, 0, Double.POSITIVE_INFINITY));
    }
}
