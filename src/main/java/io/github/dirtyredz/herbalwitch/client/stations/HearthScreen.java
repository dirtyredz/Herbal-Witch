package io.github.dirtyredz.herbalwitch.client.stations;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.stations.everyday.hearth.block.HearthBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.hearth.menu.HearthMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** The hearth: fuel, returned container and a flame showing the remaining burn. */
public class HearthScreen extends AbstractContainerScreen<HearthMenu> {
    public static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "textures/gui/stations/hearth.png");

    public HearthScreen(HearthMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(ATLAS, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int total = menu.data(HearthBlockEntity.DATA_BURN_TOTAL);
        if (total > 0) {
            ProcessWidgets.flame(graphics, ATLAS, leftPos + 81, topPos + 26, menu.data(HearthBlockEntity.DATA_BURN) / (float) total);
        }
    }
}
