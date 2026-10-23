package io.github.dirtyredz.herbalwitch.client.grimoire;

import io.github.dirtyredz.herbalwitch.client.grimoire.widgets.EntryList;
import io.github.dirtyredz.herbalwitch.grimoire.access.menu.GrimoireMenu;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.GrimoireCatalog;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

/**
 * The reader: the open book (252 x 180 from the reader atlas) with the index on the left page and
 * the chosen entry on the right. A stand opens on the research filter, a bookshelf and a carried
 * book on the full index. The menu has no slots, so nothing here moves items.
 */
public class GrimoireScreen extends AbstractContainerScreen<GrimoireMenu> {
    private GrimoireView view;

    public GrimoireScreen(GrimoireMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 252;
        imageHeight = 180;
    }

    @Override
    protected void init() {
        super.init();
        if (view == null) {
            EntryList.Filter filter = menu.access().opens() == GrimoireCatalog.Opens.RESEARCH ? EntryList.Filter.RESEARCH
                    : EntryList.Filter.ALL;
            view = new GrimoireView(font, GrimoireView.Layout.SPREAD,
                    Optional.of(new GrimoireView.ReaderLink(menu.containerId, menu.nonce(), menu.access())), filter, Optional.empty());
        }
        view.setBounds(leftPos + 10, topPos + 10, 232, 160);
        var actions = io.github.dirtyredz.herbalwitch.grimoire.access.menu.GrimoireMenuActions.actions();
        for (int i = 0; i < actions.size(); i++) {
            int button = i;
            addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.translatable(actions.get(i).label()),
                    ignored -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button))
                    .bounds(leftPos + i * 62, topPos + imageHeight + 2, 60, 16).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        view.render(graphics, mouseX, mouseY, partialTick);
        ItemStack hovered = view.hovered();
        if (!hovered.isEmpty()) {
            graphics.renderTooltip(font, hovered, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(GrimoireView.ATLAS, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // the book shows its own titles; no container or inventory labels
    }

    @Override
    protected void containerTick() {
        view.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return view.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return view.mouseScrolled(mouseX, mouseY, delta) || super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key != GLFW.GLFW_KEY_ESCAPE && view.searching()) {
            view.keyPressed(key, scanCode, modifiers);
            return true; // typing in the search box never closes the book
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        return view.charTyped(c, modifiers) || super.charTyped(c, modifiers);
    }
}
