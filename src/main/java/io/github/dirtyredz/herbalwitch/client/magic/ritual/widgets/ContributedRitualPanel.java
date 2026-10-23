package io.github.dirtyredz.herbalwitch.client.magic.ritual.widgets;

import io.github.dirtyredz.herbalwitch.client.magic.ritual.RitualTargets;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.ritual.network.AltarUpdate;
import io.github.dirtyredz.herbalwitch.magic.witch.network.WitchSnapshot;
import net.minecraft.network.chat.Component;
import java.util.*;

/** One contributed action at a time, keeping the altar panel bounded as features add actions. */
public final class ContributedRitualPanel {
    private int selected;
    public interface Ui extends AltarUi { void contributed(AltarActionSpec action, UUID target); }
    public void draw(Ui ui, AltarUpdate update, WitchSnapshot status, int x, int y, int width) {
        var actions = Arrays.stream(AltarActionSpec.values()).filter(a -> a.kind() == AltarActionSpec.Kind.CONTRIBUTED).toList();
        if (actions.isEmpty()) return;
        selected = Math.floorMod(selected, actions.size()); var action = actions.get(selected);
        ui.button(Component.literal("<"), x, y, 18, true, () -> { selected--; ui.refresh(); });
        ui.button(Component.literal(">"), x + width - 18, y, 18, true, () -> { selected++; ui.refresh(); });
        int h = 17 + ui.text(Component.translatable("grimoire.herbalwitch.ritual." + action.key() + ".title"), x, y + 17, width, OfferingLines.INK);
        h += ui.text(Component.translatable("ritual.herbalwitch.ui.contributed_terms", action.energy(), action.ticks() / 20), x, y + h, width, OfferingLines.INK);
        h += ui.text(Component.literal(RitualTargets.creatureName().isEmpty() ? "Look at a creature first, or select a bond in your roster." : RitualTargets.creatureName()), x, y + h, width, OfferingLines.INK);
        h += ui.text(Component.translatable("grimoire.herbalwitch.ritual." + action.key() + ".offering"), x, y + h, width, OfferingLines.INK);
        ui.button(Component.translatable("ritual.herbalwitch.ui.confirm"), x, y + h + 2, width,
                RitualTargets.creature().isPresent() && update.circle().complete() && status.energy() >= action.energy() * 100,
                () -> RitualTargets.creature().ifPresent(target -> ui.contributed(action, target)));
    }
}
