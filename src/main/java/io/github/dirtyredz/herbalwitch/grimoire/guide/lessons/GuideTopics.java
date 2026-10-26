package io.github.dirtyredz.herbalwitch.grimoire.guide.lessons;

import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.HerbalMaterialCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;

import java.util.ArrayList;
import java.util.List;

/**
 * The first-herb guide topics: each item the guide explains and the authored page that explains it. The one
 * catalogue behind both the lesson's associated items and the generated guide pages. Plain strings and
 * catalogue reads only, so it carries no optional-library or registry type.
 */
public final class GuideTopics {
    /**
     * One guide topic.
     *
     * @param item the item id the page is about, e.g. {@code herbalwitch:stations/mortar/station}
     * @param page the authored page path under {@code guide_sources/<lang>/}, e.g. {@code stations/mortar.md}
     */
    public record Topic(String item, String page) { }

    /** The fourteen first-herb topics, lavender's fresh herb first. */
    public static final List<Topic> FIRST_PREPARATION = firstPreparation();

    private GuideTopics() {
    }

    private static List<Topic> firstPreparation() {
        List<Topic> topics = new ArrayList<>();
        addHerb(topics, HerbCatalog.LAVENDER, "apothecary/lavender/");
        addHerb(topics, HerbCatalog.VALERIAN, "apothecary/valerian/");
        topics.add(topic(EverydayProductCatalog.RESTFUL_INFUSION.path(), "apothecary/restful_infusion.md"));
        topics.add(topic(StationCatalog.DRYING_RACK.path(), "stations/drying_rack.md"));
        topics.add(topic(StationCatalog.MORTAR.path(), "stations/mortar.md"));
        topics.add(topic(StationCatalog.KETTLE.path(), "stations/kettle.md"));
        topics.add(topic(StationCatalog.STOVE.path(), "stations/stove.md"));
        topics.add(topic(StationCatalog.HEARTH.path(), "stations/hearth.md"));
        return List.copyOf(topics);
    }

    private static void addHerb(List<Topic> topics, HerbSpec herb, String folder) {
        topics.add(topic(herb.freshPath(), folder + "fresh.md"));
        topics.add(topic(herb.seedsPath(), folder + "seeds.md"));
        topics.add(topic(HerbalMaterialCatalog.bundlePath(herb), folder + "bundle.md"));
        topics.add(topic(HerbalMaterialCatalog.powderPath(herb), folder + "powder.md"));
    }

    private static Topic topic(String itemPath, String page) {
        return new Topic("herbalwitch:" + itemPath, page);
    }
}
