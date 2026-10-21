package io.github.dirtyredz.herbalwitch.client.stations;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Drawing shared by the station screens, from the sprites every generated atlas carries: a filled
 * progress arrow (24 x 17 at 176, 14) and a filled flame (14 x 14 at 176, 0).
 */
public final class ProcessWidgets {
    public static final int ARROW_U = 176;
    public static final int ARROW_V = 14;
    public static final int ARROW_W = 24;
    public static final int ARROW_H = 17;
    public static final int FLAME_U = 176;
    public static final int FLAME_V = 0;
    public static final int FLAME_SIZE = 14;

    private ProcessWidgets() {
    }

    /** The arrow filled left to right by {@code progress / duration}. */
    public static void arrow(GuiGraphics graphics, ResourceLocation atlas, int x, int y, int progress, int duration) {
        if (duration <= 0 || progress <= 0) {
            return;
        }
        int width = Math.min(ARROW_W, progress * ARROW_W / duration);
        graphics.blit(atlas, x, y, ARROW_U, ARROW_V, width, ARROW_H);
    }

    /** The flame filled bottom up by {@code fraction} (0..1). */
    public static void flame(GuiGraphics graphics, ResourceLocation atlas, int x, int y, float fraction) {
        int height = Math.round(Math.max(0, Math.min(1, fraction)) * FLAME_SIZE);
        if (height <= 0) {
            return;
        }
        int skip = FLAME_SIZE - height;
        graphics.blit(atlas, x, y + skip, FLAME_U, FLAME_V + skip, FLAME_SIZE, height);
    }

    /** A status line right-aligned in the title row. */
    public static void status(GuiGraphics graphics, Font font, Component text, int rightX, int y, int colour) {
        graphics.drawString(font, text, rightX - font.width(text), y, colour, false);
    }
}
