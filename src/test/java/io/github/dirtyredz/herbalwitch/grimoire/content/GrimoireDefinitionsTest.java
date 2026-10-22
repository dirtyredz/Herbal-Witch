package io.github.dirtyredz.herbalwitch.grimoire.content;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.github.dirtyredz.herbalwitch.grimoire.content.reload.GrimoireDefinition;
import io.github.dirtyredz.herbalwitch.grimoire.content.reload.GrimoireDefinitionLoader;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Editorial definitions: strict validation (never recipe mechanics), whole-set acceptance or
 * rejection, guide references checked within the set, and recipes needing no definition at all.
 */
class GrimoireDefinitionsTest {
    private static JsonElement json(String text) {
        return JsonParser.parseString(text);
    }

    private static final String GUIDE = "{\"entry\":\"guide|herbalwitch:heat\",\"title\":\"Heat\",\"pages\":[\"Warm things.\"],"
            + "\"links\":[\"station|herbalwitch:stations/hearth/station\"]}";
    private static final String RECIPE_NOTE = "{\"entry\":\"recipe|herbalwitch:processing/steeping/recovery_tea\","
            + "\"pages\":[\"Soothing.\"],\"links\":[\"guide|herbalwitch:heat\"],\"hint\":\"Find chamomile.\"}";

    @Test
    void aValidDefinitionParsesAndRoundTrips() {
        GrimoireDefinition d = GrimoireDefinition.parse(json(RECIPE_NOTE));
        assertEquals(EntryKey.Kind.RECIPE, d.key().kind());
        assertEquals(List.of("Soothing."), d.pages());
        assertEquals(d, GrimoireDefinition.parse(d.toJson()));
    }

    @Test
    void mechanicsUnknownFieldsAndBadValuesAreRejected() {
        for (String bad : List.of(
                "{\"entry\":\"recipe|herbalwitch:x\",\"duration\":20}",
                "{\"entry\":\"recipe|herbalwitch:x\",\"ingredients\":[]}",
                "{\"entry\":\"recipe|herbalwitch:x\",\"result\":{\"item\":\"minecraft:dirt\"}}",
                "{\"entry\":\"recipe|herbalwitch:x\",\"heat\":true}",
                "{\"entry\":\"recipe|herbalwitch:x\",\"colour\":\"red\"}",
                "{\"entry\":\"recipe|herbalwitch:x\",\"category\":\"Not Valid\"}",
                "{\"entry\":\"spell|herbalwitch:x\"}",
                "{\"entry\":\"guide|herbalwitch:untitled\"}",
                "{\"entry\":\"herb|herbalwitch:x\",\"links\":[\"herb|herbalwitch:x\"]}",
                "{\"entry\":\"herb|herbalwitch:x\",\"pages\":\"one\"}",
                "{\"entry\":\"herb|herbalwitch:x\",\"pages\":[\"\"]}",
                "[1, 2]")) {
            assertThrows(IllegalArgumentException.class, () -> GrimoireDefinition.parse(json(bad)), bad);
        }
        StringBuilder many = new StringBuilder("{\"entry\":\"herb|herbalwitch:x\",\"pages\":[");
        for (int i = 0; i <= GrimoireDefinition.MAX_PAGES; i++) {
            many.append(i == 0 ? "" : ",").append("\"p").append(i).append('"');
        }
        assertThrows(IllegalArgumentException.class, () -> GrimoireDefinition.parse(json(many.append("]}").toString())));
    }

    @Test
    void aBrokenGuideReferenceOrADuplicateRejectsTheWholeSet() {
        Map<String, JsonElement> broken = new LinkedHashMap<>();
        broken.put("a.json", json(RECIPE_NOTE)); // links to guide|herbalwitch:heat, which this set lacks
        GrimoireDefinition.Result result = GrimoireDefinition.parseAll(broken);
        assertFalse(result.ok());
        assertTrue(result.errors().get(0).contains("broken reference"), result.errors().toString());
        assertTrue(result.definitions().isEmpty(), "nothing partial survives");

        Map<String, JsonElement> twice = new LinkedHashMap<>();
        twice.put("a.json", json(GUIDE));
        twice.put("b.json", json(GUIDE));
        assertFalse(GrimoireDefinition.parseAll(twice).ok());
    }

    @Test
    void linksToRecipesHerbsAndStationsAreCheckedLaterAgainstLiveContent() {
        Map<String, JsonElement> files = new LinkedHashMap<>();
        files.put("guide.json", json(GUIDE)); // links a station that only the live content can confirm
        files.put("tea.json", json(RECIPE_NOTE));
        files.put("other.json", json("{\"entry\":\"recipe|othermod:processing/drying/thing\",\"pages\":[\"From another mod.\"]}"));
        GrimoireDefinition.Result result = GrimoireDefinition.parseAll(files);
        assertTrue(result.ok(), result.errors().toString());
        assertEquals(3, result.definitions().size());
    }

    @Test
    void anInvalidReloadKeepsThePreviousSetAndAValidOneReplacesItAtomically() {
        Map<String, JsonElement> good = new LinkedHashMap<>();
        good.put("guide.json", json(GUIDE));
        assertTrue(GrimoireDefinitionLoader.replace(good).isEmpty());
        Map<EntryKey, GrimoireDefinition> before = GrimoireDefinitionLoader.current();
        assertEquals(1, before.size());

        Map<String, JsonElement> bad = new LinkedHashMap<>(good);
        bad.put("tea.json", json(RECIPE_NOTE));
        bad.put("broken.json", json("{\"entry\":\"recipe|herbalwitch:y\",\"duration\":5}"));
        assertFalse(GrimoireDefinitionLoader.replace(bad).isEmpty());
        assertEquals(before, GrimoireDefinitionLoader.current(), "the previous editorial set stays in force");

        good.put("tea.json", json(RECIPE_NOTE));
        assertTrue(GrimoireDefinitionLoader.replace(good).isEmpty());
        assertEquals(2, GrimoireDefinitionLoader.current().size());
        assertTrue(GrimoireDefinitionLoader.replace(Map.of()).isEmpty(), "an empty set is valid: every page falls back");
        assertTrue(GrimoireDefinitionLoader.current().isEmpty());
    }
}
