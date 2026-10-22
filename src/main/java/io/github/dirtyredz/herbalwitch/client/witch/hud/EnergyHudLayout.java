package io.github.dirtyredz.herbalwitch.client.witch.hud;

/**
 * Where the energy HUD goes and how its fill moves, as pure arithmetic (no client classes, so it is
 * unit-tested): a nine-way anchor with offsets measured inward from that anchor's edges, a scale
 * reduced if the element would not fit, and a position clamped into the GUI-scaled viewport; and a
 * fill that eases toward the last received value without ever predicting past it.
 */
public final class EnergyHudLayout {
    /** Where on the screen the element is anchored. */
    public enum Anchor {
        TOP_LEFT,
        TOP,
        TOP_RIGHT,
        LEFT,
        CENTER,
        RIGHT,
        BOTTOM_LEFT,
        BOTTOM,
        BOTTOM_RIGHT;

        int column() {
            return ordinal() % 3;
        }

        int row() {
            return ordinal() / 3;
        }
    }

    /** The element's top-left corner in GUI pixels and the scale to draw it at. */
    public record Placement(int x, int y, float scale) {
    }

    /** Bar width and height in GUI pixels at scale 1. */
    public static final int BAR_WIDTH = 82;
    public static final int BAR_HEIGHT = 10;
    /** Height of the number line above the bar. */
    public static final int TEXT_HEIGHT = 10;

    private EnergyHudLayout() {
    }

    /** The element's size at scale 1: the bar, and the number line above it when shown. */
    public static int height(boolean numeric) {
        return BAR_HEIGHT + (numeric ? TEXT_HEIGHT : 0);
    }

    /**
     * Places an element of {@code width} x {@code height} (scale 1) on a {@code screenWidth} x
     * {@code screenHeight} viewport.
     */
    public static Placement place(Anchor anchor, int offsetX, int offsetY, double scale, int screenWidth, int screenHeight, int width,
                                  int height) {
        double fit = Math.min(screenWidth / (double) width, screenHeight / (double) height);
        float s = (float) Math.max(0.25, Math.min(scale, fit));
        int w = (int) Math.ceil(width * s);
        int h = (int) Math.ceil(height * s);
        int x = switch (anchor.column()) {
            case 0 -> offsetX;
            case 1 -> (screenWidth - w) / 2 + offsetX;
            default -> screenWidth - w - offsetX;
        };
        int y = switch (anchor.row()) {
            case 0 -> offsetY;
            case 1 -> (screenHeight - h) / 2 + offsetY;
            default -> screenHeight - h - offsetY;
        };
        x = Math.max(0, Math.min(x, Math.max(0, screenWidth - w)));
        y = Math.max(0, Math.min(y, Math.max(0, screenHeight - h)));
        return new Placement(x, y, s);
    }

    /** Pixels of fill for {@code energy} of {@code capacity} across {@code width}; 0 for an empty or unknown bar. */
    public static int fill(int energy, int capacity, int width) {
        if (capacity <= 0 || energy <= 0) {
            return 0;
        }
        return (int) Math.min(width, Math.max(1, (long) energy * width / capacity));
    }

    /**
     * Eases a displayed fill toward the received {@code target}, never beyond it: each frame closes a
     * fraction of the gap and snaps when within a pixel.
     */
    public static float ease(float displayed, float target, float fraction) {
        float next = displayed + (target - displayed) * Math.max(0, Math.min(1, fraction));
        return Math.abs(target - next) < 0.5F ? target : next;
    }
}
