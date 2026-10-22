package io.github.dirtyredz.herbalwitch.grimoire.content.input;

import java.util.List;

/** Fixed protocol-4 facts. Preference codes: 0 rain, 1 low sky, 2 night, 3 full moon. */
public record HerbSummary(String herb, String habitat, double discoveryChance, double harvestBonusChance,
                          List<Integer> preferences) {
    public static final int PREFERENCE_COUNT = 4;

    public HerbSummary {
        preferences = List.copyOf(preferences);
    }
}
