package io.github.dirtyredz.herbalwitch.data.botany;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The botany feature's expected generated files and names, written out independently of
 * HerbCatalog: the fourteen herbs, their groups and growth forms.
 */
public final class BotanyManifest {
    /** One herb as the tests expect it. */
    public record Herb(String id, String group, String name, boolean bush) {
        public String dir() {
            return "botany/" + group + "/" + id;
        }
    }

    public static final List<Herb> HERBS = List.of(
            new Herb("lavender", "temperate", "Lavender", true),
            new Herb("sage", "temperate", "Sage", true),
            new Herb("chamomile", "temperate", "Chamomile", false),
            new Herb("yarrow", "temperate", "Yarrow", false),
            new Herb("rosemary", "dry", "Rosemary", true),
            new Herb("thyme", "dry", "Thyme", true),
            new Herb("mint", "wet", "Mint", true),
            new Herb("nettle", "wet", "Nettle", false),
            new Herb("valerian", "shade", "Valerian", false),
            new Herb("mugwort", "shade", "Mugwort", false),
            new Herb("belladonna", "woodland", "Belladonna", true),
            new Herb("nightshade", "woodland", "Nightshade", true),
            new Herb("wolfsbane", "woodland", "Wolfsbane", false),
            new Herb("mandrake", "woodland", "Mandrake", false));
    public static final List<String> STARTERS = List.of("lavender", "sage", "mint", "chamomile");
    public static final List<String> GROUPS = List.of("temperate", "dry", "wet", "shade", "woodland");
    public static final String BED = "botany/garden/herb_bed";

    private BotanyManifest() {
    }

    public static Herb herb(String id) {
        return HERBS.stream().filter(h -> h.id().equals(id)).findFirst().orElseThrow();
    }

    public static Set<String> files() {
        Set<String> files = new TreeSet<>();
        for (Herb herb : HERBS) {
            String dir = herb.dir();
            files.add("assets/herbalwitch/blockstates/" + dir + "/plant.json");
            for (int age = 0; age < 4; age++) {
                files.add("assets/herbalwitch/models/block/" + dir + "/stage_" + age + ".json");
            }
            files.add("assets/herbalwitch/models/item/" + dir + "/seeds.json");
            files.add("assets/herbalwitch/models/item/" + dir + "/fresh.json");
            files.add("data/herbalwitch/loot_tables/blocks/" + dir + "/plant.json");
            files.add("data/herbalwitch/worldgen/configured_feature/" + dir + ".json");
            files.add("data/herbalwitch/worldgen/placed_feature/" + dir + ".json");
            files.add("data/herbalwitch/herb_conditions/" + herb.group() + "/" + herb.id() + ".json");
            if (STARTERS.contains(herb.id())) {
                files.add("data/herbalwitch/recipes/" + dir + "/seeds.json");
                files.add("data/herbalwitch/advancements/recipes/misc/" + dir + "/seeds.json");
            }
        }
        for (String group : GROUPS) {
            files.add("data/herbalwitch/forge/biome_modifier/botany/" + group + ".json");
            files.add("data/herbalwitch/tags/worldgen/biome/botany/habitat/" + group + ".json");
        }
        files.add("assets/herbalwitch/blockstates/" + BED + ".json");
        for (String part : List.of("core", "edge", "corner", "corner_inner")) {
            files.add("assets/herbalwitch/models/block/botany/garden/parts/moist/" + part + ".json");
        }
        files.add("assets/herbalwitch/models/item/" + BED + ".json");
        files.add("data/herbalwitch/loot_tables/blocks/" + BED + ".json");
        files.add("data/herbalwitch/recipes/" + BED + ".json");
        files.add("data/herbalwitch/advancements/recipes/decorations/" + BED + ".json");
        files.add("assets/herbalwitch/lang/en_us.json");
        files.add("data/herbalwitch/tags/blocks/botany/plants.json");
        files.add("data/herbalwitch/tags/blocks/botany/forage_sources.json");
        files.add("data/herbalwitch/tags/items/botany/seeds.json");
        files.add("data/herbalwitch/tags/items/botany/fresh_herbs.json");
        files.add("data/forge/tags/items/seeds.json");
        files.add("data/minecraft/tags/blocks/crops.json");
        files.add("data/minecraft/tags/blocks/maintains_farmland.json");
        files.add("data/minecraft/tags/blocks/mineable/axe.json");
        files.add("data/herbalwitch/loot_modifiers/botany/herb_forage.json");
        files.add("data/forge/loot_modifiers/global_loot_modifiers.json");
        return files;
    }

    public static Map<String, String> lang() {
        Map<String, String> lang = new LinkedHashMap<>();
        lang.put("itemGroup.herbalwitch.gardening", "The Herbal Witch: Gardening");
        lang.put("block.herbalwitch.botany.garden.herb_bed", "Herb Bed");
        for (Herb herb : HERBS) {
            String key = herb.dir().replace('/', '.');
            lang.put("block.herbalwitch." + key + ".plant", herb.name() + " Plant");
            lang.put("item.herbalwitch." + key + ".seeds", herb.name() + " Seeds");
            lang.put("item.herbalwitch." + key + ".fresh", "Fresh " + herb.name());
        }
        for (String trait : List.of("restorative", "calming", "toxic", "protective", "growth", "attraction", "binding",
                "sight", "speed", "concealment")) {
            lang.put("trait.herbalwitch." + trait, Character.toUpperCase(trait.charAt(0)) + trait.substring(1));
        }
        for (String group : GROUPS) {
            lang.put("habitat.herbalwitch." + group, null);
        }
        lang.put("tooltip.herbalwitch.herb.traits", null);
        lang.put("tooltip.herbalwitch.herb.starter", null);
        lang.put("tooltip.herbalwitch.herb.wild", null);
        return lang;
    }
}
