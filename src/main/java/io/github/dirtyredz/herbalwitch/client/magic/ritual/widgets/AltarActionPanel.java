package io.github.dirtyredz.herbalwitch.client.magic.ritual.widgets;

import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.ritual.network.AltarRequest;
import io.github.dirtyredz.herbalwitch.magic.ritual.network.AltarUpdate;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyBalance;
import io.github.dirtyredz.herbalwitch.magic.witch.network.WitchSnapshot;
import net.minecraft.network.chat.Component;

import java.util.Optional;

/**
 * The altar's own actions on its screen: for a non-witch, founding the first lineage (explained,
 * then an explicit review of the exact offering and the permanent change before Confirm); for a
 * witch, meditation and attunement with their costs and current availability. Buttons only send
 * intent; the server decides.
 */
public final class AltarActionPanel {
    private static final String UI = "ritual.herbalwitch.ui.";
    private boolean confirming;

    /** A non-witch's founding area; returns the height used. */
    public int founding(AltarUi ui, AltarUpdate update, int x, int y, int width) {
        WitchRules rules = WitchRules.current();
        int h = 0;
        if (!update.foundingOpen()) {
            confirming = false;
            return ui.text(Component.translatable(UI + "founding_closed"), x, y, width, OfferingLines.INK);
        }
        if (!confirming) {
            h += ui.text(Component.translatable(UI + "founding_intro"), x, y, width, OfferingLines.INK);
            ui.button(Component.translatable(UI + "found"), x, y + h + 2, width, true, () -> {
                confirming = true;
                ui.refresh();
            });
            return h + 18;
        }
        h += ui.text(Component.translatable(UI + "founding_review", rules.foundingTicks() / 20), x, y, width, OfferingLines.INK);
        h += OfferingLines.write(ui, update, AltarActionSpec.FOUNDING, x, y + h, width);
        h += ui.text(Component.translatable(UI + "permanent"), x, y + h, width, OfferingLines.MISSING);
        int half = (width - 4) / 2;
        ui.button(Component.translatable(UI + "confirm"), x, y + h + 2, half, OfferingLines.complete(update, AltarActionSpec.FOUNDING), () -> {
            confirming = false;
            ui.send(AltarRequest.Op.FOUND, Optional.empty(), 0);
        });
        ui.button(Component.translatable(UI + "cancel"), x + half + 4, y + h + 2, half, true, () -> {
            confirming = false;
            ui.refresh();
        });
        return h + 18;
    }

    /** A witch's meditation and attunement; returns the height used. */
    public int witch(AltarUi ui, AltarUpdate update, WitchSnapshot status, int x, int y, int width) {
        confirming = false;
        EnergyBalance e = WitchRules.current().energy();
        int h = 0;
        boolean full = status.energy() >= status.capacity();
        h += ui.text(Component.translatable(UI + (full ? "meditation_full" : "meditation_intro")), x, y, width, OfferingLines.INK);
        ui.button(Component.translatable(UI + "meditate"), x, y + h + 1, width, !full, () -> ui.send(AltarRequest.Op.MEDITATE, Optional.empty(), 0));
        h += 18;
        h += ui.text(Component.translatable(UI + "attunement_intro", e.attunementCost()), x, y + h, width, OfferingLines.INK);
        h += OfferingLines.write(ui, update, AltarActionSpec.ATTUNEMENT, x, y + h, width);
        if (status.practice() >= e.practiceCeilingUnits()) {
            h += ui.text(Component.translatable(UI + "attunement_max"), x, y + h, width, OfferingLines.MISSING);
        }
        boolean affordable = status.energy() >= e.attunementCost() * EnergyBalance.UNITS;
        ui.button(Component.translatable(UI + "attune"), x, y + h + 1, width, affordable && OfferingLines.complete(update, AltarActionSpec.ATTUNEMENT),
                () -> ui.send(AltarRequest.Op.ATTUNE, Optional.empty(), 0));
        return h + 18;
    }
}
