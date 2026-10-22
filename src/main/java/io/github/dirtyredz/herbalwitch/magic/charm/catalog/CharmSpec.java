package io.github.dirtyredz.herbalwitch.magic.charm.catalog;

/**
 * One protective charm, plain data. A charm reduces damage from mobs only (never players, falls or
 * the environment): {@code percent} of each permitted mob hit, at most {@code cap} health. A family
 * amulet ({@code family} = {@code undead} or {@code arthropods}, the repellent family tags) reduces only
 * that family's hits; the sachet ({@code family} empty) any mob's.
 *
 * @param id          id segment: {@code magic/charms/<id>}
 * @param displayName English name
 * @param family      the mob family it guards against, or "" for every mob
 * @param percent     share of a hit it removes
 * @param cap         most health it removes from one hit
 * @param description one-line English tooltip
 */
public record CharmSpec(String id, String displayName, String family, int percent, float cap, String description) {
    public String path() {
        return "magic/charms/" + id;
    }

    public boolean everyMob() {
        return family.isEmpty();
    }
}
