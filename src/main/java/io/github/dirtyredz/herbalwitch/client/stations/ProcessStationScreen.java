package io.github.dirtyredz.herbalwitch.client.stations;

import io.github.dirtyredz.herbalwitch.client.grimoire.GrimoirePanelHost;
import io.github.dirtyredz.herbalwitch.client.stations.widgets.MagicalProcessPanel;
import io.github.dirtyredz.herbalwitch.apothecary.stations.block.StationMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessStation;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessStatus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;


import java.util.Optional;

/**
 * The screen of a processing station: its generated atlas, the progress arrow, the heat flame for
 * heated stations and the explicit status (waiting, needs a witch, needs its witch nearby, needs
 * energy, needs heat, output full, working). A view of vanilla-synchronised menu data; while a witch
 * recipe is matched the {@link MagicalProcessPanel} beside it offers Start, Resume and Cancel (vanilla
 * menu-button clicks). A "Grimoire" button opens the book on the other side on the station's category.
 *
 * @param <M> the station's menu
 */
public class ProcessStationScreen<M extends StationMenu> extends GrimoirePanelHost<M> {
    /**
     * Where a station's widgets sit on its atlas. {@code flameX < 0}: no heat indicator.
     * {@code category}: the processing category its grimoire panel lists.
     */
    public record Layout(ResourceLocation atlas, int arrowX, int arrowY, int flameX, int flameY, String category) {
    }

    private final Layout layout;
    private final MagicalProcessPanel magic;

    public ProcessStationScreen(M menu, Inventory inventory, Component title, Layout layout) {
        super(menu, inventory, title, Optional.of(layout.category()), Optional.empty(), true);
        this.layout = layout;
        this.magic = new MagicalProcessPanel(menu);
    }

    @Override
    protected void init() {
        super.init();
        initGrimoire();
        magic.init(leftPos, topPos, imageWidth, this::addRenderableWidget);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        magic.render(graphics, font);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderGrimoire(graphics, mouseX, mouseY, partialTick);
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
            case NEEDS_HEAT, OUTPUT_BLOCKED, NEEDS_WITCH, NEEDS_OPERATOR, NEEDS_ENERGY -> 0xB0305A;
            default -> 0x404040;
        };
        ProcessWidgets.status(graphics, font, Component.translatable(status.translationKey()), imageWidth - 8, titleLabelY, colour);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        magic.update();
    }


}
