package io.github.dirtyredz.herbalwitch.decor.catalog;

/**
 * One furniture piece: its stable id, name, behaviour family, the authored
 * template model it uses and how it behaves when placed. Plain data; registration and datagen read it.
 *
 * @param id          stable piece segment, e.g. {@code armchair}; never renamed once released
 * @param displayName English name, e.g. {@code Armchair}
 * @param family      behaviour family; also the first path segment under {@code decor/}
 * @param template    template model name under {@code models/block/decor/templates/} (lamps add
 *                    {@code _off} / {@code _on})
 * @param behavior    placement and interaction rules
 */
public record FurnitureSpec(String id, String displayName, Family family, String template, Behavior behavior) {

    /** Behaviour families. Room categories are catalogue metadata (docs/catalogue/decor.md), not families. */
    public enum Family {
        SEATING("seating"),
        SURFACES("surfaces"),
        STORAGE("storage"),
        LIGHTING("lighting"),
        TEXTILES("textiles");

        private final String directory;

        Family(String directory) {
            this.directory = directory;
        }

        public String directory() {
            return directory;
        }
    }

    public enum Behavior {
        /** Faces the placer; no support needed. */
        FREESTANDING,
        /** Faces away from the wall it hangs on; breaks when the wall goes. */
        WALL_MOUNTED,
        /** Needs a sturdy surface below; toggled on and off by hand, lit at light 14. */
        TABLE_LAMP,
        /** Vanilla carpet behaviour: needs a block below, no facing. */
        RUG
    }

    /** {@code decor/<family>/<piece>}: the registry path and every generated path. */
    public String path() {
        return "decor/" + family.directory() + "/" + id;
    }
}
