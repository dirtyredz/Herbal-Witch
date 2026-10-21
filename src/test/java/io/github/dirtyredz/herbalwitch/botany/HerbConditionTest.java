package io.github.dirtyredz.herbalwitch.botany;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbHabitat.Preference;
import io.github.dirtyredz.herbalwitch.botany.conditions.HerbConditions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Condition parsing: validation rejects the whole set; preference bonuses are capped. Pure JUnit. */
class HerbConditionTest {
    private static final Set<String> KNOWN = HerbCatalog.ALL.stream().map(HerbConditions::plantId).collect(Collectors.toSet());

    private static Map<String, JsonElement> defaults() {
        Map<String, JsonElement> files = new HashMap<>();
        HerbConditions.defaults().all().forEach(c -> files.put(c.herb() + ".json", c.toJson()));
        return files;
    }

    @Test
    void theDefaultsRoundTrip() {
        HerbConditions.Result result = HerbConditions.parse(defaults(), KNOWN);
        assertTrue(result.ok(), result.errors().toString());
        HerbCatalog.ALL.forEach(h -> assertEquals(HerbConditions.defaultFor(h), result.conditions().get(h)));
    }

    @Test
    void anyInvalidFileRejectsTheWholeSet() {
        for (String bad : List.of(
                "{\"herb\":\"herbalwitch:botany/dry/basil/plant\",\"habitat\":\"herbalwitch:x\",\"discovery_chance\":0.1,\"harvest_bonus_chance\":0.2}",
                "{\"herb\":\"herbalwitch:botany/dry/thyme/plant\",\"habitat\":\"herbalwitch:x\",\"discovery_chance\":1.5,\"harvest_bonus_chance\":0.2}",
                "{\"herb\":\"herbalwitch:botany/dry/thyme/plant\",\"habitat\":\"herbalwitch:x\",\"discovery_chance\":0.1,\"harvest_bonus_chance\":-1}",
                "{\"herb\":\"herbalwitch:botany/dry/thyme/plant\",\"habitat\":\"herbalwitch:x\",\"discovery_chance\":0.1,\"harvest_bonus_chance\":0.2,\"preferences\":[\"snow\"]}",
                "{\"habitat\":\"herbalwitch:x\",\"discovery_chance\":0.1,\"harvest_bonus_chance\":0.2}")) {
            Map<String, JsonElement> files = defaults();
            files.put("zz_bad.json", JsonParser.parseString(bad));
            HerbConditions.Result result = HerbConditions.parse(files, KNOWN);
            assertFalse(result.ok(), bad);
            assertNull(result.conditions(), bad);
        }
    }

    @Test
    void duplicateHerbsAreRejected() {
        Map<String, JsonElement> files = defaults();
        JsonObject copy = HerbConditions.defaultFor(HerbCatalog.SAGE).toJson();
        files.put("zz_copy.json", copy);
        assertFalse(HerbConditions.parse(files, KNOWN).ok());
    }

    @Test
    void preferencesDoubleDiscoveryUpToAnEighth() {
        HerbConditions.Condition woodland = HerbConditions.defaultFor(HerbCatalog.MANDRAKE);
        assertEquals(List.of(Preference.NIGHT, Preference.FULL_MOON), woodland.preferences());
        assertEquals(1.0 / 64, woodland.discoveryChance(0), 1e-12);
        assertEquals(1.0 / 32, woodland.discoveryChance(1), 1e-12);
        assertEquals(1.0 / 16, woodland.discoveryChance(2), 1e-12);
        HerbConditions.Condition generous = new HerbConditions.Condition("x", "y", 0.1, 0.25, List.of(Preference.RAIN));
        assertEquals(HerbConditions.DISCOVERY_CAP, generous.discoveryChance(1), 1e-12);
    }
}
