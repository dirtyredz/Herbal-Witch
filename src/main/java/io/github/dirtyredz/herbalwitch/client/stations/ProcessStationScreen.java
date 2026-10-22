package io.github.dirtyredz.herbalwitch.client.stations;

import io.github.dirtyredz.herbalwitch.client.grimoire.StationGrimoirePanel;
import io.github.dirtyredz.herbalwitch.stations.menu.StationMenu;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessStation;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessStatus;
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
 * The screen of a processing station (rack, mortar, kettle, infusion pot): its generated atlas,
 * the progress arrow, the heat flame for heated stations and the explicit status (missing input,
 * needs heat, working, output blocked). Purely a view of vanilla-synchronised menu data; it sends
 * nothing to the server. A "Grimoire" button opens the book beside it on the station's category.
 *
 * @param <M> the station's menu
 */
public class ProcessStationScreen<M extends StationMenu> extends AbstractContainerScreen<M> {
    /**
     * Where a station's widgets sit on its atlas. {@code flameX < 0}: no heat indicator.
     * {@code category}: the processing category its grimoire panel lists.
     */
    public record Layout(ResourceLocation atlas, int arrowX, int arrowY, int flameX, int flameY, String category) {
    }

    private final Layout layout;
    private final StationGrimoirePanel grimoire;

    public ProcessStationScreen(M menu, Inventory inventory, Component title, Layout layout) {
        super(menu, inventory, title);
        this.layout = layout;
        this.grimoire = new StationGrimoirePanel(Minecraft.getInstance().font, Optional.of(layout.category()), Optional.empty());
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
        graphics.blit(layout.atlas(), leftPos, topPos, 0, 0, imageWidth, imageHeight);
        ProcessWidgets.arrow(graphics, layout.atlas(), leftPos + layout.arrowX(), topPos + layout.arrowY(),
                menu.data(ProcessStation.DATA_PROGRESS), menu.data(ProcessStation.DATA_DURATION));
        if (layout.flameX() >= 0 && menu.data(ProcessStation.DATA_HEATED) != 0) {
            ProcessWidgets.flame(graphics, layout.atlas(), leftPos + layout.flameX(), topPos + layout.flameY(), 1.0F);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        ProcessStatus status = ProcessStatus.byOrdinal(menu.data(ProcessStation.DATA_STATUS));
        int colour = switch (status) {
            case WORKING -> 0x2F5A2C;
            case NEEDS_HEAT, OUTPUT_BLOCKED -> 0xB0305A;
            default -> 0x404040;
        };
        ProcessWidgets.status(graphics, font, Component.translatable(status.translationKey()), imageWidth - 8, titleLabelY, colour);
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
