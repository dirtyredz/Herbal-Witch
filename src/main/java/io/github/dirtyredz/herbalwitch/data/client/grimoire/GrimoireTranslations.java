package io.github.dirtyredz.herbalwitch.data.client.grimoire;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbHabitat;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.data.server.grimoire.GrimoireContentProvider;
import io.github.dirtyredz.herbalwitch.grimoire.access.item.GrimoireItem;
import io.github.dirtyredz.herbalwitch.grimoire.access.menu.GrimoireAccess;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.GrimoireCatalog;
import io.github.dirtyredz.herbalwitch.grimoire.content.recipe.StationRecipeEntries;
import io.github.dirtyredz.herbalwitch.grimoire.content.reference.HerbEntries;
import io.github.dirtyredz.herbalwitch.grimoire.content.reference.StationEntries;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.research.ResearchService;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.teaching.TeachingService;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireBlocks;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import io.github.dirtyredz.herbalwitch.grimoire.showcase.GrimoireShowcaseSection;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.Map;

/**
 * The grimoire's English: the book and research blocks, the reader's interface, research and
 * teaching messages, herb and station page lines, the editorial guides and the showcase signs.
 */
public final class GrimoireTranslations {
    private static final String UI = "grimoire." + HerbalWitch.MOD_ID + ".ui.";

    private GrimoireTranslations() {
    }

    public static void addTo(LanguageProvider lang) {
        lang.add(GrimoireItems.BOOK.get(), GrimoireCatalog.BOOK_NAME);
        lang.add(GrimoireItem.TOOLTIP, "Your own herbal knowledge: use it to read");
        for (GrimoireCatalog.ResearchPiece piece : GrimoireCatalog.RESEARCH_PIECES) {
            lang.add(GrimoireBlocks.block(piece), piece.displayName());
        }
        lang.add(GrimoireAccess.TITLE, GrimoireCatalog.BOOK_NAME);
        interfaceText(lang);
        messages(lang);
        pageLines(lang);
        GrimoireContentProvider.english().forEach(lang::add);
        lang.add(GrimoireShowcaseSection.TITLE, "The Grimoire");
        lang.add(GrimoireShowcaseSection.SUPPLIES, "Supplies");
        Map.of("discovery", new String[] {"Discovery", "Carry herbs"}, "research", new String[] {"Research", "At stand or shelf"},
                "missing", new String[] {"Missing counts", "Read a recipe"}, "teaching", new String[] {"Teaching", "Offer a friend"})
                .forEach((topic, text) -> {
                    lang.add(GrimoireShowcaseSection.label(topic), text[0]);
                    lang.add(GrimoireShowcaseSection.label(topic) + ".detail", text[1]);
                });
    }

    private static void interfaceText(LanguageProvider lang) {
        Map<String, String> ui = Map.ofEntries(
                Map.entry("search", "Search"), Map.entry("category.all", "All pages"), Map.entry("category.guide", "Guides"),
                Map.entry("category.stations", "Stations"), Map.entry("category.herbs", "Herbs"), Map.entry("category.furniture", "Furnishings"),
                Map.entry("category.witchcraft", "Witchcraft"),
                Map.entry("filter.all", "Everything"), Map.entry("filter.known", "Known"), Map.entry("filter.research", "Researchable"),
                Map.entry("empty", "Nothing here yet."), Map.entry("choose", "Choose a page from the index."),
                Map.entry("unknown", "You have not discovered this yet."), Map.entry("station", "Station: %s"),
                Map.entry("role.ingredient", "Ingredient"), Map.entry("role.base", "Base"), Map.entry("role.packaging", "Container"),
                Map.entry("alternatives", "any of %s"), Map.entry("have", "%s/%s"), Map.entry("missing", "%s/%s"),
                Map.entry("heat", "needs heat"), Map.entry("returns", "The base's container comes back."),
                Map.entry("scope", "Counted from your inventory and offhand only."), Map.entry("counting", "Counting what you carry..."),
                Map.entry("ready", "Ingredients available (slots, heat and room not checked)."),
                Map.entry("open_for_counts", "Open your grimoire to count what you carry."),
                Map.entry("see_also", "See also"), Map.entry("teach", "Teach this recipe"), Map.entry("offered", "Offered to %s: %s"),
                Map.entry("withdraw", "Withdraw"), Map.entry("nobody", "No one is close enough to teach."), Map.entry("invite", "Offer"),
                Map.entry("invited", "%s offers to teach you %s"), Map.entry("accept", "Accept"), Map.entry("decline", "Decline"),
                Map.entry("back", "Back"), Map.entry("research", "Research"), Map.entry("open", "Grimoire"),
                Map.entry("open.tooltip", "Open your grimoire beside this station"));
        ui.forEach((k, v) -> lang.add(UI + k, v));
    }

    private static void messages(LanguageProvider lang) {
        Map<ResearchService.Result, String> research = Map.of(
                ResearchService.Result.RESEARCHED, "You worked out %s",
                ResearchService.Result.ALREADY_KNOWN, "You already know %s",
                ResearchService.Result.NOT_A_RECIPE, "%s cannot be researched",
                ResearchService.Result.NO_FAMILIAR_INGREDIENT, "Nothing you carry or know goes into %s",
                ResearchService.Result.NEEDS_RESEARCH_BLOCK, "Research needs a grimoire stand or a witch's bookshelf");
        research.forEach((r, v) -> lang.add(r.messageKey(), v));
        Map<TeachingService.Result, String> teaching = Map.ofEntries(
                Map.entry(TeachingService.Result.SENT, "You offered to teach %s"),
                Map.entry(TeachingService.Result.ACCEPTED, "%s was learned"),
                Map.entry(TeachingService.Result.DECLINED, "The offer of %s was declined"),
                Map.entry(TeachingService.Result.CANCELLED, "The offer of %s was withdrawn"),
                Map.entry(TeachingService.Result.SELF, "You cannot teach yourself"),
                Map.entry(TeachingService.Result.NO_READER, "Teaching needs an open grimoire at a stand or bookshelf"),
                Map.entry(TeachingService.Result.OUT_OF_RANGE, "Too far apart to teach %s"),
                Map.entry(TeachingService.Result.NOT_LIVE_RECIPE, "%s cannot be taught"),
                Map.entry(TeachingService.Result.NOT_KNOWN, "You do not know %s"),
                Map.entry(TeachingService.Result.ALREADY_KNOWN, "They already know %s"),
                Map.entry(TeachingService.Result.TEACHER_BUSY, "You already have an offer waiting"),
                Map.entry(TeachingService.Result.RECIPIENT_BUSY, "They are already considering another offer"),
                Map.entry(TeachingService.Result.COOLDOWN, "Wait a moment before offering again"),
                Map.entry(TeachingService.Result.NO_INVITATION, "There is no such offer"),
                Map.entry(TeachingService.Result.EXPIRED, "The offer of %s ended"),
                Map.entry(TeachingService.Result.TEACHER_LEFT, "The teacher closed their book"),
                Map.entry(TeachingService.Result.RECIPE_CHANGED, "%s changed, so the offer ended"),
                Map.entry(TeachingService.Result.NOT_RECORDED, "%s could not be recorded"));
        teaching.forEach((r, v) -> lang.add(r.messageKey(), v));
        lang.add("grimoire." + HerbalWitch.MOD_ID + ".teaching.invited", "%s offers to teach you %s: open your grimoire to accept or decline");
        lang.add(StationRecipeEntries.HINT, "Carry all of its ingredients to work it out, or study one of them at a grimoire stand.");
    }

    private static void pageLines(LanguageProvider lang) {
        for (HerbSpec herb : HerbCatalog.ALL) {
            lang.add(HerbEntries.titleKey(herb), herb.displayName());
        }
        lang.add(HerbEntries.FORM_CROP, "A crop: harvesting by hand replants it young.");
        lang.add(HerbEntries.FORM_BUSH, "A bush: harvesting by hand leaves it standing.");
        lang.add(HerbEntries.DISCOVERY, "About 1 in %s grass breaks in its habitat turns one up.");
        lang.add(HerbEntries.DISCOVERY_NONE, "It is not found in grass.");
        lang.add(HerbEntries.LIKES, "Likes: %s (each doubles its chance, and favours a harvest).");
        lang.add(HerbEntries.BONUS, "A favoured harvest has a %s%% chance of one extra herb.");
        lang.add(HerbEntries.NO_CONDITIONS, "This world gives no growing notes for it.");
        lang.add(HerbEntries.preferenceKey(HerbHabitat.Preference.RAIN), "rain");
        lang.add(HerbEntries.preferenceKey(HerbHabitat.Preference.LOW_SKY), "shade");
        lang.add(HerbEntries.preferenceKey(HerbHabitat.Preference.NIGHT), "night");
        lang.add(HerbEntries.preferenceKey(HerbHabitat.Preference.FULL_MOON), "the full moon");
        lang.add(StationEntries.RUNS, "Runs %s recipes.");
        lang.add(StationEntries.NEEDS_HEAT, "Needs heat from directly below.");
        lang.add(StationEntries.NO_HEAT, "Needs no heat.");
        lang.add(StationEntries.HEAT_SOURCE, "A heat source: while lit, it heats the vessel standing on it.");
        lang.add(StationEntries.MULTIBLOCK, "Takes %s blocks of space.");
    }
}
