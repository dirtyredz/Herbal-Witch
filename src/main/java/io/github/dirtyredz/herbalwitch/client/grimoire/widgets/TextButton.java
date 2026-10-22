package io.github.dirtyredz.herbalwitch.client.grimoire.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * A small ink-on-parchment button drawn inside the book: a bordered label that highlights under the
 * mouse. Pages hit-test these themselves, so the book needs no screen-level widgets.
 *
 * @param x       left, screen coordinates
 * @param y       top
 * @param width   width; the height is fixed at {@link #HEIGHT}
 * @param label   text
 * @param enabled whether it reacts
 * @param action  what a click does
 */
public record TextButton(int x, int y, int width, Component label, boolean enabled, Runnable action) {
    public static final int HEIGHT = 12;
    static final int INK = 0xFF3B2416;
    static final int PLUM = 0xFF4A2A55;
    static final int GREEN = 0xFF2F5A2C;
    static final int PINK = 0xFFB0305A;
    static final int FAINT = 0xFF8E7A56;

    /** A button wide enough for its label. */
    public static TextButton fitted(Font font, int x, int y, Component label, boolean enabled, Runnable action) {
        return new TextButton(x, y, font.width(label) + 8, label, enabled, action);
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + HEIGHT;
    }

    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
        boolean hover = enabled && contains(mouseX, mouseY);
        int border = enabled ? (hover ? PINK : PLUM) : FAINT;
        graphics.fill(x, y, x + width, y + HEIGHT, border);
        graphics.fill(x + 1, y + 1, x + width - 1, y + HEIGHT - 1, hover ? 0xFFF2E8CF : 0xFFE8DAB8);
        graphics.drawString(font, label, x + (width - font.width(label)) / 2, y + 2, enabled ? INK : FAINT, false);
    }

    /** Runs the action if the click lands on an enabled button. */
    public boolean click(double mouseX, double mouseY) {
        if (enabled && contains(mouseX, mouseY)) {
            action.run();
            return true;
        }
        return false;
    }
}
