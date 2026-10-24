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
     * The section's placements; deterministic, catalogue order. Use {@link ShowcasePlacement#sampleLabel}
     * with the sample's eastern edge and
     * front row. It stands two cells east (the viewer's left), with one clear cell. The cell
     * north of every sign and block stays open; notes that annotate no block stand in a separate clear row
     * with nothing behind them.
     */
    List<ShowcasePlacement> placements();
    default List<ShowcaseEntitySample> entities() { return List.of(); }
}
