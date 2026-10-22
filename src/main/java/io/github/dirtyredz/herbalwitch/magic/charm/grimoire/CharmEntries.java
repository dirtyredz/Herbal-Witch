package io.github.dirtyredz.herbalwitch.magic.charm.grimoire;

import io.github.dirtyredz.herbalwitch.grimoire.catalog.WitchcraftKeys;

import io.github.dirtyredz.herbalwitch.platform.time.TickTime;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContentSource;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import io.github.dirtyredz.herbalwitch.magic.charm.catalog.CharmCatalog;
import io.github.dirtyredz.herbalwitch.magic.charm.registry.CharmItems;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * The charms' reference page (Milestone 7): wearing one (off hand, or a Curios slot), the single
 * active charm, its finite charge, and what each softens. Numbers are arguments (the charge from the
 * active rules, the strengths from the catalogue); the prose holds none.
 */
public final class CharmEntries implements GrimoireContentSource {
    /** Where this source reads in the index (lower first); spaced so a later feature slots between. */
    public static final int PRIORITY = 1000;

    public static final String PREFIX = WitchcraftKeys.PREFIX + "charms.";
    public static final EntryKey KEY = EntryKey.of(EntryKey.Kind.GUIDE, HerbalWitch.MOD_ID + ":witchcraft/charms");

    @Override
    public void contribute(GrimoireContent.Inputs inputs, Consumer<GrimoireEntry> out) {
        int seconds = WitchRules.current().workings().charmChargeSeconds();
        List<Component> body = List.of(
                Component.translatable(PREFIX + "0"),
                Component.translatable(PREFIX + "wear"),
                Component.translatable(PREFIX + "charge", TickTime.ceilingMinutes(seconds * 20)),
                Component.translatable(PREFIX + "sachet", CharmCatalog.PROTECTIVE_SACHET.percent(), half(CharmCatalog.PROTECTIVE_SACHET.cap())),
                Component.translatable(PREFIX + "amulets", CharmCatalog.UNDEAD_AMULET.percent(), half(CharmCatalog.UNDEAD_AMULET.cap())),
                Component.translatable(PREFIX + "limits"));
        out.accept(GrimoireEntry.page(KEY, GrimoireContent.WITCHCRAFT, Component.translatable(PREFIX + "title"),
                CharmItems.item(CharmCatalog.PROTECTIVE_SACHET).getDefaultInstance(), body, List.of()));
    }

    /** Health as hearts, for page text. */
    private static String half(float health) {
        return String.valueOf(health / 2).replaceAll("\\.0$", "");
    }
}
