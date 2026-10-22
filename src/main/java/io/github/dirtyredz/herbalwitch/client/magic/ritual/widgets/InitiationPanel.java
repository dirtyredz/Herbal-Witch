package io.github.dirtyredz.herbalwitch.client.magic.ritual.widgets;

import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.ritual.network.AltarRequest;
import io.github.dirtyredz.herbalwitch.magic.ritual.network.AltarUpdate;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyBalance;
import io.github.dirtyredz.herbalwitch.magic.witch.network.WitchSnapshot;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Consent on the altar screen. A recipient sees who invites them (or that an operator designated
 * them), that the change is permanent, who pays, and the time left, then chooses "Accept and begin"
 * or "Decline" explicitly. A witch chooses a nearby non-witch, sees the offering they supply and the
 * energy they pay, and invites (or withdraws a pending invitation). Never a forced screen.
 */
public final class InitiationPanel {
    private static final String UI = "ritual.herbalwitch.ui.";
    private Optional<UUID> selected = Optional.empty();

    /** A non-witch's incoming invitation; returns the height used (0 without one). */
    public int recipient(AltarUi ui, AltarUpdate update, int x, int y, int width) {
        if (update.incoming().isEmpty()) {
            return 0;
        }
        AltarUpdate.Invitation i = update.incoming().get();
        int h = 0;
        h += ui.text(i.administrative() ? Component.translatable(UI + "designated")
                : Component.translatable(UI + "invited_by", i.name(), WitchRules.current().energy().initiationCost()), x, y, width, OfferingLines.INK);
        h += ui.text(Component.translatable(UI + "permanent"), x, y + h, width, OfferingLines.MISSING);
        h += ui.text(Component.translatable(UI + "expires", i.ticksLeft() / 20), x, y + h, width, OfferingLines.INK);
        boolean here = i.administrative() || i.here();
        if (!here) {
            h += ui.text(Component.translatable(UI + "other_altar"), x, y + h, width, OfferingLines.MISSING);
        }
        int half = (width - 4) / 2;
        ui.button(Component.translatable(UI + "accept"), x, y + h + 2, half, here, () -> ui.send(AltarRequest.Op.ACCEPT, Optional.empty(), i.id()));
        ui.button(Component.translatable(UI + "decline"), x + half + 4, y + h + 2, half, true,
                () -> ui.send(AltarRequest.Op.DECLINE, Optional.empty(), i.id()));
        return h + 20;
    }

    /** A witch inviting someone; returns the height used. */
    public int initiator(AltarUi ui, AltarUpdate update, WitchSnapshot status, int x, int y, int width) {
        EnergyBalance e = WitchRules.current().energy();
        int h = 0;
        if (update.outgoing().isPresent()) {
            AltarUpdate.Invitation o = update.outgoing().get();
            h += ui.text(Component.translatable(UI + "inviting", o.name(), o.ticksLeft() / 20), x, y, width, OfferingLines.INK);
            ui.button(Component.translatable(UI + "withdraw"), x, y + h + 1, width, true, () -> ui.send(AltarRequest.Op.WITHDRAW, Optional.empty(), 0));
            return h + 18;
        }
        if (!WitchRules.current().initiationEnabled()) {
            return ui.text(Component.translatable(UI + "initiation_disabled"), x, y, width, OfferingLines.INK);
        }
        h += ui.text(Component.translatable(UI + "initiation_intro", e.initiationCost()), x, y, width, OfferingLines.INK);
        if (update.nearby().isEmpty()) {
            selected = Optional.empty();
            return h + ui.text(Component.translatable(UI + "nobody"), x, y + h, width, OfferingLines.MISSING);
        }
        selected = selected.filter(id -> update.nearby().stream().anyMatch(n -> n.id().equals(id)));
        int column = (width - 4) / 2;
        int n = 0;
        for (AltarUpdate.Nearby nearby : update.nearby()) {
            boolean chosen = selected.map(nearby.id()::equals).orElse(false);
            Component label = chosen ? Component.literal("> " + nearby.name()) : Component.literal(nearby.name());
            ui.button(label, x + (n % 2) * (column + 4), y + h + (n / 2) * 16, column, true, () -> {
                selected = Optional.of(nearby.id());
                ui.refresh();
            });
            n++;
        }
        h += ((n + 1) / 2) * 16 + 2;
        h += OfferingLines.write(ui, update, AltarActionSpec.INITIATION, x, y + h, width);
        boolean ready = selected.isPresent() && OfferingLines.complete(update, AltarActionSpec.INITIATION)
                && status.energy() >= e.initiationCost() * EnergyBalance.UNITS;
        ui.button(Component.translatable(UI + "invite"), x, y + h + 1, width, ready,
                () -> ui.send(AltarRequest.Op.INVITE, selected, 0));
        return h + 18;
    }
}
