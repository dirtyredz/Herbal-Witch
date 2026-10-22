package io.github.dirtyredz.herbalwitch.showcase.layout;

import java.util.List;

/**
 * One feature's contribution to {@code /herbalwitch showcase}. A section lists the placements that
 * demonstrate its content in local coordinates ({@code x, z >= 0}); the plan positions sections side
 * by side and adds each heading. Sections describe; they never write to the world.
 *
 * <p>Every feature that adds blocks or items registers a section in {@link ShowcaseRegistry} during
 * bootstrap and keeps it complete (CLAUDE.md).
 */
public interface ShowcaseSection {
    /** Stable lower-case id, used as the command's optional section argument. */
    String id();

    /** Translation key of the section's heading sign. */
    String titleKey();

    /** The showcase this section is built in; everything but furniture is witchcraft. */
    default ShowcaseGroup group() {
        return ShowcaseGroup.WITCHCRAFT;
    }

    /**
     * The section's placements; deterministic, catalogue order. Sign rule: a sign for a block stands on
     * the floor in the block's own front row, one cell west of it (the viewer's right), and the cell
     * north of every sign and block stays open; notes that annotate no block stand in the front row
     * with nothing behind them.
     */
    List<ShowcasePlacement> placements();
}
