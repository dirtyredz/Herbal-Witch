package io.github.dirtyredz.herbalwitch.client.stations;

import io.github.dirtyredz.herbalwitch.stations.menu.StationMenu;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessStation;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessStatus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * The screen of a processing station (rack, mortar, kettle, infusion pot): its generated atlas,
 * the progress arrow, the heat flame for heated stations and the explicit status (missing input,
 * needs heat, working, output blocked). Purely a view of vanilla-synchronised menu data; it sends
 * nothing to the server.
 *
 * @param <M> the station's menu
 */
public class ProcessStationScreen<M extends StationMenu> extends AbstractContainerScreen<M> {
    /** Where a station's widgets sit on its atlas. {@code flameX < 0}: no heat indicator. */
    public record Layout(ResourceLocation atlas, int arrowX, int arrowY, int flameX, int flameY) {
    }

    private final Layout layout;

    public ProcessStationScreen(M menu, Inventory inventory, Component title, Layout layout) {
        super(menu, inventory, title);
        this.layout = layout;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
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
}
