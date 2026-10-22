package io.github.dirtyredz.herbalwitch.client.ritual.widgets;

import io.github.dirtyredz.herbalwitch.magic.ritual.network.AltarRequest;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.UUID;

/** What an altar panel may do while the screen is rebuilt: write text, add a button, send intent. */
public interface AltarUi {
    int LINE = 10;

    /** Writes {@code text} wrapped to {@code width} at (x, y); returns the height used. */
    int text(Component text, int x, int y, int width, int color);

    /** Adds a button; inactive buttons show but do nothing. */
    void button(Component label, int x, int y, int width, boolean active, Runnable onPress);

    /** Sends intent for this altar menu (the server derives everything else). */
    void send(AltarRequest.Op op, Optional<UUID> target, long offer);

    /** Local UI state changed: rebuild the screen. */
    void refresh();
}
