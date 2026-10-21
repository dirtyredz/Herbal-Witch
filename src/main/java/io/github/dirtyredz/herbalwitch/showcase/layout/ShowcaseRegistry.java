package io.github.dirtyredz.herbalwitch.showcase.layout;

import java.util.ArrayList;
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
        if (byId(section.id()).isPresent()) {
            throw new IllegalStateException("duplicate showcase section " + section.id());
        }
        SECTIONS.add(section);
    }

    public static synchronized List<ShowcaseSection> sections() {
        return Collections.unmodifiableList(new ArrayList<>(SECTIONS));
    }

    public static synchronized Optional<ShowcaseSection> byId(String id) {
        return SECTIONS.stream().filter(s -> s.id().equals(id)).findFirst();
    }
}
