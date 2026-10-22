package io.github.dirtyredz.herbalwitch.data.client.preparations;

import io.github.dirtyredz.herbalwitch.brewing.product.catalog.ProductSpec;
import io.github.dirtyredz.herbalwitch.brewing.product.item.AdvancedOilItem;
import io.github.dirtyredz.herbalwitch.brewing.product.item.FamiliarPreparationItem;
import io.github.dirtyredz.herbalwitch.brewing.product.item.ProductTooltips;
import io.github.dirtyredz.herbalwitch.brewing.product.item.ThrowablePreparationItem;
import io.github.dirtyredz.herbalwitch.brewing.product.metadata.PreparationMetadata;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationEffects;
import io.github.dirtyredz.herbalwitch.brewing.product.showcase.AdvancedPreparationShowcaseSection;
import io.github.dirtyredz.herbalwitch.grimoire.content.reference.AdvancedPreparationEntries;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Milestone 7's preparation English: every advanced, familiar and mixture name with its tooltip, the
 * new effects, the use messages (label, no weapon, throwing limit, familiar use pending), the showcase
 * signs and the grimoire's brewing, preparation and familiar pages (numbers are arguments).
 */
public final class AdvancedTranslations {
    private AdvancedTranslations() {
    }

    public static void addTo(LanguageProvider lang) {
        for (ProductSpec spec : AdvancedPreparations.specs()) {
            Item item = AdvancedPreparations.item(spec);
            lang.add(item, spec.displayName());
            lang.add(ProductTooltips.key(item), spec.description());
        }
        Map<String, String> effects = Map.of("binding", "Bound", "sleep", "Asleep", "steadfast", "Steadfast", "concealment", "Concealed",
                "dazzled", "Dazzled", "thorns", "Thorns", "undead_repellent", "Undead Repellent", "arthropod_repellent", "Arthropod Repellent");
        PreparationEffects.advanced().forEach(e -> lang.add(e.get(), effects.get(e.getId().getPath().substring("brewing/effects/".length()))));
        lang.add(PreparationMetadata.LABEL_KEY, "Labelled: %s");
        lang.add(AdvancedOilItem.NO_WEAPON, "Hold a sword or axe in your off hand to coat it.");
        lang.add(ThrowablePreparationItem.LIMIT, "Too many of your preparations are in flight.");
        lang.add(FamiliarPreparationItem.UNAVAILABLE, "Familiar application becomes available with familiar bonding");
        lang.add(AdvancedPreparationShowcaseSection.TITLE, "Witch's Preparations");
        Map<String, String[]> barrels = Map.of(
                "player", new String[] {"Preparations", "Draughts, vials, oils"},
                "familiar", new String[] {"For familiars", "Used once bonded"},
                "mixtures", new String[] {"Mixtures", "Bottle at the bench"});
        barrels.forEach((topic, text) -> {
            lang.add(AdvancedPreparationShowcaseSection.label(topic), text[0]);
            lang.add(AdvancedPreparationShowcaseSection.label(topic) + ".detail", text[1]);
        });
        pages().forEach((suffix, text) -> lang.add(AdvancedPreparationEntries.PREFIX + suffix, text));
    }

    /** The grimoire pages ({@code grimoire.herbalwitch.witchcraft.<suffix>}); never a digit in the prose. */
    public static Map<String, String> pages() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("brewing.title", "Witch's Work at the Stations");
        m.put("brewing.0", "The cauldron, the still and the charm workbench, and a few recipes of the mortar and the infusion pot, "
                + "work only for a witch who starts each batch and pays its energy.");
        m.put("brewing.start", "Fill the station, then press Start. Nothing is reserved or spent until you do, and each batch needs its own start.");
        m.put("brewing.payer", "The witch who started a batch pays for it, and it only progresses while she stays within %s blocks. "
                + "After she leaves the world or travels away, she presses Resume on her return; anyone may cancel a batch whose witch has left.");
        m.put("brewing.cost", "Energy is spent only when a batch completes, at %s percent of the listed cost on this server.");
        m.put("brewing.bottling", "The cauldron brews a mixture of %s doses. The bottling bench, which needs no witch, fills that many bottles "
                + "or vials from it; paper adds a label.");
        m.put("preparations.title", "Draughts, Vials and Oils");
        m.put("preparations.0", "Anyone may use a finished preparation; only making it needs a witch.");
        m.put("preparations.drinks", "Draughts are drunk like potions and give their bottle back. A healing draught only helps the hurt, "
                + "and one waits %s seconds before the next.");
        m.put("preparations.throwing", "A thrown vial bursts on what it hits, reaching creatures within %s blocks that it can see, nearest "
                + "first. Throwing waits %s seconds between vials and never returns the glass.");
        m.put("preparations.control", "Binding holds a creature in place and sleep stills it until it is struck. Neither works on players or "
                + "the greatest creatures, and after one ends a creature is steadfast for %s seconds.");
        m.put("preparations.sight", "Concealment smoke and blinding dust keep creatures from finding anyone farther than %s blocks.");
        m.put("preparations.oils", "Venom oil coats the blade in your off hand; thorn oil pricks whoever strikes you; repellents keep a "
                + "family of creatures from choosing you.");
        m.put("preparations.consent", "Harmful preparations never touch other players unless the server allows it, nor another's pets "
                + "or protected folk.");
        m.put("familiar.title", "Preparations for Familiars");
        m.put("familiar.0", "Every familiar preparation can be brewed, distilled or infused already, and the healing salve keeps its own use.");
        m.put("familiar.deferred", "Giving them to a familiar becomes possible with familiar bonding. Until then using one does nothing and "
                + "keeps it.");
        return m;
    }
}
