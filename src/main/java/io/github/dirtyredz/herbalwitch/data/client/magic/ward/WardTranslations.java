package io.github.dirtyredz.herbalwitch.data.client.magic.ward;

import io.github.dirtyredz.herbalwitch.magic.ward.catalog.WardCatalog;
import io.github.dirtyredz.herbalwitch.magic.ward.grimoire.WardEntries;
import io.github.dirtyredz.herbalwitch.magic.ward.menu.WardMenu;
import io.github.dirtyredz.herbalwitch.magic.ward.registry.WardRegistration;
import io.github.dirtyredz.herbalwitch.magic.ward.service.WardService;
import io.github.dirtyredz.herbalwitch.magic.ward.showcase.WardShowcaseSection;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.LinkedHashMap;
import java.util.Map;

/** The ward stone's English: its name and screen, every outcome, the grimoire page (numbers are arguments) and the showcase signs. */
public final class WardTranslations {
    private WardTranslations() {
    }

    public static void addTo(LanguageProvider lang) {
        lang.add(WardRegistration.STONE.get(), WardCatalog.NAME);
        lang.add(WardMenu.TITLE, WardCatalog.NAME);
        outcomes().forEach((suffix, text) -> lang.add(WardService.KEY + suffix, text));
        ui().forEach((suffix, text) -> lang.add(WardService.KEY + "ui." + suffix, text));
        pages().forEach((suffix, text) -> lang.add(WardEntries.PREFIX + suffix, text));
        lang.add(WardShowcaseSection.TITLE, WardCatalog.NAME);
        Map.of("activate", new String[] {"Activate", "A witch charges it"},
                "boundary", new String[] {"Boundary", "A hearth widens it"},
                "protection", new String[] {"Protection", "Undead and crawlers"})
                .forEach((topic, text) -> {
                    lang.add(WardShowcaseSection.label(topic), text[0]);
                    lang.add(WardShowcaseSection.label(topic) + ".detail", text[1]);
                });
    }

    /** What a ward action did ({@code ward.herbalwitch.<outcome>}). */
    public static Map<String, String> outcomes() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("no_stone", "That is not a ward stone.");
        m.put("unsupported", "The ward records cannot be read; wards are disabled.");
        m.put("already_charged", "The ward is already charged; it cannot be topped up.");
        m.put("not_owner", "This ward is bound to another witch.");
        m.put("not_witch", "Only a witch can activate a ward.");
        m.put("owner_limit", "You hold as many wards as you may.");
        m.put("world_limit", "This world holds as many wards as it may.");
        m.put("dimension_limit", "This world already has as many active wards as it may.");
        m.put("chunk_limit", "Too many wards stand close together.");
        m.put("overlap", "It would overlap another active ward.");
        m.put("protected", "The land is protected here.");
        m.put("too_far", "Stand closer to the stone.");
        m.put("not_enough_energy", "Not enough energy.");
        m.put("failed", "The ward could not be charged; nothing was spent.");
        m.put("activated", "The ward is charged.");
        m.put("not_charged", "The ward is not charged.");
        m.put("deactivated", "The ward is quiet; its charge is gone.");
        m.put("unbound", "The ward is bound to nobody.");
        m.put("released", "You released the ward.");
        m.put("radius_limit", "That is as far as it reaches.");
        m.put("radius", "The boundary changed.");
        m.put("profile", "The ward keeps different creatures away now.");
        return m;
    }

    /** The screen ({@code ward.herbalwitch.ui.<suffix>}). */
    public static Map<String, String> ui() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("state.0", "The ward is quiet.");
        m.put("state.1", "The ward is protecting.");
        m.put("state.2", "Charged, but suspended: it overlaps another ward or the land is protected.");
        m.put("state.3", "The ward records cannot be read.");
        m.put("owner.0", "Bound to nobody yet.");
        m.put("owner.1", "Bound to you.");
        m.put("owner.2", "Bound to another witch.");
        m.put("charge", "%s min %s s left");
        m.put("radius", "Radius %s of %s, height %s");
        m.put("radius_down", "-");
        m.put("radius_up", "+");
        m.put("profile", "Change");
        m.put("profile_is", "Keeps away: %s");
        m.put("profile.undead", "the undead");
        m.put("profile.arthropods", "spiders and pests");
        m.put("profile.both", "the undead and spiders");
        m.put("activate", "Activate");
        m.put("deactivate", "Quiet");
        m.put("release", "Release");
        m.put("cost", "Activation: %s energy, %s min");
        return m;
    }

    /** The grimoire page ({@code grimoire.herbalwitch.witchcraft.wards.<suffix>}); never a digit in the prose. */
    public static Map<String, String> pages() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("title", "Home Wards");
        m.put("0", "A ward stone keeps chosen creatures from choosing anyone inside its boundary as prey.");
        m.put("activation", "A witch standing by an empty stone activates it for %s energy, or charges it at an altar in a ritual "
                + "circle; either way it protects for %s minutes of loaded time. It is never topped up, and quieting it gives nothing back.");
        m.put("boundary", "It reaches %s blocks each way and %s up and down. A hearth or stove within %s blocks widens it to %s; "
                + "losing that focus shrinks it at once.");
        m.put("profiles", "It keeps away the undead, spiders and crawling pests, or both, as its owner chooses.");
        m.put("owner", "The first activation binds it to that witch, who alone changes it; a witch may hold %s wards.");
        m.put("loaded", "It only works, and only spends its charge, while its land is loaded. Breaking it ends the ward; the stone "
                + "comes back empty.");
        m.put("never", "It never harms, pushes or removes a creature and stops no damage: it only keeps them from choosing you or your "
                + "guests.");
        return m;
    }
}
