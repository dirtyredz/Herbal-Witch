package io.github.dirtyredz.herbalwitch.client.magic.ward;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.client.grimoire.GrimoirePanelHost;
import io.github.dirtyredz.herbalwitch.magic.ward.catalog.WardCatalog;
import io.github.dirtyredz.herbalwitch.magic.ward.grimoire.WardEntries;
import io.github.dirtyredz.herbalwitch.magic.ward.menu.WardMenu;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WorkingRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Optional;

/**
 * A ward stone's screen (200 x 150 from {@code textures/gui/witch/ward.png}): its state, whose it is
 * and the charge left above the divider; the owner's settings (radius within the current maximum,
 * the target profile) and Activate, Deactivate and Release below. A view of the menu's data; buttons
 * are vanilla menu-button clicks and the server decides. Closing it shows the boundary briefly
 * ({@link WardBoundaryPreview}).
 */
public class WardScreen extends GrimoirePanelHost<WardMenu> {
    public static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "textures/gui/witch/ward.png");
    private static final String UI = "ward.herbalwitch.ui.";
    private static final int INK = 0xFF3B2416;
    private static final int ALERT = 0xFFB0305A;

    private Button activate;
    private Button deactivate;
    private Button release;
    private Button down;
    private Button up;
    private Button profile;

    public WardScreen(WardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, Optional.empty(), Optional.of(WardEntries.KEY), false);
        imageWidth = 200;
        imageHeight = 150;
    }

    @Override
    protected void init() {
        super.init();
        initGrimoire();
        int x = leftPos + 8;
        int y = topPos + 82;
        down = button("radius_down", x + 110, y, 18, WardMenu.Button.RADIUS_DOWN);
        up = button("radius_up", x + 132, y, 18, WardMenu.Button.RADIUS_UP);
        profile = button("profile", x + 110, y + 18, 74, WardMenu.Button.PROFILE);
        activate = button("activate", x, y + 40, 58, WardMenu.Button.ACTIVATE);
        deactivate = button("deactivate", x + 62, y + 40, 58, WardMenu.Button.DEACTIVATE);
        release = button("release", x + 124, y + 40, 60, WardMenu.Button.RELEASE);
        updateButtons();
    }

    private Button button(String key, int x, int y, int width, WardMenu.Button which) {
        return addRenderableWidget(Button.builder(Component.translatable(UI + key), b -> click(which)).bounds(x, y, width, 14).build());
    }

    private void click(WardMenu.Button which) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode != null) {
            mc.gameMode.handleInventoryButtonClick(menu.containerId, which.id());
        }
    }

    private void updateButtons() {
        int owner = menu.data(WardMenu.DATA_OWNER);
        int state = menu.data(WardMenu.DATA_STATE);
        boolean mine = owner == 1;
        boolean charged = state == 1 || state == 2;
        activate.active = !charged && owner != 2 && state != 3;
        deactivate.active = mine && charged;
        release.active = mine;
        down.active = mine;
        up.active = mine;
        profile.active = mine;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtons();
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
        int total = Math.max(1, WitchRules.current().workings().wardChargeSeconds());
        int left = menu.data(WardMenu.DATA_SECONDS);
        graphics.blit(ATLAS, leftPos + 8, topPos + 58, 0, 154, 120, 8);
        graphics.blit(ATLAS, leftPos + 10, topPos + 60, 0, 164, Math.min(116, left * 116 / total), 4);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, INK, false);
        WorkingRules w = WitchRules.current().workings();
        int state = menu.data(WardMenu.DATA_STATE);
        int owner = menu.data(WardMenu.DATA_OWNER);
        int y = 18;
        y += text(graphics, Component.translatable(UI + "state." + state), 8, y, state == 2 || state == 3 ? ALERT : INK);
        y += text(graphics, Component.translatable(UI + "owner." + owner), 8, y, INK);
        int left = menu.data(WardMenu.DATA_SECONDS);
        text(graphics, Component.translatable(UI + "charge", left / 60, left % 60), 132, 57, INK);
        text(graphics, Component.translatable(UI + "radius", menu.data(WardMenu.DATA_RADIUS), menu.data(WardMenu.DATA_MAX_RADIUS), w.wardHeight()),
                8, 85, INK);
        text(graphics, Component.translatable(UI + "profile_is", Component.translatable(UI + "profile."
                + WardCatalog.Profile.byOrdinal(menu.data(WardMenu.DATA_PROFILE)).key())), 8, 103, INK);
        text(graphics, Component.translatable(UI + "cost", w.wardActivationCost(), w.wardChargeSeconds() / 60), 8, 140, INK);
    }

    private int text(GuiGraphics graphics, Component text, int x, int y, int color) {
        List<FormattedCharSequence> lines = font.split(text, imageWidth - x - 8);
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(font, lines.get(i), x, y + i * 10, color, false);
        }
        return lines.size() * 10;
    }

    @Override
    public void removed() {
        super.removed();
        WardBoundaryPreview.show(menu.pos(), Math.min(menu.data(WardMenu.DATA_RADIUS), menu.data(WardMenu.DATA_MAX_RADIUS)),
                WitchRules.current().workings().wardHeight());
    }

}
