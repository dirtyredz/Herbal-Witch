package io.github.dirtyredz.herbalwitch.data.client.magic.charm;

import io.github.dirtyredz.herbalwitch.magic.charm.catalog.CharmCatalog;
import io.github.dirtyredz.herbalwitch.magic.charm.catalog.CharmSpec;
import io.github.dirtyredz.herbalwitch.magic.charm.grimoire.CharmEntries;
import io.github.dirtyredz.herbalwitch.magic.charm.item.ProtectiveCharmItem;
import io.github.dirtyredz.herbalwitch.magic.charm.registry.CharmItems;
import io.github.dirtyredz.herbalwitch.magic.charm.showcase.CharmShowcaseSection;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.LinkedHashMap;
import java.util.Map;

/** The charms' English: names, tooltips and the charge line, the grimoire page (numbers are arguments) and the showcase signs. */
public final class CharmTranslations {
    private CharmTranslations() {
    }

    public static void addTo(LanguageProvider lang) {
        for (CharmSpec spec : CharmCatalog.ALL) {
            Item item = CharmItems.item(spec);
            lang.add(item, spec.displayName());
            lang.add(item.getDescriptionId() + ".tooltip", spec.description());
        }
        lang.add(ProtectiveCharmItem.WEAR, "%s of %s seconds of protection left");
        pages().forEach((suffix, text) -> lang.add(CharmEntries.PREFIX + suffix, text));
        lang.add(CharmShowcaseSection.TITLE, "Protective Charms");
        lang.add(CharmShowcaseSection.label("samples"), "Charms");
        Map.of("wear", new String[] {"Wear one", "Off hand or a charm slot"}, "soften", new String[] {"Softer blows", "From mobs only"})
                .forEach((topic, text) -> {
                    lang.add(CharmShowcaseSection.label(topic), text[0]);
                    lang.add(CharmShowcaseSection.label(topic) + ".detail", text[1]);
                });
    }

    /** The grimoire page ({@code grimoire.herbalwitch.witchcraft.charms.<suffix>}); never a digit in the prose. */
    public static Map<String, String> pages() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("title", "Protective Charms");
        m.put("0", "Charms are made at the charm workbench by a witch, and anyone may wear one.");
        m.put("wear", "Hold a charm in your off hand, or with Curios wear it in a charm slot. Only one charm works at a time: "
                + "the off hand first.");
        m.put("charge", "A fresh charm lasts about %s minutes of wearing; it only spends while worn and alive, and breaks when spent.");
        m.put("sachet", "The protective sachet softens any mob's blow by %s percent, at most %s hearts a blow.");
        m.put("amulets", "An amulet softens its family's blows (the undead, or spiders and pests) by %s percent, at most %s hearts.");
        m.put("limits", "Charms never soften a player's blow, a fall or the weather, and never strike back.");
        return m;
    }
}
