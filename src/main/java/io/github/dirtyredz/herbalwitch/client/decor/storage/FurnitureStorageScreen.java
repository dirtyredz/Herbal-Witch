package io.github.dirtyredz.herbalwitch.client.decor.storage;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.behavior.storage.FurnitureStorageMenu;
import io.github.dirtyredz.herbalwitch.decor.behavior.storage.StorageSpec;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * The screen of every cabinet and display: the generated atlas for its size
 * ({@code gui/decor/<cabinet_27|cabinet_54|display_3|display_6|display_9>}) under the piece's name.
 * A plain view of the vanilla-synchronised menu.
 */
public class FurnitureStorageScreen extends AbstractContainerScreen<FurnitureStorageMenu> {
    private final ResourceLocation atlas;

    public FurnitureStorageScreen(FurnitureStorageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        StorageSpec spec = menu.spec();
        this.atlas = ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID,
                "textures/gui/decor/" + (spec.display() ? "display_" : "cabinet_") + spec.slots() + ".png");
        this.imageHeight = FurnitureStorageMenu.imageHeight(spec);
        this.inventoryLabelY = FurnitureStorageMenu.playerTop(spec) - 11;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(atlas, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
