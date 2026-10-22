package io.github.dirtyredz.herbalwitch.companions.animals.trust;

/** One viewer's immutable relationship. Reading it never creates a saved entry. */
public record AnimalRelationshipView(int trust, int feeds, long nextFeed) {
    public static final AnimalRelationshipView EMPTY = new AnimalRelationshipView(0, 0, 0);

    public AnimalRelationshipView {
        if (trust < 0 || trust > TrustRules.MAX_TRUST || feeds < 0 || feeds > 1_000_000 || nextFeed < 0) {
            throw new IllegalArgumentException("Invalid animal relationship");
        }
    }

    public long remaining(long clock) {
        return Math.max(0, nextFeed - clock);
    }
}
