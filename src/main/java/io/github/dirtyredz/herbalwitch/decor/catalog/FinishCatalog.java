package io.github.dirtyredz.herbalwitch.decor.catalog;

import net.minecraft.world.level.material.MapColor;

import java.util.List;

/**
 * The furniture designs. The owner rejected the 18-finish system (2026-10-21): one design is
 * registered, dark wood with deep green and hot pink fabric, dark purple details and trailing
 * vines. Its colours live in the textures; nothing is tinted.
 */
public final class FinishCatalog {
    public static final Finish COTTAGE = new Finish("cottage", "Cottage", MapColor.COLOR_BROWN);

    /** Every registered design; exactly one for now. */
    public static final List<Finish> ALL = List.of(COTTAGE);

    /** The design every piece is registered in. */
    public static final Finish BASE = COTTAGE;

    private FinishCatalog() {
    }
}
