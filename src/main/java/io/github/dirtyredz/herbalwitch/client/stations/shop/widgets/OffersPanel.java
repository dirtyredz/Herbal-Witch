package io.github.dirtyredz.herbalwitch.client.stations.shop.widgets;

import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopRequest;
import io.github.dirtyredz.herbalwitch.client.stations.shop.ShopScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;

/**
 * Owner offers: up to twelve, each an exact stock sample's product with a lot and whole-emerald price.
 * Select a row to edit it, or leave none selected to create one from a stock slot. Every saved change
 * makes a new revision on the server; buyers holding the old one are refused, not overcharged.
 */
public final class OffersPanel implements ShopPanel {
    private static final int ROWS = 5, TOP = 48;
    private UUID selected;
    private String sample = "1", lot = "1", price = "1";
    private boolean enabled = true;
    private int scroll;

    @Override
    public String id() {
        return "offers";
    }

    @Override
    public void build(ShopScreen screen, int x, int y, CompoundTag snapshot) {
        List<CompoundTag> offers = ShopPanel.list(snapshot, "offers");
        if (selected != null && offers.stream().noneMatch(o -> o.getUUID("id").equals(selected))) selected = null;
        scroll = Math.max(0, Math.min(scroll, offers.size() - ROWS));
        if (offers.size() > ROWS) {
            screen.button(x + 192, y, 20, Component.literal("<"), () -> { scroll = Math.max(0, scroll - ROWS); screen.refresh(); });
            screen.button(x + 214, y, 20, Component.literal(">"), () -> { scroll += ROWS; screen.refresh(); });
        }
        int by = screen.top() + 152;
        EditBox s = screen.box(x, by, 26, 2, sample);
        s.setResponder(v -> sample = v);
        EditBox l = screen.box(x + 30, by, 26, 2, lot);
        l.setResponder(v -> lot = v);
        EditBox p = screen.box(x + 60, by, 26, 2, price);
        p.setResponder(v -> price = v);
        screen.button(x + 90, by, 44, ShopPanel.text(enabled ? "enabled" : "disabled"), () -> {
            enabled = !enabled;
            screen.refresh();
        });
        screen.button(x + 138, by, 46, ShopPanel.text(selected == null ? "add" : "save"), () -> screen.send(ShopRequest.Action.OFFER_SAVE, selected, null, 0,
                ShopScreen.parse(sample, 0) - 1, ShopScreen.parse(lot, 0), ShopScreen.parse(price, 0), enabled, ""));
        screen.button(x + 188, by, 46, ShopPanel.text("remove"), () -> screen.send(ShopRequest.Action.OFFER_REMOVE, selected, null, 0, 0, 0, 0, false, ""))
                .active = selected != null;
    }

    @Override
    public boolean clicked(ShopScreen screen, double x, double y) {
        if (x < 8 || x > 200 || y < TOP || y >= TOP + ROWS * 20) return false;
        List<CompoundTag> offers = ShopPanel.list(screen.snapshot(), "offers");
        int row = scroll + (int) ((y - TOP) / 20);
        if (row >= offers.size()) {
            selected = null;
            return true;
        }
        CompoundTag offer = offers.get(row);
        UUID id = offer.getUUID("id");
        selected = id.equals(selected) ? null : id;
        if (selected != null) {
            lot = String.valueOf(offer.getInt("lot"));
            price = String.valueOf(offer.getInt("price"));
            enabled = offer.getBoolean("enabled");
        }
        return true;
    }

    @Override
    public void labels(ShopScreen screen, GuiGraphics graphics, CompoundTag snapshot, int mouseX, int mouseY) {
        var font = screen.font();
        List<CompoundTag> offers = ShopPanel.list(snapshot, "offers");
        graphics.drawString(font, ShopPanel.text("offers_heading", offers.size(), 12), 8, 34, ShopScreen.INK, false);
        for (int i = 0; i < Math.min(ROWS, offers.size() - scroll); i++) {
            CompoundTag offer = offers.get(scroll + i);
            int ry = TOP + i * 20;
            graphics.blit(ShopScreen.ATLAS, 8, ry, 0, 204, 232, 20);
            var stack = ShopPanel.stack(offer, "stack");
            ShopPanel.item(screen, graphics, stack, 9, ry + 1);
            graphics.drawString(font, font.plainSubstrByWidth(ShopPanel.text("lot", offer.getInt("lot"), stack.getHoverName()).getString(), 120), 28, ry + 2, ShopScreen.INK, false);
            graphics.drawString(font, ShopPanel.text("offer_detail", offer.getInt("price"), offer.getInt("stocked"),
                    ShopPanel.text(offer.getBoolean("enabled") ? "enabled" : "disabled")), 28, ry + 11, ShopScreen.MUTED, false);
            if (offer.getUUID("id").equals(selected)) graphics.renderOutline(8, ry, 232, 20, 0xFFE0B040);
        }
        graphics.drawString(font, ShopPanel.text("offer_editor"), 8, 142, ShopScreen.MUTED, false);
    }

    @Override
    public void tooltips(ShopScreen screen, GuiGraphics graphics, CompoundTag snapshot, int mouseX, int mouseY) {
        List<CompoundTag> offers = ShopPanel.list(snapshot, "offers");
        for (int i = 0; i < Math.min(ROWS, offers.size() - scroll); i++) {
            ShopPanel.tooltip(screen, graphics, ShopPanel.stack(offers.get(scroll + i), "stack"), 9, TOP + i * 20 + 1, mouseX, mouseY);
        }
        int bx = screen.left() + 8, by = screen.top() + 152;
        if (mouseY >= by && mouseY < by + 14 && mouseX >= bx && mouseX < bx + 86) {
            graphics.renderTooltip(screen.font(), Component.translatable(ShopScreen.UI + "offer_fields"), mouseX, mouseY);
        }
    }
}
