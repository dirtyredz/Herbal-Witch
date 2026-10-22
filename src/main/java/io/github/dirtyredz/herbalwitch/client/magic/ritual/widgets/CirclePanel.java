package io.github.dirtyredz.herbalwitch.client.magic.ritual.widgets;

import io.github.dirtyredz.herbalwitch.client.magic.ritual.RitualTargets;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.ritual.network.AltarRequest;
import io.github.dirtyredz.herbalwitch.magic.ritual.network.AltarUpdate;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WorkingRules;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyBalance;
import io.github.dirtyredz.herbalwitch.magic.witch.network.WitchSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.Optional;

/**
 * The circle's side of the altar screen (a witch's view): the circle's state from the server (complete,
 * or which cells are missing or unloaded), then the garden blessing and home protection with their
 * offerings and costs, each for the herb bed or ward stone the player last looked at. Buttons send
 * intent with that position; the server revalidates everything.
 */
public final class CirclePanel {
    private static final String UI = "ritual.herbalwitch.ui.";

    /** Writes the panel; returns the height used. */
    public int witch(CircleUi ui, AltarUpdate update, WitchSnapshot status, int x, int y, int width) {
        WorkingRules w = WitchRules.current().workings();
        AltarUpdate.Circle circle = update.circle();
        int h = 0;
        if (!circle.complete()) {
            h += ui.text(Component.translatable(UI + "circle_state", circle.missing().size(), circle.unloaded().size()), x, y, width,
                    OfferingLines.MISSING);
        } else {
            h += ui.text(Component.translatable(UI + "circle_complete"), x, y, width, OfferingLines.MET);
        }
        h += 2;
        h += action(ui, update, status, AltarActionSpec.GARDEN_BLESSING, w.gardenBlessingCost(), RitualTargets.bed(), AltarRequest.Op.BLESS,
                "bless", x, y + h, width, circle.complete());
        h += 2;
        h += action(ui, update, status, AltarActionSpec.HOME_PROTECTION, w.homeProtectionCost(), RitualTargets.ward(), AltarRequest.Op.PROTECT,
                "protect", x, y + h, width, circle.complete());
        return h;
    }

    private int action(CircleUi ui, AltarUpdate update, WitchSnapshot status, AltarActionSpec action, int cost, Optional<BlockPos> target,
                       AltarRequest.Op op, String name, int x, int y, int width, boolean circle) {
        int h = ui.text(Component.translatable(UI + name + "_intro", cost), x, y, width, OfferingLines.INK);
        int targetLine = target.isPresent()
                ? ui.text(Component.translatable(UI + name + "_target", target.get().getX(), target.get().getY(), target.get().getZ()), x, y + h, width,
                OfferingLines.MET)
                : ui.text(Component.translatable(UI + name + "_no_target"), x, y + h, width, OfferingLines.MISSING);
        h += targetLine;
        h += OfferingLines.write(ui, update, action, x, y + h, width);
        boolean affordable = status.energy() >= cost * EnergyBalance.UNITS;
        ui.button(Component.translatable(UI + name), x, y + h + 1, width,
                circle && target.isPresent() && affordable && OfferingLines.complete(update, action), () -> ui.send(op, target));
        return h + 18;
    }

    /** What the circle panel needs from the altar screen. */
    public interface CircleUi extends AltarUi {
        void send(AltarRequest.Op op, Optional<BlockPos> place);
    }
}
