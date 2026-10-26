package io.github.dirtyredz.herbalwitch.grimoire.guide.lessons;

import java.util.List;

/**
 * The first-preparation lesson's title and named captions with their English defaults. Language datagen and
 * the Ponder plugin's shared-text registration both read this catalogue, so a caption's key never depends on
 * its position in the storyboard (Ponder's {@code .text(String)} would key it {@code text_<n>}).
 *
 * <p>The lesson illustrates the bundled recipe; captions never quote amounts, durations or yields, and point
 * players to the written guide's live recipe cards for what their world accepts.
 */
public final class FirstPreparationText {
    /** Scene segment of the title key. */
    public static final String SCENE = "first_preparation";
    public static final String TITLE = "First preparation: Restful Infusion";

    /**
     * One shared caption.
     *
     * @param id      Ponder shared-text id, e.g. {@code first_preparation.harvest}
     * @param english the English default
     */
    public record Caption(String id, String english) {
        /** The language key Ponder resolves for this shared text. */
        public String key() {
            return "herbalwitch.ponder.shared." + id;
        }
    }

    public static final Caption INTRO = caption("intro",
            "This lesson shows the bundled Restful Infusion recipe, with the waiting shortened. The written guide lists the ingredients your world currently uses.");
    public static final Caption HARVEST = caption("harvest",
            "Use a mature lavender plant to harvest it by hand. The bush is cut back and gives fresh lavender, sometimes with seeds.");
    public static final Caption DRY = caption("dry",
            "Use the Herb Drying Rack to open its inventory, then put the fresh lavender in the input slot.");
    public static final Caption DRY_WAIT = caption("dry_wait",
            "Drying starts on its own and needs no fuel or heat. When it finishes, take the dried bundle from the output.");
    public static final Caption GRIND = caption("grind",
            "Open the Mortar and Pestle and put the dried bundle in its input slot. Grinding starts on its own; take the powder from the output.");
    public static final Caption VALERIAN = caption("valerian",
            "Valerian is dried and ground the same way. The infusion needs both powders.");
    public static final Caption HEAT = caption("heat",
            "The kettle needs a working heat source directly below it. Open the Witch's Stove and add furnace fuel; it lights and burns on its own.");
    public static final Caption LOAD = caption("load",
            "Open the kettle. Put the lavender and valerian powders in the ingredient slots and a water bottle in the base-liquid slot.");
    public static final Caption CONTAINER = caption("container",
            "The water bottle becomes the drink's container, so this recipe needs no separate packaging.");
    public static final Caption BREW = caption("brew",
            "Brewing starts automatically once the inputs and heat match. No initiation or Start button is needed.");
    public static final Caption DRINK = caption("drink",
            "Hold the Restful Infusion and use it until you finish drinking. It resets your time since rest, and you keep the empty bottle.");
    public static final Caption REPLAY = caption("replay",
            "Replay this lesson whenever you like. The live recipe cards in the written guide show what your world accepts.");

    /** Every caption, in storyboard order. */
    public static final List<Caption> ALL = List.of(INTRO, HARVEST, DRY, DRY_WAIT, GRIND, VALERIAN, HEAT, LOAD, CONTAINER,
            BREW, DRINK, REPLAY);

    private FirstPreparationText() {
    }

    /** The scene title's language key. */
    public static String titleKey() {
        return "herbalwitch.ponder." + SCENE + ".header";
    }

    private static Caption caption(String name, String english) {
        return new Caption(SCENE + "." + name, english);
    }
}
