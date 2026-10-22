package io.github.dirtyredz.herbalwitch.client.witch.hud;

import io.github.dirtyredz.herbalwitch.client.witch.hud.EnergyHudLayout.Anchor;
import io.github.dirtyredz.herbalwitch.client.witch.hud.EnergyHudLayout.Placement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The energy HUD's pure layout: nine anchors, scale, clamping into the viewport, fill width and easing. */
class EnergyHudLayoutTest {
    private static final int SW = 320;
    private static final int SH = 240;
    private static final int W = EnergyHudLayout.BAR_WIDTH;
    private static final int H = EnergyHudLayout.height(true);

    private static Placement place(Anchor anchor, int ox, int oy, double scale) {
        return EnergyHudLayout.place(anchor, ox, oy, scale, SW, SH, W, H);
    }

    @Test
    void heightAddsTheNumberLineOnlyWhenShown() {
        assertEquals(10, EnergyHudLayout.height(false));
        assertEquals(20, EnergyHudLayout.height(true));
    }

    @Test
    void bottomLeftMeasuresOffsetsInwardFromItsEdges() {
        Placement p = place(Anchor.BOTTOM_LEFT, 8, 18, 1.0);
        assertEquals(8, p.x());
        assertEquals(SH - 20 - 18, p.y());
        assertEquals(1.0F, p.scale());
    }

    @Test
    void allNineAnchorsPlaceWithInwardOffsets() {
        int ox = 10;
        int oy = 6;
        int left = ox;
        int centerX = (SW - W) / 2 + ox;
        int right = SW - W - ox;
        int top = oy;
        int centerY = (SH - H) / 2 + oy;
        int bottom = SH - H - oy;
        int[][] expected = {
                {left, top}, {centerX, top}, {right, top},
                {left, centerY}, {centerX, centerY}, {right, centerY},
                {left, bottom}, {centerX, bottom}, {right, bottom}};
        Anchor[] anchors = Anchor.values();
        assertEquals(9, anchors.length);
        for (int i = 0; i < anchors.length; i++) {
            Placement p = place(anchors[i], ox, oy, 1.0);
            assertEquals(expected[i][0], p.x(), anchors[i] + " x");
            assertEquals(expected[i][1], p.y(), anchors[i] + " y");
            assertEquals(1.0F, p.scale(), anchors[i] + " scale");
        }
    }

    @Test
    void scaleTwoDoublesTheSize() {
        Placement topLeft = place(Anchor.TOP_LEFT, 0, 0, 2.0);
        assertEquals(2.0F, topLeft.scale());
        assertEquals(0, topLeft.x());
        assertEquals(0, topLeft.y());
        Placement bottomRight = place(Anchor.BOTTOM_RIGHT, 0, 0, 2.0);
        assertEquals(SW - 2 * W, bottomRight.x());
        assertEquals(SH - 2 * H, bottomRight.y());
    }

    @Test
    void negativeOffsetsAreClampedOntoTheScreen() {
        Placement p = place(Anchor.TOP_LEFT, -50, -50, 1.0);
        assertEquals(0, p.x());
        assertEquals(0, p.y());
        Placement q = place(Anchor.BOTTOM_RIGHT, -50, -50, 1.0);
        assertEquals(SW - W, q.x());
        assertEquals(SH - H, q.y());
    }

    @Test
    void hugeOffsetsAreClampedOntoTheScreen() {
        Placement p = place(Anchor.TOP_LEFT, 5_000, 5_000, 1.0);
        assertEquals(SW - W, p.x());
        assertEquals(SH - H, p.y());
        Placement q = place(Anchor.BOTTOM_RIGHT, 5_000, 5_000, 1.0);
        assertEquals(0, q.x());
        assertEquals(0, q.y());
        Placement c = place(Anchor.CENTER, -5_000, 5_000, 1.0);
        assertEquals(0, c.x());
        assertEquals(SH - H, c.y());
    }

    @Test
    void aSmallViewportReducesTheScaleToFit() {
        Placement p = EnergyHudLayout.place(Anchor.TOP_LEFT, 0, 0, 2.0, W / 2, 100, W, H);
        assertEquals(0.5F, p.scale());
        assertEquals(0, p.x());
        Placement fits = EnergyHudLayout.place(Anchor.TOP_LEFT, 0, 0, 1.0, 2 * W, 2 * H, W, H);
        assertEquals(1.0F, fits.scale(), "no reduction when it already fits");
    }

    @Test
    void scaleNeverDropsBelowAQuarter() {
        Placement p = EnergyHudLayout.place(Anchor.BOTTOM_RIGHT, 0, 0, 1.0, 10, 10, W, H);
        assertEquals(0.25F, p.scale());
        assertTrue(p.x() >= 0 && p.y() >= 0, "still clamped to the screen origin");
        Placement tiny = place(Anchor.CENTER, 0, 0, 0.01);
        assertEquals(0.25F, tiny.scale(), "a requested scale below the floor is raised to it");
    }

    @Test
    void fillIsZeroForAnEmptyOrUnknownBar() {
        assertEquals(0, EnergyHudLayout.fill(0, 10_000, W));
        assertEquals(0, EnergyHudLayout.fill(-5, 10_000, W));
        assertEquals(0, EnergyHudLayout.fill(5_000, 0, W));
        assertEquals(0, EnergyHudLayout.fill(5_000, -1, W));
    }

    @Test
    void fillShowsAtLeastOnePixelForAnyPositiveEnergy() {
        assertEquals(1, EnergyHudLayout.fill(1, 20_000, W));
        assertEquals(1, EnergyHudLayout.fill(10, 20_000, W));
    }

    @Test
    void fillScalesProportionallyAndNeverExceedsTheWidth() {
        assertEquals(W, EnergyHudLayout.fill(10_000, 10_000, W));
        assertEquals(W, EnergyHudLayout.fill(20_000, 10_000, W), "energy above capacity is capped");
        assertEquals(W / 2, EnergyHudLayout.fill(5_000, 10_000, W));
        assertEquals(W, EnergyHudLayout.fill(Integer.MAX_VALUE, 1, W));
    }

    @Test
    void easeMovesTowardTheTargetByAFractionOfTheGap() {
        assertEquals(5.0F, EnergyHudLayout.ease(0, 10, 0.5F));
        assertEquals(5.0F, EnergyHudLayout.ease(10, 0, 0.5F));
        assertEquals(2.5F, EnergyHudLayout.ease(0, 10, 0.25F));
    }

    @Test
    void easeSnapsWithinHalfAPixel() {
        assertEquals(10.0F, EnergyHudLayout.ease(9.7F, 10, 0.5F));
        assertEquals(10.0F, EnergyHudLayout.ease(9.6F, 10, 0.0F));
        assertEquals(10.0F, EnergyHudLayout.ease(10, 10, 0.5F));
    }

    @Test
    void easeNeverOvershootsTheTarget() {
        assertEquals(10.0F, EnergyHudLayout.ease(0, 10, 1.0F));
        assertEquals(10.0F, EnergyHudLayout.ease(0, 10, 5.0F), "a fraction above one is capped");
        assertEquals(0.0F, EnergyHudLayout.ease(10, 0, 5.0F));
        assertEquals(0.0F, EnergyHudLayout.ease(0, 10, -1.0F), "a negative fraction does not move away");
        float shown = 0;
        for (int i = 0; i < 60; i++) {
            shown = EnergyHudLayout.ease(shown, 41, 0.3F);
            assertTrue(shown <= 41.0F);
        }
        assertEquals(41.0F, shown);
    }
}
