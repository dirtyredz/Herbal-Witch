package io.github.dirtyredz.herbalwitch.companions.animals.trust;

/** Ordinary care uses loaded animal ticks, never world time or an offline catch-up. */
public final class TrustRules {
    public static final int MAX_TRUST = 100;
    public static final int FEED_GAIN = 20;
    public static final int FEED_INTERVAL = 1200;
    public static final int HURT_LOSS = 40;
    public static final int HURT_LOCKOUT = 600;
    public static final int HEAL_INTERVAL = 200;
    public static final int CALM_TICKS = 200;
    public static final int CALM_LOCKOUT = 100;
    public static final int DEFAULT_CAPACITY = 8;
    public static final int MAX_CAPACITY = 16;

    private TrustRules() {}

    public static boolean earned(AnimalRelationshipView relationship, boolean nativeTaming) {
        return relationship.trust() >= (nativeTaming ? 100 : 80)
                && relationship.feeds() >= (nativeTaming ? 5 : 4);
    }
}
