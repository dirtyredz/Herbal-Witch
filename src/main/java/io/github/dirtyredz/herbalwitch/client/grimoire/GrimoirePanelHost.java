package io.github.dirtyredz.herbalwitch.client.grimoire;

import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;
import java.util.function.BooleanSupplier;

/** One persistent panel per screen, with shared layout, tick, tooltip and input fallback rules. */
public abstract class GrimoirePanelHost<M extends AbstractContainerMenu> extends AbstractContainerScreen<M> {
    private final Routing grimoire;
    private final boolean containerTooltips;

    protected GrimoirePanelHost(M menu, Inventory inventory, Component title, Optional<String> category,
                                Optional<EntryKey> start, boolean containerTooltips) {
        super(menu, inventory, title);
        this.grimoire = new Routing(new StationGrimoirePanel(Minecraft.getInstance().font, category, start));
        this.containerTooltips = containerTooltips;
    }

    /** Also called after an altar rebuild clears its widgets; the panel/view itself is retained. */
    protected final void initGrimoire() {
        addRenderableWidget(grimoire.init(leftPos, topPos, imageWidth, imageHeight));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        grimoire.tick();
    }

    /** Called after each host's own drawing, retaining its original layer order. */
    protected final void renderGrimoire(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        grimoire.render(graphics, font, mouseX, mouseY, partialTick, () -> {
            if (containerTooltips) renderTooltip(graphics, mouseX, mouseY);
        });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return grimoire.mouseClicked(mouseX, mouseY, button, () -> super.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return grimoire.mouseScrolled(mouseX, mouseY, delta, () -> super.mouseScrolled(mouseX, mouseY, delta));
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        return grimoire.keyPressed(key, scanCode, modifiers, () -> super.keyPressed(key, scanCode, modifiers));
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        return grimoire.charTyped(c, modifiers, () -> super.charTyped(c, modifiers));
    }

    /** The headless boundary tests drive the same delegate used by all four real screens. */
    interface Panel {
        Button init(int left, int top, int width, int height);
        void tick();
        void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);
        boolean tooltip(GuiGraphics graphics, Font font, int mouseX, int mouseY);
        boolean mouseClicked(double mouseX, double mouseY, int button);
        boolean mouseScrolled(double mouseX, double mouseY, double delta);
        boolean keyPressed(int key, int scanCode, int modifiers);
        boolean charTyped(char c, int modifiers);
    }

    static final class Routing {
        private final Panel panel;

        Routing(Panel panel) {
            this.panel = panel;
        }

        public Button init(int left, int top, int width, int height) {
            return panel.init(left, top, width, height);
        }

        public void tick() {
            panel.tick();
        }

        public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, Runnable fallback) {
            panel.render(graphics, mouseX, mouseY, partialTick);
            if (!panel.tooltip(graphics, font, mouseX, mouseY)) fallback.run();
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button, BooleanSupplier fallback) {
            return panel.mouseClicked(mouseX, mouseY, button) || fallback.getAsBoolean();
        }

        public boolean mouseScrolled(double mouseX, double mouseY, double delta, BooleanSupplier fallback) {
            return panel.mouseScrolled(mouseX, mouseY, delta) || fallback.getAsBoolean();
        }

        public boolean keyPressed(int key, int scanCode, int modifiers, BooleanSupplier fallback) {
            return (key != GLFW.GLFW_KEY_ESCAPE && panel.keyPressed(key, scanCode, modifiers)) || fallback.getAsBoolean();
        }

        public boolean charTyped(char c, int modifiers, BooleanSupplier fallback) {
            return panel.charTyped(c, modifiers) || fallback.getAsBoolean();
        }
    }
}
