package io.github.dirtyredz.herbalwitch.magic.ward.grimoire;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContentSource;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import io.github.dirtyredz.herbalwitch.magic.witch.grimoire.WitchEntries;
import io.github.dirtyredz.herbalwitch.magic.ward.catalog.WardCatalog;
import io.github.dirtyredz.herbalwitch.magic.ward.registry.WardRegistration;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WorkingRules;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The ward's reference page (Milestone 7): how a ward stone is activated (directly or by the circle's
 * home protection ritual), how long it lasts, its boundary and focus, its target profiles, its limits,
 * and what it never does. Every number is an argument from the active rules; the prose holds none.
 * Activation and settings are explained here, never shown as a fictitious crafting recipe.
 */
public final class WardEntries implements GrimoireContentSource {
    public static final String PREFIX = WitchEntries.PREFIX + "wards.";
    public static final EntryKey KEY = EntryKey.of(EntryKey.Kind.GUIDE, HerbalWitch.MOD_ID + ":witchcraft/wards");

    @Override
    public void contribute(GrimoireContent.Inputs inputs, Consumer<GrimoireEntry> out) {
        WorkingRules w = WitchRules.current().workings();
        List<Component> body = List.of(
                Component.translatable(PREFIX + "0"),
                Component.translatable(PREFIX + "activation", w.wardActivationCost(), WitchEntries.minutes(w.wardChargeSeconds() * 20)),
                Component.translatable(PREFIX + "boundary", w.wardRadius(), w.wardHeight(), WardCatalog.FOCUS_REACH, w.wardFocusRadius()),
                Component.translatable(PREFIX + "profiles"),
                Component.translatable(PREFIX + "owner", w.wardsPerOwner()),
                Component.translatable(PREFIX + "loaded"),
                Component.translatable(PREFIX + "never"));
        out.accept(new GrimoireEntry(KEY, GrimoireContent.WITCHCRAFT, Component.translatable(PREFIX + "title"),
                WardRegistration.STONE_ITEM.get().getDefaultInstance(), body, List.of(WitchEntries.key("energy")), Optional.empty(), true,
                Optional.empty()));
    }
}
