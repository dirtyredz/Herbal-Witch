package io.github.dirtyredz.herbalwitch.client.stations.shop.widgets;

import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopRequest;
import io.github.dirtyredz.herbalwitch.client.stations.shop.ShopScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;

/**
 * Owner settings and fixtures. Left: open/close, public buying, villager customers, rename, bans and
 * retirement. Right: arm a link (the menu closes; the next click on a counter, display or board links
 * it), the linked fixtures, and which offers a selected display shows.
 */
public final class SettingsPanel implements ShopPanel {
    private static final String[] MODES = {"link_counter", "link_display", "link_board", "link_unlink"};
    private String name = "", ban = "";
    private int link = -1, slot, offer;

    @Override
    public String id() {
        return "settings";
    }

    @Override
    public void build(ShopScreen screen, int x, int y, CompoundTag snapshot) {
        boolean open = "OPEN".equals(snapshot.getString("lifecycle"));
        screen.button(x, y + 14, 56, ShopPanel.text(open ? "close" : "open"), () -> screen.send(open ? ShopRequest.Action.CLOSE : ShopRequest.Action.OPEN));
        boolean publicBuying = snapshot.getBoolean("publicBuying");
        screen.button(x + 60, y + 14, 56, ShopPanel.text(publicBuying ? "public_on" : "public_off"),
                () -> screen.send(ShopRequest.Action.PUBLIC, null, null, 0, 0, 0, 0, !publicBuying, ""));
        boolean npc = snapshot.getBoolean("npcService");
        screen.button(x, y + 30, 56, ShopPanel.text(npc ? "villagers_on" : "villagers_off"),
                () -> screen.send(ShopRequest.Action.NPC, null, null, 0, 0, 0, 0, !npc, ""));
        screen.button(x + 60, y + 30, 56, ShopPanel.text("retire"), () -> screen.send(ShopRequest.Action.RETIRE));
        if (name.isEmpty()) name = snapshot.getString("name");
        EditBox rename = screen.box(x, y + 48, 72, 32, name);
        rename.setResponder(v -> name = v);
        screen.button(x + 74, y + 48, 42, ShopPanel.text("rename"), () -> screen.send(ShopRequest.Action.RENAME, null, null, 0, 0, 0, 0, false, name.strip()));
        EditBox banBox = screen.box(x, y + 66, 72, 16, ban);
        banBox.setResponder(v -> ban = v);
        screen.button(x + 74, y + 66, 42, ShopPanel.text("ban"), () -> screen.send(ShopRequest.Action.BAN, null, null, 0, 0, 0, 0, false, ban.strip()));
        var bans = snapshot.getList("bans", Tag.TAG_INT_ARRAY);
        for (int i = 0; i < Math.min(3, bans.size()); i++) {
            UUID banned = NbtUtils.loadUUID(bans.get(i));
            screen.button(x + 74, y + 96 + i * 15, 42, ShopPanel.text("unban"), () -> screen.send(ShopRequest.Action.UNBAN, banned, null, 0, 0, 0, 0, false, ""));
        }
        int rx = x + 122;
        for (int i = 0; i < MODES.length; i++) {
            int mode = i;
            screen.button(rx + (i % 2) * 58, y + 14 + (i / 2) * 16, 56, ShopPanel.text(MODES[i]),
                    () -> screen.send(ShopRequest.Action.LINK, null, null, 0, mode, 0, 0, false, ""));
        }
        List<CompoundTag> links = ShopPanel.list(snapshot, "links");
        if (link >= links.size()) link = -1;
        List<CompoundTag> offers = ShopPanel.list(snapshot, "offers");
        if (link >= 0) {
            int index = link;
            screen.button(rx + 84, y + 114, 30, ShopPanel.text("unlink"), () -> screen.send(ShopRequest.Action.UNLINK, null, null, 0, index, 0, 0, false, ""));
        }
        if (link >= 0 && isDisplay(links.get(link))) {
            int slots = "SHELF".equals(links.get(link).getString("kind")) ? 3 : 1;
            slot = Math.min(slot, slots - 1);
            offer = Math.min(offer, offers.size());
            screen.button(rx, y + 132, 30, ShopPanel.text("slot", slot + 1), () -> {
                slot = (slot + 1) % slots;
                screen.refresh();
            });
            Component chosen = offer < offers.size() ? ShopPanel.stack(offers.get(offer), "stack").getHoverName() : ShopPanel.text("none");
            screen.button(rx + 32, y + 132, 50, Component.literal(screen.font().plainSubstrByWidth(chosen.getString(), 46)), () -> {
                offer = (offer + 1) % (offers.size() + 1);
                screen.refresh();
            });
            UUID target = offer < offers.size() ? offers.get(offer).getUUID("id") : null;
            int index = link;
            screen.button(rx + 84, y + 132, 30, ShopPanel.text("assign"),
                    () -> screen.send(ShopRequest.Action.DISPLAY_ASSIGN, target, null, 0, index, slot, 0, false, ""));
        }
    }

    private static boolean isDisplay(CompoundTag link) {
        return "SHELF".equals(link.getString("kind")) || "JAR".equals(link.getString("kind"));
    }

    @Override
    public boolean clicked(ShopScreen screen, double x, double y) {
        if (x < 130 || x > 244 || y < 82 || y >= 82 + 6 * 10) return false;
        int row = (int) ((y - 82) / 10);
        link = row < ShopPanel.list(screen.snapshot(), "links").size() ? row : -1;
        return true;
    }

    @Override
    public void labels(ShopScreen screen, GuiGraphics graphics, CompoundTag snapshot, int mouseX, int mouseY) {
        var font = screen.font();
        graphics.drawString(font, ShopPanel.text("settings_heading",
                ShopPanel.text("lifecycle." + snapshot.getString("lifecycle").toLowerCase(java.util.Locale.ROOT))), 8, 34, ShopScreen.INK, false);
        var bans = snapshot.getList("bans", Tag.TAG_INT_ARRAY);
        graphics.drawString(font, ShopPanel.text("bans", bans.size()), 8, 118, ShopScreen.MUTED, false);
        for (int i = 0; i < Math.min(3, bans.size()); i++) {
            graphics.drawString(font, NbtUtils.loadUUID(bans.get(i)).toString().substring(0, 8), 8, 131 + i * 15, ShopScreen.INK, false);
        }
        List<CompoundTag> links = ShopPanel.list(snapshot, "links");
        graphics.drawString(font, ShopPanel.text("links", links.size()), 130, 70, ShopScreen.MUTED, false);
        for (int i = 0; i < Math.min(6, links.size()); i++) {
            CompoundTag l = links.get(i);
            int[] p = l.getIntArray("pos");
            String line = ShopPanel.text("kind." + l.getString("kind").toLowerCase(java.util.Locale.ROOT)).getString()
                    + (p.length == 3 ? " " + p[0] + "," + p[1] + "," + p[2] : "")
                    + (isDisplay(l) ? " (" + l.getList("offers", Tag.TAG_INT_ARRAY).size() + ")" : "");
            graphics.drawString(font, font.plainSubstrByWidth(line, 112), 130, 82 + i * 10, i == link ? ShopScreen.ALERT : ShopScreen.INK, false);
        }
    }
}
