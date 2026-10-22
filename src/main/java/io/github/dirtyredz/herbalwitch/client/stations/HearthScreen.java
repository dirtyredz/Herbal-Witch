package io.github.dirtyredz.herbalwitch.client.stations;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.client.grimoire.GrimoirePanelHost;
import io.github.dirtyredz.herbalwitch.grimoire.content.reference.GuideEntries;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.hearth.HearthBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.hearth.HearthMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.Optional;

/**
 * The hearth (and stove): fuel, returned container and a flame showing the remaining burn. Its
 * "Grimoire" button opens the book beside it on the heat guidance.
 */
public class HearthScreen extends GrimoirePanelHost<HearthMenu> {
    public static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "textures/gui/stations/hearth.png");

    public HearthScreen(HearthMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, Optional.empty(), Optional.of(GuideEntries.key("heat")), true);
    }

    @Override
    protected void init() {
        super.init();
        initGrimoire();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderGrimoire(graphics, mouseX, mouseY, partialTick);
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
