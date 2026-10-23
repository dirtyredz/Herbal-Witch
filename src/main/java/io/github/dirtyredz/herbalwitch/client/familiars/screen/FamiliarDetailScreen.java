package io.github.dirtyredz.herbalwitch.client.familiars.screen;

import io.github.dirtyredz.herbalwitch.client.familiars.state.FamiliarClientState;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.FamiliarCommand;
import io.github.dirtyredz.herbalwitch.companions.familiars.menu.FamiliarDetailMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import java.util.Locale;

/** The nearby card has exactly two gear slots; cargo is collected through a validated action. */
public final class FamiliarDetailScreen extends FamiliarRosterScreen<FamiliarDetailMenu> {
    private FamiliarCommand desired = FamiliarCommand.STAY;
    public FamiliarDetailScreen(FamiliarDetailMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); imageHeight = 240; }
    @Override protected void rebuild() {
        clearWidgets(); initGrimoire(); displayed = FamiliarClientState.get(menu);
        var chosen = row(); if (chosen == null) return;
        boolean active = chosen.phase().equals("ACTIVE");
        button("previous_command", 12, 94, 20, active, () -> { desired = FamiliarCommand.values()[Math.floorMod(desired.ordinal() - 1, 7)]; rebuild(); });
        button("command." + desired.key(), 36, 94, 150, active, () -> request("command", selected, null, desired.key()));
        button("next_command", 190, 94, 20, active, () -> { desired = FamiliarCommand.values()[(desired.ordinal() + 1) % 7]; rebuild(); });
        button("recall", 214, 94, 54, active, () -> request("recall", selected, null, ""));
        button("collect_cargo", 138, 126, 130, active, () -> request("cargo", selected, null, ""));
    }
    @Override protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
        super.renderBg(g, partial, mouseX, mouseY);
        for (var slot : menu.slots) {
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
        line(g, chosen.buffs().isEmpty() ? Component.translatable("familiar.herbalwitch.equipment_hint") : Component.literal(chosen.buffs()), 72);
        g.drawString(font, Component.translatable("familiar.herbalwitch.charm_slot"), 12, 114, 0xFF3B2416, false);
        g.drawString(font, Component.translatable("familiar.herbalwitch.gear_slot"), 80, 114, 0xFF3B2416, false);
        if (displayed != null && !displayed.notice().isEmpty()) line(g, Component.translatable("familiar.herbalwitch." + displayed.notice()), 228);
    }
}
