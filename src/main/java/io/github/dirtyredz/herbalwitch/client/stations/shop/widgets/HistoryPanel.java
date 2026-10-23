package io.github.dirtyredz.herbalwitch.client.stations.shop.widgets;

import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopRequest;
import io.github.dirtyredz.herbalwitch.client.stations.shop.ShopScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Owner-only economic history (newest first, 32 per page from the server) and the identity and reason
 * of any retained transfer, so an interrupted transfer shows as unavailable, never as success.
 */
public final class HistoryPanel implements ShopPanel {
    private static final int SHOWN = 10;

    @Override
    public String id() {
        return "history";
    }

    @Override
    public void build(ShopScreen screen, int x, int y, CompoundTag snapshot) {
        int page = snapshot.getInt("historyPage"), pages = Math.max(1, snapshot.getInt("historyPages"));
        screen.button(x + 192, y, 20, Component.literal("<"), () -> screen.send(ShopRequest.Action.HISTORY_PAGE, null, null, 0, page - 1, 0, 0, false, ""))
                .active = page > 0;
        screen.button(x + 214, y, 20, Component.literal(">"), () -> screen.send(ShopRequest.Action.HISTORY_PAGE, null, null, 0, page + 1, 0, 0, false, ""))
                .active = page + 1 < pages;
    }

    @Override
    public void labels(ShopScreen screen, GuiGraphics graphics, CompoundTag snapshot, int mouseX, int mouseY) {
        var font = screen.font();
        graphics.drawString(font, ShopPanel.text("history_heading", snapshot.getInt("historyPage") + 1, Math.max(1, snapshot.getInt("historyPages"))),
                8, 34, ShopScreen.INK, false);
        int y = 48;
        for (CompoundTag t : ShopPanel.list(snapshot, "recovery")) {
            graphics.blit(ShopScreen.ATLAS, 8, y, 40, 226, 9, 9);
            graphics.drawString(font, font.plainSubstrByWidth(ShopPanel.text("recovery_line", t.getString("kind"), t.getString("phase"),
                    t.getUUID("id").toString(), t.getString("reason")).getString(), 220), 20, y, ShopScreen.ALERT, false);
            y += 11;
        }
        List<CompoundTag> history = ShopPanel.list(snapshot, "history");
        for (int i = 0; i < Math.min(SHOWN, history.size()); i++) {
            CompoundTag e = history.get(i);
            Component line = ShopPanel.text("history." + e.getString("type").toLowerCase(java.util.Locale.ROOT), e.getString("actor"),
                    e.getInt("quantity"), e.getString("product"), e.getLong("emeralds"));
            graphics.drawString(font, font.plainSubstrByWidth(line.getString(), 232), 8, y + i * 11, ShopScreen.INK, false);
        }
    }
}
