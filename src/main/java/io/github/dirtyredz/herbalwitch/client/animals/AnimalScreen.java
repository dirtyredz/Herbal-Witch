package io.github.dirtyredz.herbalwitch.client.animals;

import io.github.dirtyredz.herbalwitch.client.grimoire.GrimoirePanelHost;
import io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalCareAccess.Reason;
import io.github.dirtyredz.herbalwitch.companions.animals.grimoire.AnimalEntries;
import io.github.dirtyredz.herbalwitch.companions.animals.inspection.menu.AnimalMenu;
import io.github.dirtyredz.herbalwitch.companions.animals.inspection.network.AnimalRequest;
import io.github.dirtyredz.herbalwitch.platform.network.Messages;
import io.github.dirtyredz.herbalwitch.platform.time.TickTime;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import java.util.Locale;
import java.util.Optional;

/** An animal card with the existing grimoire panel. It never opens a competing reader menu. */
public final class AnimalScreen extends GrimoirePanelHost<AnimalMenu> {
    private Button adopt;
    public AnimalScreen(AnimalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, Optional.empty(), Optional.of(AnimalEntries.KEY), false);
        imageWidth = 260; imageHeight = 220;
    }
    @Override protected void init() {
        super.init();
        // Keep the shared book toggle (16px above the card) visible at the minimum 240px GUI height.
        topPos = Math.max(18, topPos);
        initGrimoire();
        adopt = addRenderableWidget(Button.builder(Component.translatable("animal.herbalwitch.adopt"), button -> {
            var state = AnimalClientState.get(menu);
            if (state != null) Messages.sendToServer(new AnimalRequest(menu.containerId, menu.nonce(), menu.target(), state.revision()));
        }).bounds(leftPos + 12, topPos + 194, 236, 18).build());
        update();
    }
    private void update() {
        var state = AnimalClientState.get(menu);
        adopt.active = state != null && state.adoption() == Reason.READY;
        adopt.setMessage(Component.translatable(state != null && (state.actions() & 2) != 0 ? "animal.herbalwitch.tame" : "animal.herbalwitch.adopt"));
    }
    @Override protected void containerTick() { super.containerTick(); update(); }
    @Override public void render(GuiGraphics g, int x, int y, float partial) {
        renderBackground(g); super.render(g, x, y, partial); renderGrimoire(g, x, y, partial);
    }
    @Override protected void renderBg(GuiGraphics g, float partial, int x, int y) {
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF3B2416);
        g.fill(leftPos + 3, topPos + 3, leftPos + imageWidth - 3, topPos + imageHeight - 3, 0xFFD4A03A);
        g.fill(leftPos + 5, topPos + 5, leftPos + imageWidth - 5, topPos + imageHeight - 5, 0xFFE5D6AE);
    }
    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        var state = AnimalClientState.get(menu);
        int y = 12;
        if (state == null) { line(g, Component.translatable("animal.herbalwitch.loading"), y); return; }
        y = line(g, Component.translatable("entity." + state.species().replace(':', '.')), y);
        y = line(g, Component.translatable("animal.herbalwitch.trust", state.trust(), state.feeds()), y + 4);
        y = line(g, Component.translatable("animal.herbalwitch.owner." + state.owner()), y);
        y = line(g, Component.translatable("animal.herbalwitch.cooldown", TickTime.ceilingSeconds(state.cooldown())), y);
        y = line(g, Component.translatable("animal.herbalwitch.effects", TickTime.ceilingSeconds(state.calm()), TickTime.ceilingSeconds(state.sleep())), y);
        y = line(g, Component.translatable("animal.herbalwitch.supported", available((state.actions() & 4) != 0),
                available((state.actions() & 1) != 0), available((state.actions() & 8) != 0)), y);
        if ((state.actions() & 1) != 0) y = line(g, Component.translatable("animal.herbalwitch.requirements",
                (state.actions() & 2) != 0 ? 100 : 80, (state.actions() & 2) != 0 ? 5 : 4), y);
        y = line(g, Component.translatable("animal.herbalwitch.reason." + state.adoption().name().toLowerCase(Locale.ROOT)), y + 6);
        if (state.species().equals("minecraft:bat")) line(g, Component.translatable("animal.herbalwitch.trust_only"), y + 4);
        else if (state.species().equals("minecraft:goat") || state.species().equals("minecraft:axolotl")) line(g, Component.translatable("animal.herbalwitch.brain"), y + 4);
    }
    private int line(GuiGraphics g, Component text, int y) {
        for (var line : font.split(text, 236)) { g.drawString(font, line, 12, y, 0xFF3B2416, false); y += 10; }
        return y;
    }
    private Component available(boolean value) { return Component.translatable("animal.herbalwitch.guide." + (value ? "enabled" : "disabled")); }
    @Override public void removed() { super.removed(); AnimalClientState.clear(); }
}
