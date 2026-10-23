package io.github.dirtyredz.herbalwitch.client.familiars.screen;

import io.github.dirtyredz.herbalwitch.client.familiars.state.FamiliarClientState;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.FamiliarCommand;
import io.github.dirtyredz.herbalwitch.companions.familiars.menu.FamiliarDetailMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import java.util.Locale;
import java.util.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.ability.FamiliarPower;
import io.github.dirtyredz.herbalwitch.companions.familiars.network.FamiliarObservations;
import io.github.dirtyredz.herbalwitch.client.familiars.state.FamiliarObservationCache;
import net.minecraft.client.gui.components.Button;

/** The nearby card has exactly two gear slots; cargo is collected through a validated action. */
public final class FamiliarDetailScreen extends FamiliarRosterScreen<FamiliarDetailMenu> {
    private FamiliarCommand desired = FamiliarCommand.STAY;
    private boolean powers;
    private int powerIndex, targetOffset;
    private List<FamiliarObservations.Mark> marks = List.of();
    public FamiliarDetailScreen(FamiliarDetailMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); imageHeight = 240; }
    @Override protected void rebuild() {
        clearWidgets(); initGrimoire(); displayed = FamiliarClientState.get(menu);
        var chosen = row(); if (chosen == null) return;
        boolean active = chosen.phase().equals("ACTIVE");
        button("previous_command", 12, 94, 20, active, () -> { desired = FamiliarCommand.values()[Math.floorMod(desired.ordinal() - 1, 7)]; rebuild(); });
        button("command." + desired.key(), 36, 94, 150, active, () -> request("command", selected, null, desired.key()));
        button("next_command", 190, 94, 20, active, () -> { desired = FamiliarCommand.values()[(desired.ordinal() + 1) % 7]; rebuild(); });
        button("recall", 214, 94, 54, active, () -> request("recall", selected, null, ""));
        menu.powersView(powers);
        button(powers ? "equipment" : "powers", 214, 72, 54, true, () -> { powers = !powers; rebuild(); });
        if (!powers) { button("collect_cargo", 138, 126, 130, active, () -> request("cargo", selected, null, "")); return; }
        var available = Arrays.stream(FamiliarPower.values()).filter(p -> p != FamiliarPower.VENOM
                && p.available(chosen.species(), minecraft.level != null && minecraft.level.isNight(), chosen.buffs().contains("nightwatch"))).toList();
        powerIndex = Math.floorMod(powerIndex, available.size()); var power = available.get(powerIndex);
        button("previous_command", 12, 126, 20, active, () -> { powerIndex--; targetOffset = 0; rebuild(); });
        button("power." + power.key(), 36, 126, 174, active, () -> request("power", selected, null, power.key()));
        button("next_command", 214, 126, 20, active, () -> { powerIndex++; targetOffset = 0; rebuild(); });
        button(chosen.automatic() ? "auto_on" : "auto_off", 12, 148, 104, active, () -> request("auto", selected, null, ""));
        button("charge", 120, 148, 104, active, () -> request("charge", selected, null, ""));
        marks = FamiliarObservationCache.marks(selected);
        button("next_command", 228, 148, 40, marks.size() > 3, () -> { targetOffset = (targetOffset + 3) % marks.size(); rebuild(); });
        targetOffset = Math.min(targetOffset, Math.max(0, marks.size() - 1));
        for (int i = targetOffset; i < Math.min(targetOffset + 3, marks.size()); i++) {
            var mark = marks.get(i); var pos = net.minecraft.core.BlockPos.of(mark.position());
            var button = Button.builder(Component.literal(font.plainSubstrByWidth(mark.label() + " " + pos.toShortString(), 248)),
                    ignored -> request("power", selected, mark.token(), power.key())).bounds(leftPos + 12, topPos + 174 + (i - targetOffset) * 17, 256, 16).build();
            button.active = active && !power.sensing(); addRenderableWidget(button);
        }
    }
    @Override protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
        super.renderBg(g, partial, mouseX, mouseY);
        for (var slot : menu.slots) {
            if (!slot.isActive()) continue;
            g.fill(leftPos + slot.x - 1, topPos + slot.y - 1, leftPos + slot.x + 17, topPos + slot.y + 17, 0xFF684A34);
            g.fill(leftPos + slot.x, topPos + slot.y, leftPos + slot.x + 16, topPos + slot.y + 16, 0xFFC0AD88);
        }
    }
    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        var chosen = row();
        if (chosen == null) { line(g, Component.translatable("familiar.herbalwitch.loading"), 26); return; }
        line(g, Component.literal(chosen.name() + " · " + chosen.species()), 12);
        line(g, Component.translatable("familiar.herbalwitch.health", chosen.level(), chosen.health(), chosen.maximum(), Component.translatable("familiar.herbalwitch.loaded")), 30);
        line(g, Component.translatable("familiar.herbalwitch.command." + chosen.command()).append(" · ").append(Component.translatable("familiar.herbalwitch.phase." + chosen.phase().toLowerCase(Locale.ROOT))), 44);
        line(g, Component.translatable("familiar.herbalwitch.home", chosen.home().isEmpty() ? "—" : chosen.home()), 58);
        g.drawString(font, font.plainSubstrByWidth(chosen.buffs().isEmpty() ? Component.translatable("familiar.herbalwitch.equipment_hint").getString() : chosen.buffs(), 198), 12, 72, 0xFF3B2416, false);
        if (powers) line(g, Component.translatable("familiar.herbalwitch.reserve", chosen.reserve() / 100.0), 114);
        else {
        g.drawString(font, Component.translatable("familiar.herbalwitch.charm_slot"), 12, 114, 0xFF3B2416, false);
        g.drawString(font, Component.translatable("familiar.herbalwitch.gear_slot"), 80, 114, 0xFF3B2416, false);
        }
        if (displayed != null && !displayed.notice().isEmpty()) line(g, Component.translatable("familiar.herbalwitch." + displayed.notice()), 228);
    }
    @Override protected void containerTick() {
        super.containerTick(); if (powers && !marks.equals(FamiliarObservationCache.marks(selected))) rebuild();
    }
}
