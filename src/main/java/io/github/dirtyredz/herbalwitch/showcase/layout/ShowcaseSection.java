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

    /** The section's placements; deterministic, catalogue order. */
    List<ShowcasePlacement> placements();
}
