package io.github.dirtyredz.herbalwitch.decor.catalog;

/**
 * What a piece does when used, independent of its size and mounting. Each behaviour has one block
 * implementation (decor/registry/DecorBlockFactory); a piece composes one behaviour with its
 * {@link MountSpec}.
 */
public enum FurnitureBehavior {
    /** Purely decorative: facing, shape, support and the shared structure lifecycle. */
    STATIC,
    /** Stationary seats (decor/seating): {@link FurnitureSpec#seats()} riders. */
    SEATING,
    /** A closed cabinet with one origin inventory (decor/storage): {@link FurnitureSpec#slots()} slots, automation allowed. */
    STORAGE,
    /** An open shelf that shows what it holds, one item per slot, manual only. */
    DISPLAY,
    /** Toggled on and off by hand; light 14 while lit; no fuel or heat. */
    LIGHT,
    /** Vanilla-compatible sleeping and respawn (decor/sleeping): one sleeper per bed. */
    BED,
    /** Vanilla-compatible door: two halves, hinge, open and powered states. */
    DOOR,
    /** Carpet-thin floor covering; the small rug is vanilla carpet. */
    RUG
}
