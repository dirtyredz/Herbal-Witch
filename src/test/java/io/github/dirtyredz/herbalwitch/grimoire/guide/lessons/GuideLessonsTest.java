package io.github.dirtyredz.herbalwitch.grimoire.guide.lessons;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The neutral lesson facts load and resolve on the plain test classpath, which holds no Ponder or Flywheel
 * (they are compile-only for main), proving presentation and datagen can use them with the libraries absent.
 */
class GuideLessonsTest {
    @Test
    void firstPreparationIsFoundByIdAndUnknownLessonsAreNot() {
        assertSame(GuideLessons.FIRST_PREPARATION, GuideLessons.find("herbalwitch:first_preparation").orElseThrow());
        assertTrue(GuideLessons.find("herbalwitch:missing").isEmpty());
        assertTrue(GuideLessons.find("first_preparation").isEmpty(), "ids are namespaced");
        var lesson = GuideLessons.FIRST_PREPARATION;
        assertEquals("herbalwitch:apothecary/first_preparation", lesson.schematic());
        assertEquals("assets/herbalwitch/ponder/apothecary/first_preparation.nbt", lesson.schematicAsset());
        assertEquals("herbalwitch:apothecary/lavender.md", lesson.page());
        assertEquals("first_preparation", lesson.scene());
    }

    @Test
    void associationsAreTheFourteenFirstHerbItemsCanonicalFirst() {
        var items = GuideLessons.FIRST_PREPARATION.items();
        assertEquals("herbalwitch:botany/temperate/lavender/fresh", items.get(0));
        assertEquals(Set.of(
                "herbalwitch:botany/temperate/lavender/fresh", "herbalwitch:botany/temperate/lavender/seeds",
                "herbalwitch:preparations/herbs/temperate/lavender/bundle", "herbalwitch:preparations/herbs/temperate/lavender/powder",
                "herbalwitch:botany/shade/valerian/fresh", "herbalwitch:botany/shade/valerian/seeds",
                "herbalwitch:preparations/herbs/shade/valerian/bundle", "herbalwitch:preparations/herbs/shade/valerian/powder",
                "herbalwitch:preparations/drinks/restful_infusion",
                "herbalwitch:stations/drying_rack/station", "herbalwitch:stations/mortar/station",
                "herbalwitch:stations/kettle/station", "herbalwitch:stations/stove/station",
                "herbalwitch:stations/hearth/station"), Set.copyOf(items));
        assertEquals(14, items.size());
    }

    @Test
    void lessonsRejectAMisplacedCanonicalItemOrDuplicates() {
        assertThrows(IllegalArgumentException.class, () -> new GuideLessons.Lesson("a:b", "b", "a:b", "a:b.md", "a:x", List.of("a:y", "a:x")));
        assertThrows(IllegalArgumentException.class, () -> new GuideLessons.Lesson("a:b", "b", "a:b", "a:b.md", "a:x", List.of("a:x", "a:x")));
    }

    @Test
    void captionsAreNamedUniquelyAndKeyedLikePonderSharedText() {
        Set<String> ids = new HashSet<>();
        for (var caption : FirstPreparationText.ALL) {
            assertTrue(ids.add(caption.id()), caption.id());
            assertTrue(caption.id().matches("first_preparation\\.[a-z_]+"), caption.id());
            assertFalse(caption.id().contains("text_"), "named, never positional");
            assertEquals("herbalwitch.ponder.shared." + caption.id(), caption.key());
            assertFalse(caption.english().isBlank());
        }
        assertEquals("herbalwitch.ponder.first_preparation.header", FirstPreparationText.titleKey());
        assertTrue(FirstPreparationText.VALERIAN.english().toLowerCase().contains("valerian"), "valerian is explicit");
        assertTrue(FirstPreparationText.LOAD.english().contains("water bottle"), "the water bottle is explicit");
    }

    @Test
    void layoutKeepsTheKettleOnTheStoveOriginInsideTheScene() {
        assertEquals(FirstPreparationLayout.STOVE_ORIGIN.above(), FirstPreparationLayout.KETTLE);
        assertEquals(FirstPreparationLayout.LAVENDER.below(), FirstPreparationLayout.SOIL);
        for (var pos : List.of(FirstPreparationLayout.LAVENDER, FirstPreparationLayout.SOIL, FirstPreparationLayout.RACK_ORIGIN,
                FirstPreparationLayout.MORTAR, FirstPreparationLayout.STOVE_ORIGIN, FirstPreparationLayout.KETTLE)) {
            assertTrue(FirstPreparationLayout.contains(pos), pos.toString());
        }
    }
}
