package io.github.dirtyredz.herbalwitch.decor.catalog;

import io.github.dirtyredz.herbalwitch.decor.catalog.MountSpec.Support;

/**
 * One furniture piece: its permanent id and name, its family and art group, the milestone wave that
 * delivers it, what it does ({@link FurnitureBehavior}), where it stands and which cells it fills
 * ({@link MountSpec}), its seat or slot count, and where its compiled models live. Plain data;
 * registration, datagen, the showcase and the grimoire read it.
 *
 * @param id          stable piece segment, e.g. {@code armchair}; never renamed once released
 * @param displayName English name
 * @param family      behaviour family; the first path segment under {@code decor/}
 * @param group       art and recipe group inside the family ({@code chairs}, {@code cabinets}...), or
 *                    null for the five 0.1.0 pieces, whose art predates groups
 * @param wave        the Milestone 5 wave that delivers it ({@link Wave#M1} for the five 0.1.0 pieces)
 * @param behavior    what using it does
 * @param mount       support and cells
 * @param seats       seats ({@link FurnitureBehavior#SEATING}), else 0
 * @param slots       inventory slots ({@link FurnitureBehavior#STORAGE}, {@link FurnitureBehavior#DISPLAY}), else 0
 * @param template    the 0.1.0 template name under {@code models/block/decor/templates/}, or null for
 *                    pieces compiled under {@code models/block/decor/pieces/}
 */
public record FurnitureSpec(String id, String displayName, Family family, String group, Wave wave, FurnitureBehavior behavior,
                            MountSpec mount, int seats, int slots, String template) {

    /** Behaviour families. Room categories are catalogue metadata (docs/catalogue/decor.md), not families. */
    public enum Family {
        SEATING("seating"),
        SURFACES("surfaces"),
        STORAGE("storage"),
        LIGHTING("lighting"),
        TEXTILES("textiles"),
        FIXTURES("fixtures"),
        OPENINGS("openings");

        private final String directory;

        Family(String directory) {
            this.directory = directory;
        }

        public String directory() {
            return directory;
        }
    }

    /** Delivery waves: the 0.1.0 art spike, then Milestone 5's A, B and C. */
    public enum Wave {
        M1,
        A,
        B,
        C
    }

    public FurnitureSpec {
        if ((behavior == FurnitureBehavior.SEATING) != (seats > 0)) {
            throw new IllegalArgumentException(id + ": seats belong to seating only");
        }
        boolean stores = behavior == FurnitureBehavior.STORAGE || behavior == FurnitureBehavior.DISPLAY;
        if (stores != (slots > 0)) {
            throw new IllegalArgumentException(id + ": slots belong to storage and display only");
        }
        if ((group == null) != (template != null)) {
            throw new IllegalArgumentException(id + ": a piece has an art group or a 0.1.0 template, not both");
        }
    }

    /** {@code decor/<family>/<piece>}: the registry path, item, blockstate and loot path. */
    public String path() {
        return "decor/" + family.directory() + "/" + id;
    }

    /** The recipe id path: the five 0.1.0 pieces keep {@link #path()}, the rest add their group. */
    public String recipePath() {
        return group == null ? path() : "decor/" + family.directory() + "/" + group + "/" + id;
    }

    /** The compiled model directory (new pieces) or template stem (0.1.0 pieces) under {@code models/}. */
    public String modelRoot() {
        return template != null ? "block/decor/templates/" + template
                : "block/decor/pieces/" + family.directory() + "/" + group + "/" + id;
    }

    /** The Blockbench project directory under src/art/blockbench (documentation and art tests). */
    public String artProject() {
        return template != null ? "decor/" + template : "decor/pieces/" + family.directory() + "/" + group + "/" + id;
    }

    public Support support() {
        return mount.support();
    }

    /** Whether the block has a facing. The 0.1.0 table lamp and rug have none (their saved states stay valid). */
    public boolean directional() {
        return !(template != null && (behavior == FurnitureBehavior.LIGHT || behavior == FurnitureBehavior.RUG));
    }

    public boolean multiblock() {
        return mount.multiblock();
    }

    /** Builds catalogue entries compactly. */
    public static Builder piece(Wave wave, Family family, String group, String id, String displayName) {
        return new Builder(wave, family, group, id, displayName);
    }

    public static final class Builder {
        private final Wave wave;
        private final Family family;
        private final String group;
        private final String id;
        private final String displayName;
        private String template;
        private int seats;
        private int slots;

        private Builder(Wave wave, Family family, String group, String id, String displayName) {
            this.wave = wave;
            this.family = family;
            this.group = group;
            this.id = id;
            this.displayName = displayName;
        }

        /** A 0.1.0 piece keeping its template name. */
        public Builder template(String name) {
            this.template = name;
            return this;
        }

        public Builder seats(int count) {
            this.seats = count;
            return this;
        }

        public Builder slots(int count) {
            this.slots = count;
            return this;
        }

        public FurnitureSpec build(FurnitureBehavior behavior, MountSpec mount) {
            return new FurnitureSpec(id, displayName, family, group, wave, behavior, mount, seats, slots, template);
        }

        /** A width x depth x height box. */
        public FurnitureSpec build(FurnitureBehavior behavior, Support support, int width, int depth, int height) {
            return build(behavior, MountSpec.box(support, width, depth, height));
        }
    }
}
