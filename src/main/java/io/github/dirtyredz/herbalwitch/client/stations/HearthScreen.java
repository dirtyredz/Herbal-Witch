package io.github.dirtyredz.herbalwitch.client.stations;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.client.grimoire.StationGrimoirePanel;
import io.github.dirtyredz.herbalwitch.grimoire.content.reference.GuideEntries;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.hearth.HearthBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.hearth.HearthMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

/**
 * The hearth (and stove): fuel, returned container and a flame showing the remaining burn. Its
 * "Grimoire" button opens the book beside it on the heat guidance.
 */
public class HearthScreen extends AbstractContainerScreen<HearthMenu> {
    public static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "textures/gui/stations/hearth.png");
    private final StationGrimoirePanel grimoire = new StationGrimoirePanel(Minecraft.getInstance().font, Optional.empty(),
            Optional.of(GuideEntries.key("heat")));

    public HearthScreen(HearthMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(grimoire.init(leftPos, topPos, imageWidth, imageHeight));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        grimoire.render(graphics, mouseX, mouseY, partialTick);
        ItemStack page = grimoire.hovered();
        if (!page.isEmpty()) {
            graphics.renderTooltip(font, page, mouseX, mouseY);
        } else {
            renderTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(ATLAS, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int total = menu.data(HearthBlockEntity.DATA_BURN_TOTAL);
        if (total > 0) {
            ProcessWidgets.flame(graphics, ATLAS, leftPos + 81, topPos + 26, menu.data(HearthBlockEntity.DATA_BURN) / (float) total);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        grimoire.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return grimoire.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return grimoire.mouseScrolled(mouseX, mouseY, delta) || super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        return (key != GLFW.GLFW_KEY_ESCAPE && grimoire.keyPressed(key, scanCode, modifiers))
                || super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        return grimoire.charTyped(c, modifiers) || super.charTyped(c, modifiers);
    }
}
