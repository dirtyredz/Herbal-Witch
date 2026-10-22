package io.github.dirtyredz.herbalwitch.data.server.grimoire;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.reference.GuideEntries;
import io.github.dirtyredz.herbalwitch.grimoire.content.reference.HerbForms;
import io.github.dirtyredz.herbalwitch.grimoire.content.reference.StationEntries;
import io.github.dirtyredz.herbalwitch.grimoire.content.reload.GrimoireDefinition;
import io.github.dirtyredz.herbalwitch.grimoire.content.reload.GrimoireDefinitionLoader;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationSpec;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The mod's editorial grimoire content, written to {@code data/herbalwitch/grimoire_entries/}:
 * the public guides ({@code guide/<name>.json}) and a paragraph per station
 * ({@code stations/<station>.json}). Text is stored as translation keys; their English lives here
 * too and {@code GrimoireTranslations} writes it into the language file. Recipe mechanics never
 * appear here: recipe pages read the live recipes.
 */
public class GrimoireContentProvider implements DataProvider {
    /** One editorial page: its key, title (guides), English paragraphs and links. */
    public record Page(String file, EntryKey key, Optional<String> title, List<String> english, List<EntryKey> links) {
        public String titleKey() {
            return base() + ".title";
        }

        public String pageKey(int i) {
            return base() + "." + i;
        }

        private String base() {
            return "grimoire." + HerbalWitch.MOD_ID + "." + file.replace('/', '.');
        }

        public GrimoireDefinition definition() {
            List<String> keys = new ArrayList<>();
            for (int i = 0; i < english.size(); i++) {
                keys.add(pageKey(i));
            }
            return new GrimoireDefinition(key, Optional.empty(), title.map(t -> titleKey()), keys, links, Optional.empty());
        }
    }

    private final PackOutput output;

    public GrimoireContentProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public String getName() {
        return "Herbal Witch grimoire entries";
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        PackOutput.PathProvider paths = output.createPathProvider(PackOutput.Target.DATA_PACK, GrimoireDefinitionLoader.DIRECTORY);
        List<CompletableFuture<?>> writes = new ArrayList<>();
        for (Page page : pages()) {
            writes.add(DataProvider.saveStable(cache, page.definition().toJson(),
                    paths.json(ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, page.file()))));
        }
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    private static Page guide(String name, String title, List<EntryKey> links, String... english) {
        return new Page("guide/" + name, GuideEntries.key(name), Optional.of(title), List.of(english), links);
    }

    private static Page station(StationSpec station, String... english) {
        return new Page("stations/" + station.directory(), StationEntries.key(station), Optional.empty(), List.of(english), List.of());
    }

    /** Every editorial page, guides in reading order then stations; English keyed for the language file. */
    public static List<Page> pages() {
        List<Page> pages = new ArrayList<>();
        pages.add(guide("getting_started", "Getting Started", List.of(GuideEntries.key("acquisition"), GuideEntries.key("preparation"),
                        HerbForms.key(HerbCatalog.LAVENDER), HerbForms.key(HerbCatalog.CHAMOMILE)),
                "This grimoire records what you have learned. Any grimoire opens your own knowledge, so giving one away or crafting with it shares or loses nothing.",
                "Guides like this one are open to everyone. Herb and recipe pages fill in as you discover them, and nothing here stops you crafting, brewing or automating anything.",
                "Begin with a starter herb: lavender, sage, mint and chamomile turn up when you break tall grass anywhere."));
        pages.add(guide("acquisition", "Finding Herbs", List.of(GuideEntries.key("preparation")),
                "Break grass and ferns by hand to forage. Starter herbs turn up anywhere, other herbs only in their own habitats; each herb page says where and how often.",
                "Wild patches grow in their habitats as well. Plant seeds on farmland or a herb bed; a mature plant gives two fresh herbs and a seed when you use it.",
                "Carrying any form of a herb (seeds, fresh, dried or powdered) records it here."));
        pages.add(guide("preparation", "The Preparation Chain", List.of(StationEntries.key(StationCatalog.DRYING_RACK),
                        StationEntries.key(StationCatalog.MORTAR), StationEntries.key(StationCatalog.KETTLE),
                        StationEntries.key(StationCatalog.INFUSION_POT)),
                "Fresh herbs dry on a drying rack into bundles. A mortar grinds bundles into powder. Powders steep with a water bottle in a kettle, or go into an infusion pot with a base and a container.",
                "Each recipe page lists exactly what its station needs, how long it takes and whether it needs heat, and links to the recipes that make its ingredients.",
                "When every ingredient of a recipe is familiar to you, the recipe records itself."));
        pages.add(guide("stations", "Stations", List.of(GuideEntries.key("heat")),
                "A station keeps its ingredients in its own slots, reserved while it works, and fills its output when it is done. Closing its screen leaves it working.",
                "The Grimoire button on a station's screen opens this book beside it, on that station's recipes."));
        pages.add(guide("heat", "Heat", List.of(StationEntries.key(StationCatalog.HEARTH), StationEntries.key(StationCatalog.STOVE)),
                "The kettle, the infusion pot, the cauldron and the still need heat from directly below: a lit hearth or stove, a lit campfire or magma. One hopper may stand between them and carry the heat.",
                "The hearth and the stove burn ordinary fuel. A waterlogged vessel gets no heat."));
        pages.add(guide("products", "Products", List.of(GuideEntries.key("preparation")),
                "Teas, remedies, tonics, the salve, oils and garden products each do one thing today, written on their recipe page and their tooltip.",
                "Some stronger preparations need a witch to make them, at the cauldron, the still or the charm workbench; anyone may use what is made."));
        pages.add(guide("missing_ingredients", "Counting Ingredients", List.of(GuideEntries.key("research")),
                "Opened from a grimoire, a stand or a bookshelf, a recipe page counts what you carry against what it needs.",
                "Only your main inventory and offhand count: not armour, containers you carry, nearby storage or a station's own slots. Nothing is ever taken.",
                "A station slot holds one kind of item, so different alternatives are not added together. Returned bottles and bowls are results, not ingredients.",
                "Ingredients available means the items are there; it does not check the station's slots, its heat or room for the result."));
        pages.add(guide("research", "Research", List.of(GuideEntries.key("teaching")),
                "At a grimoire stand or a witch's bookshelf you may research a recipe when at least one of its ingredients is familiar: you carry it, or it is a form of a herb you know.",
                "Research is instant and free, and records the recipe only; it gives no items.",
                "The stand opens on the recipes you could research right now."));
        pages.add(guide("teaching", "Teaching", List.of(GuideEntries.key("research")),
                "At a stand or bookshelf, open a recipe you know and offer it to a player within eight blocks.",
                "They are told who offers what, and may accept or decline from their own grimoire within thirty seconds. Accepting records that one recipe and nothing else.",
                "An offer ends when either of you leaves, dies or changes dimension, or when the teacher closes their book."));
        pages.add(station(StationCatalog.DRYING_RACK, "Hang fresh herbs here to dry them into bundles. It stands two blocks wide and two tall."));
        pages.add(station(StationCatalog.MORTAR, "Grinds dried bundles, seeds and flowers into powders and dusts."));
        pages.add(station(StationCatalog.KETTLE, "Steeps powders in a water bottle over heat into teas, remedies and tonics."));
        pages.add(station(StationCatalog.HEARTH, "Burns fuel to heat the kettle or infusion pot standing on it."));
        pages.add(station(StationCatalog.STOVE, "A stove with two burners, each heating the vessel above it."));
        pages.add(station(StationCatalog.INFUSION_POT, "Infuses powders into a base (honey, plant oil or water) and fills a bowl or bottle."));
        pages.add(station(StationCatalog.CAULDRON, "A witch brews powders and a water bottle over heat into a mixture of four doses. Each batch waits for a witch to start it."));
        pages.add(station(StationCatalog.STILL, "A witch distils essences and extracts from a water or oil base into a bottle. It stands two blocks tall over its heat."));
        pages.add(station(StationCatalog.BOTTLING_BENCH, "Portions a whole mixture into four bottles or vials; gunpowder makes vials, paper adds a label. No witch needed."));
        pages.add(station(StationCatalog.CHARM_WORKBENCH, "A witch works herbs, string and gold into protective charms here."));
        return pages;
    }

    /** English for every editorial key, for the language provider. */
    public static Map<String, String> english() {
        Map<String, String> lang = new LinkedHashMap<>();
        for (Page page : pages()) {
            page.title().ifPresent(t -> lang.put(page.titleKey(), t));
            for (int i = 0; i < page.english().size(); i++) {
                lang.put(page.pageKey(i), page.english().get(i));
            }
        }
        return lang;
    }
}
