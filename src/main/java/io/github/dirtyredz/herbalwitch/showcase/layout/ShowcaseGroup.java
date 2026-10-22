package io.github.dirtyredz.herbalwitch.showcase.layout;

/**
 * The separately built showcases. Each group is planned, checked and built on its own plot, so the
 * furniture never stands among the products and abilities. A section belongs to
 * {@link #WITCHCRAFT} unless it says otherwise ({@link ShowcaseSection#group()}).
 */
public enum ShowcaseGroup {
    /** The decor pieces. */
    FURNITURE("furniture"),
    /** Herbs, stations, products, magic and abilities: everything that is not furniture. */
    WITCHCRAFT("witchcraft");

    private final String id;

    ShowcaseGroup(String id) {
        this.id = id;
    }

    /** The command argument that selects the group. */
    public String id() {
        return id;
    }
}
