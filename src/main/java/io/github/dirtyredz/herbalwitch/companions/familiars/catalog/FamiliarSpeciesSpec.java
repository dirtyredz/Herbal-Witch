package io.github.dirtyredz.herbalwitch.companions.familiars.catalog;

/**
 * Permanent, registry-independent species facts. Crow is an appearance of raven. A stealthy species
 * (the fox) carries the shared concealment effect as a native trait while it is not drawing attention.
 */
public record FamiliarSpeciesSpec(String key, String originalType, String bait, int trust, int feeds,
                                  boolean flying, double speed, float damage, String equipment, boolean stealthy) {
    public String entityId() { return "herbalwitch:familiars/entity/" + key; }
}
