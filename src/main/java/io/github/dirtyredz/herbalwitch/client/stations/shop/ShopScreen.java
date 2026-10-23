package io.github.dirtyredz.herbalwitch.client.stations.shop;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopMenu;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopPackets;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopRequest;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopSession;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopUpdate;
import io.github.dirtyredz.herbalwitch.client.stations.shop.widgets.BrowsePanel;
import io.github.dirtyredz.herbalwitch.client.stations.shop.widgets.CreatePanel;
import io.github.dirtyredz.herbalwitch.client.stations.shop.widgets.HistoryPanel;
import io.github.dirtyredz.herbalwitch.client.stations.shop.widgets.OffersPanel;
import io.github.dirtyredz.herbalwitch.client.stations.shop.widgets.OrdersPanel;
import io.github.dirtyredz.herbalwitch.client.stations.shop.widgets.SettingsPanel;
import io.github.dirtyredz.herbalwitch.client.stations.shop.widgets.ShopPanel;
import io.github.dirtyredz.herbalwitch.client.stations.shop.widgets.StockPanel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The commerce screen (252 x 200 from {@code textures/gui/shop/shop.png}): tabs chosen by the menu's
 * view and the viewer's role, one panel at a time, and a notice line with the last request's result
 * (an unresolved transfer shows its transaction id, never success). It renders the server's projection
 * and sends bounded intents with the next sequence number; it decides nothing.
 */
public final class ShopScreen extends AbstractContainerScreen<ShopMenu> {
    public static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "textures/gui/shop/shop.png");
    public static final String UI = "shop.herbalwitch.ui.";
    public static final int INK = 0xFF3B2416;
    public static final int ALERT = 0xFFB0305A;
    public static final int MUTED = 0xFF7A6448;
    private final List<ShopPanel> panels = new ArrayList<>();
    private int tab;
    private int seen = -1;
    private int sequence;

    public ShopScreen(ShopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 252;
        imageHeight = 200;
    }

    /** The current projection, or an empty one while the first update is in flight. */
    public CompoundTag snapshot() {
        ShopUpdate update = ShopClientState.latest(menu);
        return update == null ? new CompoundTag() : update.snapshot();
    }

    private List<ShopPanel> panelsFor(CompoundTag snapshot) {
        List<ShopPanel> out = new ArrayList<>();
        if (menu.view() == ShopSession.View.CREATE) {
            out.add(new CreatePanel());
            return out;
        }
        if (snapshot.getBoolean("owner")) {
            out.add(new StockPanel());
            out.add(new OffersPanel());
            out.add(new SettingsPanel());
            out.add(new HistoryPanel());
            out.add(new OrdersPanel());
            out.add(new BrowsePanel());
        } else if (menu.view() == ShopSession.View.ORDERS) {
            out.add(new OrdersPanel());
            out.add(new BrowsePanel());
        } else {
            out.add(new BrowsePanel());
            out.add(new OrdersPanel());
        }
        return out;
    }

    @Override
    protected void init() {
        super.init();
        rebuild();
    }

    private void rebuild() {
        CompoundTag snapshot = snapshot();
        List<ShopPanel> next = panelsFor(snapshot);
        if (next.size() != panels.size() || !sameKinds(next)) {
            panels.clear();
            panels.addAll(next);
            tab = Math.min(tab, panels.size() - 1);
        }
        clearWidgets();
        for (int i = 0; i < panels.size() && panels.size() > 1; i++) {
            int index = i;
            Button b = addRenderableWidget(Button.builder(Component.translatable(UI + "tab." + panels.get(i).id()), x -> {
                tab = index;
                rebuild();
            }).bounds(leftPos + 8 + i * 40, topPos + 10, 39, 14).build());
            b.active = i != tab;
        }
        if (!panels.isEmpty()) panels.get(tab).build(this, leftPos + 8, topPos + 32, snapshot);
        seen = ShopClientState.version();
    }

    /** Rebuilds the current panel (local scrolling and selection). */
    public void refresh() {
        rebuild();
    }

    private boolean sameKinds(List<ShopPanel> next) {
        for (int i = 0; i < next.size(); i++) if (!next.get(i).id().equals(panels.get(i).id())) return false;
        return true;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (seen != ShopClientState.version()) rebuild();
    }

    // Widget helpers for panels.

    public <T extends AbstractWidget> T add(T widget) {
        return addRenderableWidget(widget);
    }

    public Button button(int x, int y, int width, Component label, Runnable action) {
        return addRenderableWidget(Button.builder(label, b -> action.run()).bounds(x, y, width, 14).build());
    }

    public EditBox box(int x, int y, int width, int max, String value) {
        EditBox box = new EditBox(font, x, y, width, 14, Component.empty());
        box.setMaxLength(max);
        box.setValue(value);
        return addRenderableWidget(box);
    }

    public net.minecraft.client.gui.Font font() {
        return font;
    }

    /** Sends one intent with the session's next sequence number. */
    public void send(ShopRequest.Action action, UUID target, UUID target2, long revision, int a, int b, int c, boolean flag, String text) {
        sequence++;
        ShopPackets.request(new ShopRequest(menu.containerId, menu.nonce(), sequence, action, target == null ? ShopRequest.NONE : target,
                target2 == null ? ShopRequest.NONE : target2, revision, a, b, c, flag, text));
    }

    public void send(ShopRequest.Action action) {
        send(action, null, null, 0, 0, 0, 0, false, "");
    }

    public static int parse(String text, int fallback) {
        try {
            return Integer.parseInt(text.strip());
        } catch (NumberFormatException invalid) {
            return fallback;
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(ATLAS, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        CompoundTag snapshot = snapshot();
        if (!panels.isEmpty()) panels.get(tab).labels(this, graphics, snapshot, mouseX - leftPos, mouseY - topPos);
        ShopUpdate update = ShopClientState.latest(menu);
        if (update != null && update.result() != null) {
            Component line = Component.translatable(update.result().key());
            if (update.result().transaction() != null) line = Component.translatable(UI + "transaction", line, update.result().transaction().toString());
            graphics.drawString(font, font.plainSubstrByWidth(line.getString(), imageWidth - 16), 8, imageHeight - 16,
                    update.result().success() ? INK : ALERT, false);
        }
        if (snapshot.getBoolean("blocked")) graphics.blit(ATLAS, imageWidth - 18, imageHeight - 17, 40, 226, 9, 9);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (!panels.isEmpty()) panels.get(tab).tooltips(this, graphics, snapshot(), mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == 0 && !panels.isEmpty() && panels.get(tab).clicked(this, mouseX - leftPos, mouseY - topPos)) {
            rebuild();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int key, int scan, int modifiers) {
        // Typing in a text box must not close the screen with the inventory key.
        if (getFocused() instanceof EditBox box && box.isFocused() && key != 256) return box.keyPressed(key, scan, modifiers) || box.canConsumeInput();
        return super.keyPressed(key, scan, modifiers);
    }

    public int left() {
        return leftPos;
    }

    public int top() {
        return topPos;
    }
}
