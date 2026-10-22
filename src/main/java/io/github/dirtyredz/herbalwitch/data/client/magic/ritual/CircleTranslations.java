package io.github.dirtyredz.herbalwitch.data.client.magic.ritual;

import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.CircleCatalog;
import io.github.dirtyredz.herbalwitch.magic.ritual.circle.RitualCircleBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.grimoire.AltarEntries;
import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualBlocks;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessionService;
import io.github.dirtyredz.herbalwitch.magic.ritual.showcase.CircleShowcaseSection;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The ritual circle's English (Milestone 7): its name, the circle rituals' notices and screen lines,
 * their grimoire pages (numbers are arguments), the ritual cards of the recipe viewers and the showcase
 * signs. Kept apart from the altar's own English, which it extends.
 */
public final class CircleTranslations {
    private static final String CARD = "category.herbalwitch.ritual.";
    /** Ward refusals a home protection may report ({@code ritual.herbalwitch.home_protection.<reason>}). */
    public static final List<String> WARD_REFUSALS = List.of("no_stone", "unsupported", "already_charged", "not_owner", "not_witch",
            "owner_limit", "world_limit", "dimension_limit", "chunk_limit", "overlap", "protected");

    private CircleTranslations() {
    }

    public static void addTo(LanguageProvider lang) {
        lang.add(RitualBlocks.CIRCLE.get(), CircleCatalog.NAME);
        lang.add(RitualCircleBlock.NO_ALTAR, "Place the Witch's Altar inside the circle, facing the same way.");
        notices().forEach((suffix, text) -> lang.add(AltarSessionService.KEY + suffix, text));
        ui().forEach((suffix, text) -> lang.add(AltarSessionService.KEY + "ui." + suffix, text));
        pages().forEach((suffix, text) -> lang.add(AltarEntries.PREFIX + suffix, text));
        cards().forEach(lang::add);
        lang.add(CircleShowcaseSection.TITLE, CircleCatalog.NAME);
        Map.of("arrangement", new String[] {"The circle", "Altar inside it"},
                "garden_blessing", new String[] {"Garden blessing", "Enriches herb beds"},
                "home_protection", new String[] {"Home protection", "Charges a ward"})
                .forEach((topic, text) -> {
                    lang.add(CircleShowcaseSection.label(topic), text[0]);
                    lang.add(CircleShowcaseSection.label(topic) + ".detail", text[1]);
                });
        lang.add(CircleShowcaseSection.label("supplies"), "Offerings");
    }

    /** Results and interruptions ({@code ritual.herbalwitch.<suffix>}). */
    public static Map<String, String> notices() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("circle.incomplete", "The ritual circle around this altar is not complete.");
        m.put("circle.unloaded", "Part of the circle is too far away to be felt.");
        m.put("garden_blessing.not_witch", "Only a witch can bless a garden.");
        m.put("garden_blessing.no_bed", "Look at an herb bed near the altar first.");
        m.put("garden_blessing.unloaded", "Part of that garden is too far away.");
        m.put("garden_blessing.not_allowed", "You may not change some of those beds.");
        m.put("garden_blessing.nothing", "Every bed there is already enriched.");
        m.put("garden_blessing.begun", "The garden blessing begins. Stay at the altar.");
        m.put("garden_blessing.done", "The garden is blessed: its beds will give more.");
        m.put("home_protection.not_witch", "Only a witch can ward a home.");
        m.put("home_protection.no_ward", "Look at a ward stone near the altar first.");
        Map<String, String> refusals = new LinkedHashMap<>();
        refusals.put("no_stone", "that is not a ward stone");
        refusals.put("unsupported", "the ward records cannot be read");
        refusals.put("already_charged", "that ward is already charged");
        refusals.put("not_owner", "that ward is bound to another witch");
        refusals.put("not_witch", "only a witch can ward a home");
        refusals.put("owner_limit", "you hold as many wards as you may");
        refusals.put("world_limit", "this world holds as many wards as it may");
        refusals.put("dimension_limit", "this world already has as many active wards as it may");
        refusals.put("chunk_limit", "too many wards stand close together");
        refusals.put("overlap", "it would overlap another active ward");
        refusals.put("protected", "the land is protected here");
        for (String reason : WARD_REFUSALS) {
            m.put("home_protection." + reason, "Home protection: " + refusals.get(reason) + ".");
        }
        m.put("home_protection.begun", "The home protection begins. Stay at the altar.");
        m.put("home_protection.done", "The ward is charged.");
        Map<String, String> cancel = new LinkedHashMap<>();
        cancel.put("circle_broken", "the circle was broken");
        cancel.put("garden_changed", "the garden changed");
        cancel.put("ward_changed", "the ward changed");
        cancel.forEach((k, v) -> m.put("cancel." + k, "Ritual interrupted: " + v + ". Nothing was used."));
        return m;
    }

    /** The altar screen's circle lines ({@code ritual.herbalwitch.ui.<suffix>}). */
    public static Map<String, String> ui() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("view_circle", "Circle rituals");
        m.put("view_initiation", "Initiation");
        m.put("circle_state", "Circle: %s cells missing, %s too far away.");
        m.put("circle_complete", "The circle is complete.");
        m.put("bless_intro", "Garden blessing: %s energy and:");
        m.put("bless_target", "Bed at %s %s %s");
        m.put("bless_no_target", "Look at an herb bed first.");
        m.put("bless", "Bless");
        m.put("protect_intro", "Home protection: %s energy and:");
        m.put("protect_target", "Ward at %s %s %s");
        m.put("protect_no_target", "Look at a ward stone first.");
        m.put("protect", "Protect");
        return m;
    }

    /** The circle's grimoire pages ({@code grimoire.herbalwitch.ritual.<suffix>}). */
    public static Map<String, String> pages() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("circle.0", "A ring of engraved stone laid around the Witch's Altar, for the rituals that reach beyond it.");
        m.put("circle.size", "Five blocks square, one layer, hollow in the middle; every part needs a sturdy floor.");
        m.put("circle.altar", "The altar stands inside it, one block in from the circle's left and two back, facing the same way. "
                + "Using the circle opens that altar.");
        m.put("circle.crafted", "Crafted from four smooth stone slabs, two calcite, an amethyst shard and a gold nugget.");
        m.put("circle.needed", "It needs a complete circle around the altar; a part out of reach pauses it, a broken part ends it.");
        m.put("garden_blessing.title", "Garden Blessing");
        m.put("garden_blessing.0", "Look at an herb bed, then bless it at the altar: the beds around it are enriched, each giving one "
                + "more fresh herb at its next harvest.");
        m.put("garden_blessing.terms", "It costs %s energy and takes %s seconds. The soil tonic's bottle comes back.");
        m.put("garden_blessing.area", "It reaches a square %s beds wide at that bed's height; the bed must lie within %s blocks of the "
                + "altar. Enriched beds stay as they are.");
        m.put("home_protection.title", "Home Protection");
        m.put("home_protection.0", "Look at an empty ward stone, then charge it at the altar, just as touching it would.");
        m.put("home_protection.terms", "It costs %s energy and takes %s seconds, and the stone itself asks nothing more.");
        m.put("home_protection.ward", "The stone must lie within %s blocks of the altar and be unbound or yours.");
        return m;
    }

    /** The recipe viewers' ritual cards. */
    public static Map<String, String> cards() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("category.herbalwitch.ritual", "Rituals");
        m.put("emi.category.herbalwitch.ritual", "Rituals");
        m.put(CARD + "needs_altar", "At the Witch's Altar");
        m.put(CARD + "needs_circle", "At an altar in a ritual circle");
        m.put(CARD + "time", "%s s");
        m.put(CARD + "terms", "%s s, %s energy");
        m.put(CARD + "open", "Until full or interrupted");
        Map<AltarActionSpec, String> results = Map.of(
                AltarActionSpec.FOUNDING, "Becomes the first witch",
                AltarActionSpec.INITIATION, "Makes a consenting witch",
                AltarActionSpec.MEDITATION, "Restores energy",
                AltarActionSpec.ATTUNEMENT, "Practice towards capacity",
                AltarActionSpec.GARDEN_BLESSING, "Enriches herb beds",
                AltarActionSpec.HOME_PROTECTION, "Charges a ward stone");
        for (AltarActionSpec action : AltarActionSpec.values()) {
            m.put(CARD + "result." + action.key(), results.get(action));
        }
        return m;
    }
}
