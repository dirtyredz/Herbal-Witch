package io.github.dirtyredz.herbalwitch.client.grimoire.guide.content;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GuideRecipeSlotTest {
    @Test void wideTooltipNeverEscapesTheLeftEdgeAfterRightOverflow() {
        // Pinned GuideME computes 120 - 16 - 240 = -136 for this case.
        assertEquals(4, GuideRecipeSlot.position(320, 240, 120, 80, 240, 40).x);
    }

    @Test void allEdgesAndGuiScalesStayInsideTheViewport() {
        for (int width : new int[]{320, 480, 960}) {
            for (int x : new int[]{0, 12, width / 2, width - 1}) {
                for (int y : new int[]{0, 120, 239}) {
                    var pos = GuideRecipeSlot.position(width, 240, x, y, width - 16, 60);
                    assertTrue(pos.x >= 4 && pos.x + width - 16 <= width - 4);
                    assertTrue(pos.y >= 4 && pos.y + 60 <= 236);
                }
            }
        }
    }
}
