package io.github.dirtyredz.herbalwitch.companions.familiars.catalog;

/** Permanent, registry-independent species facts. Crow is an appearance of raven. */
public record FamiliarSpeciesSpec(String key, String originalType, String bait, int trust, int feeds,
                                  boolean flying, double speed, float damage, String equipment) {
    public String entityId() { return "herbalwitch:familiars/entity/" + key; }
}
