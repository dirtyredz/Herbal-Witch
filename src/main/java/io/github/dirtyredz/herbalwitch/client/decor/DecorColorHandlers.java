package io.github.dirtyredz.herbalwitch.client.decor;

import io.github.dirtyredz.herbalwitch.decor.catalog.Finish;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;

/**
 * Finish colours for furniture models: tint index 0 is the frame, 1 the fabric (upholstery, panels,
 * lamp shade, rug). Faces without a tint index (brass, glass, tinctures) keep their own colours. The
 * same colours are registered for the placed block and its item, so both look alike.
 */
public final class DecorColorHandlers {
    public static final int FRAME_TINT = 0;
    public static final int FABRIC_TINT = 1;

    private DecorColorHandlers() {
    }

    /** The tint for a face of a piece in {@code finish}; -1 means "untinted". */
    public static int color(Finish finish, int tintIndex) {
        return switch (tintIndex) {
            case FRAME_TINT -> finish.frameColor();
            case FABRIC_TINT -> finish.fabricColor();
            default -> -1;
        };
    }

    public static void registerBlocks(RegisterColorHandlersEvent.Block event) {
        for (DecorBlocks.Entry entry : DecorBlocks.entries()) {
            Finish finish = entry.finish();
            event.register((state, level, pos, tint) -> color(finish, tint), entry.block().get());
        }
    }

    public static void registerItems(RegisterColorHandlersEvent.Item event) {
        for (DecorBlocks.Entry entry : DecorBlocks.entries()) {
            Finish finish = entry.finish();
            event.register((stack, tint) -> color(finish, tint), entry.block().get().asItem());
        }
    }
}
