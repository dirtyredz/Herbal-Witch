package io.github.dirtyredz.herbalwitch.client.familiars.screen;

import io.github.dirtyredz.herbalwitch.client.familiars.state.FamiliarClientState;
import io.github.dirtyredz.herbalwitch.client.grimoire.GrimoirePanelHost;
import io.github.dirtyredz.herbalwitch.client.magic.ritual.RitualTargets;
import io.github.dirtyredz.herbalwitch.companions.familiars.menu.FamiliarRosterMenu;
import io.github.dirtyredz.herbalwitch.companions.familiars.network.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.FamiliarCommand;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import java.util.*;

public class FamiliarRosterScreen<T extends FamiliarRosterMenu> extends GrimoirePanelHost<T> {
    protected UUID selected;
    protected int offset;
    private FamiliarRosterPage displayed;
    private FamiliarCommand desired = FamiliarCommand.STAY;
    public FamiliarRosterScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title, Optional.empty(), Optional.empty(), false); imageWidth = 280; imageHeight = 220; selected = menu.selected();
    }
    @Override protected void init() { super.init(); topPos = Math.max(18, topPos); rebuild(); }
    protected FamiliarRosterPage.Row row() {
        var page = FamiliarClientState.get(menu);
        return page == null ? null : page.rows().stream().filter(r -> r.bond().equals(selected)).findFirst().orElse(null);
    }
    protected void request(String action, UUID bond, UUID cursor, String value) {
        var page = FamiliarClientState.get(menu); if (page == null) return;
        FamiliarPackets.request(new FamiliarRequest(menu.containerId, menu.nonce(), page.revision(), action, bond, cursor, value));
    }
    protected void button(String key, int x, int y, int width, boolean enabled, Runnable action) {
        var button = Button.builder(Component.translatable("familiar.herbalwitch." + key), ignored -> action.run()).bounds(leftPos + x, topPos + y, width, 16).build();
        button.active = enabled; addRenderableWidget(button);
    }
    protected void rebuild() {
        clearWidgets(); initGrimoire(); displayed = FamiliarClientState.get(menu); if (displayed == null) return;
        offset = Math.max(0, Math.min(offset, Math.max(0, displayed.rows().size() - 5)));
        for (int i = offset; i < Math.min(offset + 5, displayed.rows().size()); i++) {
            var row = displayed.rows().get(i);
            String label = (row.bond().equals(selected) ? "> " : "") + row.name() + " · " + row.species() + " · " + Component.translatable("familiar.herbalwitch.command." + row.command()).getString();
            addRenderableWidget(Button.builder(Component.literal(font.plainSubstrByWidth(label, 248)), ignored -> { selected = row.bond(); desired = FamiliarCommand.parse(row.command()); rebuild(); })
                    .bounds(leftPos + 12, topPos + 26 + (i - offset) * 17, 256, 16).build());
        }
        var chosen = row();
        boolean active = chosen != null && chosen.phase().equals("ACTIVE");
        button("previous_command", 12, 162, 20, active, () -> { desired = FamiliarCommand.values()[Math.floorMod(desired.ordinal() - 1, 7)]; rebuild(); });
        button("command." + desired.key(), 36, 162, 150, active, () -> request("command", selected, null, desired.key()));
        button("next_command", 190, 162, 20, active, () -> { desired = FamiliarCommand.values()[(desired.ordinal() + 1) % 7]; rebuild(); });
        button("recall", 214, 162, 54, active && chosen.loaded(), () -> request("recall", selected, null, ""));
        button("first_page", 12, 8, 70, menu.selected() == null, () -> { offset = 0; request("page", null, null, ""); });
        button("next_page", 198, 8, 70, menu.selected() == null && displayed.more(), () -> {
            offset = 0; request("page", null, displayed.rows().get(displayed.rows().size() - 1).bond(), "");
        });
        button("detail", 12, 180, 123, chosen != null && chosen.loaded(), () -> request("detail", selected, null, ""));
        button("altar_target", 145, 180, 123, chosen != null, () -> {
            var current = row(); if (current != null) { RitualTargets.remember(current.bond(), current.name()); minecraft.player.closeContainer(); }
        });
    }
    @Override protected void containerTick() {
        super.containerTick(); var page = FamiliarClientState.get(menu);
        if (!Objects.equals(displayed, page)) rebuild();
    }
    @Override public boolean mouseScrolled(double x, double y, double amount) {
        if (x >= leftPos && x <= leftPos + imageWidth && y >= topPos + 26 && y < topPos + 112 && displayed != null) {
            offset = Math.max(0, Math.min(Math.max(0, displayed.rows().size() - 5), offset + (amount < 0 ? 1 : -1))); rebuild(); return true;
        }
        return super.mouseScrolled(x, y, amount);
    }
    @Override public void render(GuiGraphics graphics, int x, int y, float partial) {
        renderBackground(graphics); super.render(graphics, x, y, partial); renderGrimoire(graphics, x, y, partial);
    }
    @Override protected void renderBg(GuiGraphics g, float partial, int x, int y) {
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF3B2416);
        g.fill(leftPos + 3, topPos + 3, leftPos + imageWidth - 3, topPos + imageHeight - 3, 0xFFD4A03A);
        g.fill(leftPos + 5, topPos + 5, leftPos + imageWidth - 5, topPos + imageHeight - 5, 0xFFE5D6AE);
    }
    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        var page = FamiliarClientState.get(menu);
        if (page == null) { line(g, Component.translatable("familiar.herbalwitch.loading"), 114); return; }
        var row = row();
        if (row != null) {
            line(g, Component.translatable("familiar.herbalwitch.health", row.level(), row.health(), row.maximum(),
                    Component.translatable("familiar.herbalwitch." + (row.loaded() ? "loaded" : "last_observed"))), 114);
            line(g, Component.translatable("familiar.herbalwitch.phase." + row.phase().toLowerCase(Locale.ROOT)).append(" · " + row.dimension()), 126);
            line(g, Component.translatable("familiar.herbalwitch.home", row.home().isEmpty() ? "—" : row.home()), 138);
            line(g, row.buffs().isEmpty() ? row.status().isEmpty() ? Component.empty() : Component.translatable("familiar.herbalwitch." + row.status()) : Component.literal(row.buffs()), 150);
        } else line(g, Component.translatable(page.rows().isEmpty() ? "familiar.herbalwitch.empty" : "familiar.herbalwitch.select"), 114);
        if (!page.notice().isEmpty()) line(g, Component.translatable("familiar.herbalwitch." + page.notice()), 202);
    }
    protected void line(GuiGraphics g, Component component, int y) { g.drawString(font, font.plainSubstrByWidth(component.getString(), 256), 12, y, 0xFF3B2416, false); }
    @Override public void removed() { super.removed(); FamiliarClientState.clear(); }
}
