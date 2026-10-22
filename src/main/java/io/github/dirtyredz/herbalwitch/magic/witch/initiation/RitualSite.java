package io.github.dirtyredz.herbalwitch.magic.witch.initiation;

/**
 * Where a ritual stands, dimension-qualified: the dimension id and the altar's origin position
 * (packed as {@code BlockPos.asLong}). Plain Java, so consent and founding records are unit-tested
 * and saved without Minecraft types.
 *
 * @param dimension dimension id, {@code namespace:path}
 * @param origin    the altar origin, packed
 */
public record RitualSite(String dimension, long origin) {
    public RitualSite {
        if (dimension == null || dimension.isEmpty() || dimension.length() > 256) {
            throw new IllegalArgumentException("bad dimension " + dimension);
        }
    }
}
