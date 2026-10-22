package io.github.dirtyredz.herbalwitch.data.magic.witch;

import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.generated;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Witch status and energy's generated data: the nature biome tag is the union of the four habitat
 * tags; the witchcraft and altar pages take every mechanical number as an argument (configurable
 * values are never written into prose); the drink tooltips name no configurable amount.
 */
class WitchResourcesTest {
    /** A digit in page prose means a hard-coded mechanical value. */
    private static final Pattern DIGIT = Pattern.compile("\\d");

    @Test
    void natureIsTheUnionOfTheFourHabitats() {
        JsonObject tag = generated(WitchManifest.NATURE_TAG);
        List<String> values = new ArrayList<>();
        tag.getAsJsonArray("values").forEach(v -> values.add(v.getAsString()));
        assertEquals(WitchManifest.NATURE_HABITATS.stream().map(h -> "#herbalwitch:botany/habitat/" + h).toList(), values);
    }

    @Test
    void pagesQuoteMechanicsAsArgumentsNeverAsProse() {
        JsonObject lang = ResourceFiles.json(ResourceFiles.GENERATED_ASSETS.resolve("lang/en_us.json"));
        int checked = 0;
        for (String key : lang.keySet()) {
            boolean page = key.startsWith("grimoire.herbalwitch.witchcraft.") || key.startsWith("grimoire.herbalwitch.ritual.");
            if (!page) {
                continue;
            }
            String text = lang.get(key).getAsString();
            assertFalse(DIGIT.matcher(text).find(), key + " hard-codes a number: " + text);
            checked++;
        }
        assertTrue(checked > 30, "pages found: " + checked);
        for (String key : List.of("grimoire.herbalwitch.witchcraft.energy.capacity", "grimoire.herbalwitch.witchcraft.recovery.tea",
                "grimoire.herbalwitch.ritual.initiation.terms", "grimoire.herbalwitch.ritual.attunement.terms")) {
            assertTrue(lang.get(key).getAsString().contains("%s"), key + " takes its numbers as arguments");
        }
    }

    @Test
    void drinkTooltipsNameNoConfigurableAmount() {
        JsonObject lang = ResourceFiles.json(ResourceFiles.GENERATED_ASSETS.resolve("lang/en_us.json"));
        for (String drink : List.of("energy_tea", "restful_infusion")) {
            String text = lang.get("item.herbalwitch.preparations.drinks." + drink + ".tooltip").getAsString();
            assertFalse(DIGIT.matcher(text).find(), drink + ": " + text);
            assertTrue(text.contains("witch"), drink + " mentions what it does for witches: " + text);
        }
    }
}
