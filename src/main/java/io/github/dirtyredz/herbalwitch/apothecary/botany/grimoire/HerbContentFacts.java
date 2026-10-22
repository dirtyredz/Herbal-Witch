package io.github.dirtyredz.herbalwitch.apothecary.botany.grimoire;

import io.github.dirtyredz.herbalwitch.apothecary.botany.conditions.HerbConditionReloadListener;
import io.github.dirtyredz.herbalwitch.apothecary.botany.conditions.HerbConditions;
import io.github.dirtyredz.herbalwitch.grimoire.content.input.HerbSummary;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Converts feature data once per condition-set identity; iteration order is also the wire order. */
public final class HerbContentFacts {
    private static HerbConditions source;
    private static Map<String, HerbSummary> summaries = Map.of();

    private HerbContentFacts() { }

    public static synchronized Map<String, HerbSummary> summaries() {
        HerbConditions next = HerbConditionReloadListener.current();
        if (source != next) {
            Map<String, HerbSummary> built = new LinkedHashMap<>();
            next.all().forEach(c -> built.put(c.herb(), summary(c)));
            summaries = Collections.unmodifiableMap(built);
            source = next;
        }
        return summaries;
    }

    public static HerbSummary summary(HerbConditions.Condition c) {
        return new HerbSummary(c.herb(), c.habitat(), c.discoveryChance(), c.harvestBonusChance(),
                c.preferences().stream().map(p -> switch (p) {
                    case RAIN -> 0;
                    case LOW_SKY -> 1;
                    case NIGHT -> 2;
                    case FULL_MOON -> 3;
                }).toList());
    }
}
