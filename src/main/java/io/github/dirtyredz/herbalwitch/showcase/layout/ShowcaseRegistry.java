package io.github.dirtyredz.herbalwitch.showcase.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** The ordered showcase sections. Features register during bootstrap; the order is the layout order. */
public final class ShowcaseRegistry {
    private static final List<ShowcaseSection> SECTIONS = new ArrayList<>();

    private ShowcaseRegistry() {
    }

    public static synchronized void register(ShowcaseSection section) {
        if (!section.id().matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException("bad showcase section id " + section.id());
        }
        if (group(section.id()).isPresent()) {
            throw new IllegalArgumentException("showcase section id " + section.id() + " is a group name");
        }
        if (byId(section.id()).isPresent()) {
            throw new IllegalStateException("duplicate showcase section " + section.id());
        }
        SECTIONS.add(section);
    }

    /**
     * Registers {@code section} directly after the section {@code after} (appended when that section is
     * absent), so a late feature can take a free slot in an earlier row without reflowing later rows.
     */
    public static synchronized void registerAfter(String after, ShowcaseSection section) {
        register(section);
        SECTIONS.remove(section);
        int at = SECTIONS.indexOf(byId(after).orElse(null));
        SECTIONS.add(at < 0 ? SECTIONS.size() : at + 1, section);
    }

    public static synchronized List<ShowcaseSection> sections() {
        return Collections.unmodifiableList(new ArrayList<>(SECTIONS));
    }

    /** The registered sections of one showcase, in layout order. */
    public static synchronized List<ShowcaseSection> sections(ShowcaseGroup group) {
        return SECTIONS.stream().filter(s -> s.group() == group).toList();
    }

    /** The group a command argument names, if it names one. */
    public static Optional<ShowcaseGroup> group(String id) {
        return Arrays.stream(ShowcaseGroup.values()).filter(g -> g.id().equals(id)).findFirst();
    }

    public static synchronized Optional<ShowcaseSection> byId(String id) {
        return SECTIONS.stream().filter(s -> s.id().equals(id)).findFirst();
    }
}
