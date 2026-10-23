package io.github.dirtyredz.herbalwitch.client.stations.shop.widgets;

import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopRequest;
import io.github.dirtyredz.herbalwitch.client.stations.shop.ShopScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Public browsing: the shop's name and owner, its open state, and each offer's exact sample (hover
 * for purity, label and charge), lot, price and availability with Buy. Buying sends the offer id and
 * the revision shown; a changed price or product is refused by the server rather than bought.
 */
public final class BrowsePanel implements ShopPanel {
    static final int ROWS = 6;
    private int scroll;

    @Override
    public String id() {
        return "browse";
    }

    static List<CompoundTag> offers(CompoundTag snapshot) {
        List<CompoundTag> all = ShopPanel.list(snapshot, "offers");
        if (!snapshot.contains("focus")) return all;
        List<UUID> focus = new ArrayList<>();
        snapshot.getList("focus", Tag.TAG_INT_ARRAY).forEach(t -> focus.add(NbtUtils.loadUUID(t)));
        return all.stream().filter(o -> focus.contains(o.getUUID("id"))).toList();
    }

    @Override
    public void build(ShopScreen screen, int x, int y, CompoundTag snapshot) {
        List<CompoundTag> offers = offers(snapshot);
        scroll = Math.max(0, Math.min(scroll, offers.size() - ROWS));
        boolean customer = !snapshot.getBoolean("owner") && !snapshot.getBoolean("banned");
        for (int i = 0; i < Math.min(ROWS, offers.size() - scroll); i++) {
            CompoundTag offer = offers.get(scroll + i);
            var buy = screen.button(x + 192, y + 16 + i * 20 + 3, 42, ShopPanel.text("buy"),
                    () -> screen.send(ShopRequest.Action.BUY, offer.getUUID("id"), null, offer.getLong("revision"), 0, 0, 0, false, ""));
            buy.active = customer && offer.getBoolean("available");
        }
        if (offers.size() > ROWS) {
            screen.button(x + 192, y, 20, Component.literal("<"), () -> { scroll = Math.max(0, scroll - ROWS); screen.refresh(); });
            screen.button(x + 214, y, 20, Component.literal(">"), () -> { scroll += ROWS; screen.refresh(); });
        }
    }

    @Override
    public void labels(ShopScreen screen, GuiGraphics graphics, CompoundTag snapshot, int mouseX, int mouseY) {
        var font = screen.font();
        if (snapshot.contains("problem")) {
            graphics.drawString(font, ShopPanel.text("problem." + snapshot.getString("problem")), 8, 34, ShopScreen.ALERT, false);
            return;
        }
        graphics.drawString(font, ShopPanel.text("heading", snapshot.getString("name"), snapshot.getString("ownerName")), 8, 34, ShopScreen.INK, false);
        String state = snapshot.getBoolean("blocked") ? "recovery" : !snapshot.getBoolean("registerLoaded") ? "unavailable"
                : snapshot.getString("lifecycle").toLowerCase(java.util.Locale.ROOT);
        graphics.drawString(font, ShopPanel.text("state." + state), 150, 34, ShopScreen.MUTED, false);
        List<CompoundTag> offers = offers(snapshot);
        if (offers.isEmpty()) graphics.drawString(font, ShopPanel.text("no_offers"), 8, 52, ShopScreen.MUTED, false);
        for (int i = 0; i < Math.min(ROWS, offers.size() - scroll); i++) {
            CompoundTag offer = offers.get(scroll + i);
            int ry = 48 + i * 20;
            graphics.blit(ShopScreen.ATLAS, 8, ry, 0, 204, 182, 20);
            var stack = ShopPanel.stack(offer, "stack");
            ShopPanel.item(screen, graphics, stack, 9, ry + 1);
            graphics.drawString(font, font.plainSubstrByWidth(ShopPanel.text("lot", offer.getInt("lot"), stack.getHoverName()).getString(), 112), 28, ry + 2, ShopScreen.INK, false);
            graphics.drawString(font, ShopPanel.text("price", offer.getInt("price")), 28, ry + 11, ShopScreen.MUTED, false);
            graphics.blit(ShopScreen.ATLAS, 176, ry + 5, offer.getBoolean("available") ? 20 : 30, 226, 9, 9);
        }
    }

    @Override
    public void tooltips(ShopScreen screen, GuiGraphics graphics, CompoundTag snapshot, int mouseX, int mouseY) {
        List<CompoundTag> offers = offers(snapshot);
        for (int i = 0; i < Math.min(ROWS, offers.size() - scroll); i++) {
            ShopPanel.tooltip(screen, graphics, ShopPanel.stack(offers.get(scroll + i), "stack"), 9, 49 + i * 20, mouseX, mouseY);
        }
    }
}
