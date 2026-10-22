package io.github.dirtyredz.herbalwitch.magic.witch.grimoire;

import io.github.dirtyredz.herbalwitch.grimoire.catalog.WitchcraftKeys;

import io.github.dirtyredz.herbalwitch.platform.time.TickTime;

import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContentSource;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyBalance;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The public witchcraft guidance (Milestone 6): what witch status is and how it is gained, energy and
 * capacity with every practice threshold, and how energy recovers. Every number comes from the active
 * rules ({@link WitchRules#current()}: the server's own, or on a client the copy its server sent),
 * never from prose, so the page always describes the mechanics in force. Readable before any
 * discovery; it grants and authorises nothing.
 */
public final class WitchEntries implements GrimoireContentSource {
    /** Where this source reads in the index (lower first); spaced so a later feature slots between. */
    public static final int PRIORITY = 100;

    public static final String PREFIX = WitchcraftKeys.PREFIX;
    /** The pages, in reading order. */
    public static final List<String> PAGES = WitchcraftKeys.CONVERSION_PAGES;

    @Override
    public void contribute(GrimoireContent.Inputs inputs, Consumer<GrimoireEntry> out) {
        WitchRules rules = WitchRules.current();
        EnergyBalance e = rules.energy();
        for (String page : PAGES) {
            List<Component> body = new ArrayList<>();
            body.add(Component.translatable(PREFIX + page + ".0"));
            switch (page) {
                case "status" -> {
                    body.add(Component.translatable(PREFIX + "status.routes"));
                    body.add(Component.translatable(PREFIX + "status.permanent"));
                }
                case "energy" -> {
                    body.add(Component.translatable(PREFIX + "energy.capacity", e.initialCapacity(), e.capacityPerThreshold(), e.finalCapacity()));
                    body.add(Component.translatable(PREFIX + "energy.thresholds",
                            e.thresholds().stream().map(String::valueOf).collect(Collectors.joining(", "))));
                    body.add(Component.translatable(PREFIX + "energy.practice"));
                    body.add(Component.translatable(PREFIX + "energy.exhaustion"));
                }
                case "recovery" -> {
                    body.add(Component.translatable(PREFIX + "recovery.rest", e.restAmount(), TickTime.ceilingSeconds(e.restInterval())));
                    body.add(Component.translatable(PREFIX + "recovery.meditation", TickTime.ceilingSeconds(e.meditationSettle()), e.meditationAmount(),
                            TickTime.ceilingSeconds(e.meditationInterval())));
                    body.add(Component.translatable(PREFIX + "recovery.sleep", e.sleepAmount()));
                    body.add(Component.translatable(PREFIX + "recovery.tea", e.teaAmount(), TickTime.ceilingSeconds(e.teaCooldown())));
                    body.add(Component.translatable(PREFIX + "recovery.restful", e.restfulBonus(), TickTime.ceilingMinutes(e.restfulDuration())));
                    body.add(Component.translatable(PREFIX + "recovery.bonus", e.natureBonus(), e.moonBonus(), e.bonusCap()));
                }
                default -> throw new IllegalStateException(page);
            }
            List<EntryKey> links = PAGES.stream().filter(p -> !p.equals(page)).map(WitchcraftKeys::key).toList();
            out.accept(GrimoireEntry.page(WitchcraftKeys.key(page), GrimoireContent.WITCHCRAFT, Component.translatable(PREFIX + page + ".title"),
                    GrimoireItems.BOOK.get().getDefaultInstance(), body, links));
        }
    }

}
