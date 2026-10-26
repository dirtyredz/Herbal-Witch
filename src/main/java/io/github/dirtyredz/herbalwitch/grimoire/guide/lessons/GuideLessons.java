package io.github.dirtyredz.herbalwitch.grimoire.guide.lessons;

import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;

import java.util.List;
import java.util.Optional;

/**
 * The animated lessons' neutral facts: identity, structure, readable page and associated items. Plain strings
 * and catalogue reads only, so guide presentation, datagen and the optional Ponder integration share one
 * source without any optional-library or registry type.
 */
public final class GuideLessons {
    /** The guide's Watch link label; shared by every lesson. */
    public static final String WATCH_LABEL_KEY = "herbalwitch.guide.watch";

    /**
     * One animated lesson.
     *
     * @param id            lesson identity, as written in a guide page's Watch tag
     * @param scene         scene segment of the Ponder title key ({@code herbalwitch.ponder.<scene>.header})
     * @param schematic     structure id; the asset lives at {@code assets/<ns>/ponder/<path>.nbt}
     * @param page          the complete written equivalent
     * @param canonicalItem the component the lesson is routed through (exact Watch opening)
     * @param items         every component the lesson is associated with, canonical first
     */
    public record Lesson(String id, String scene, String schematic, String page, String canonicalItem, List<String> items) {
        public Lesson {
            items = List.copyOf(items);
            if (items.isEmpty() || !items.get(0).equals(canonicalItem)) {
                throw new IllegalArgumentException("the canonical item must be the first associated item: " + id);
            }
            if (items.stream().distinct().count() != items.size()) throw new IllegalArgumentException("duplicate item: " + id);
        }

        /** The packaged structure resource, e.g. {@code assets/herbalwitch/ponder/apothecary/first_preparation.nbt}. */
        public String schematicAsset() {
            int colon = schematic.indexOf(':');
            return "assets/" + schematic.substring(0, colon) + "/ponder/" + schematic.substring(colon + 1) + ".nbt";
        }
    }

    /** Lavender harvest, drying, grinding, heat and the bundled Restful Infusion (with valerian). */
    public static final Lesson FIRST_PREPARATION = new Lesson("herbalwitch:first_preparation", FirstPreparationText.SCENE,
            "herbalwitch:apothecary/first_preparation", "herbalwitch:apothecary/lavender.md",
            item(HerbCatalog.LAVENDER.freshPath()), firstPreparationItems());

    public static final List<Lesson> ALL = List.of(FIRST_PREPARATION);

    private GuideLessons() {
    }

    /** The lesson with this id, if one exists. */
    public static Optional<Lesson> find(String id) {
        return ALL.stream().filter(lesson -> lesson.id().equals(id)).findFirst();
    }

    /** The fourteen items the first-herb guide pages are about, lavender's fresh herb first. */
    private static List<String> firstPreparationItems() {
        return GuideTopics.FIRST_PREPARATION.stream().map(GuideTopics.Topic::item).toList();
    }

    private static String item(String path) {
        return "herbalwitch:" + path;
    }
}
