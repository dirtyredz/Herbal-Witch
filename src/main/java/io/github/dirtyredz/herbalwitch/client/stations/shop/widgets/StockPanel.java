package io.github.dirtyredz.herbalwitch.client.stations.shop.widgets;

import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopRequest;
import io.github.dirtyredz.herbalwitch.client.stations.shop.ShopScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Owner stock: the 27 canonical slots (a read-only grid, never a container), the owner's main
 * inventory and hotbar as a picker, explicit deposit and withdrawal amounts, and proceeds with an
 * emerald withdrawal. Armour, offhand, the cursor and other inventories never take part.
 */
public final class StockPanel implements ShopPanel {
    private static final int GRID_X = 8, STOCK_Y = 48, INV_Y = 108;
    private int stockSlot = -1, invSlot = -1;
    private String withdraw = "1", deposit = "1", proceeds = "1";

    @Override
    public String id() {
        return "stock";
    }

    private static ItemStack stock(CompoundTag snapshot, int slot) {
        for (CompoundTag entry : ShopPanel.list(snapshot, "stock")) {
            if (entry.getInt("slot") == slot) {
                ItemStack stack = ItemStack.of(entry);
                return stack.isEmpty() ? stack : stack.copyWithCount(entry.getInt("count"));
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack held(int slot) {
        var player = Minecraft.getInstance().player;
        return player == null ? ItemStack.EMPTY : player.getInventory().items.get(slot);
    }

    @Override
    public void build(ShopScreen screen, int x, int y, CompoundTag snapshot) {
        int rx = screen.left() + 176;
        EditBox w = screen.box(rx, screen.top() + 62, 28, 4, withdraw);
        w.setResponder(v -> withdraw = v);
        screen.button(rx + 30, screen.top() + 62, 38, ShopPanel.text("withdraw"),
                () -> screen.send(ShopRequest.Action.WITHDRAW, null, null, 0, stockSlot, ShopScreen.parse(withdraw, 0), 0, false, "")).active = stockSlot >= 0;
        EditBox d = screen.box(rx, screen.top() + 122, 28, 4, deposit);
        d.setResponder(v -> deposit = v);
        screen.button(rx + 30, screen.top() + 122, 38, ShopPanel.text("deposit"),
                () -> screen.send(ShopRequest.Action.DEPOSIT, null, null, 0, invSlot, ShopScreen.parse(deposit, 0), 0, false, "")).active = invSlot >= 0;
        EditBox p = screen.box(rx, screen.top() + 162, 28, 7, proceeds);
        p.setResponder(v -> proceeds = v);
        screen.button(rx + 30, screen.top() + 162, 38, ShopPanel.text("take"),
                () -> screen.send(ShopRequest.Action.PROCEEDS, null, null, 0, ShopScreen.parse(proceeds, 0), 0, 0, false, ""));
    }

    @Override
    public boolean clicked(ShopScreen screen, double x, double y) {
        int col = (int) Math.floor((x - GRID_X) / 18);
        if (col < 0 || col > 8) return false;
        if (y >= STOCK_Y && y < STOCK_Y + 54) {
            stockSlot = col + 9 * (int) ((y - STOCK_Y) / 18);
            return true;
        }
        if (y >= INV_Y && y < INV_Y + 72) {
            int row = (int) ((y - INV_Y) / 18);
            invSlot = row < 3 ? 9 + row * 9 + col : col;
            return true;
        }
        return false;
    }

    @Override
    public void labels(ShopScreen screen, GuiGraphics graphics, CompoundTag snapshot, int mouseX, int mouseY) {
        var font = screen.font();
        graphics.drawString(font, ShopPanel.text("stock_heading", snapshot.getString("name")), 8, 34, ShopScreen.INK, false);
        for (int i = 0; i < 27; i++) {
            int sx = GRID_X + (i % 9) * 18, sy = STOCK_Y + (i / 9) * 18;
            graphics.blit(ShopScreen.ATLAS, sx, sy, 0, 226, 18, 18);
            ShopPanel.item(screen, graphics, stock(snapshot, i), sx + 1, sy + 1);
            if (i == stockSlot) graphics.renderOutline(sx, sy, 18, 18, 0xFFE0B040);
        }
        graphics.drawString(font, ShopPanel.text("inventory"), 8, INV_Y - 4, ShopScreen.MUTED, false);
        for (int i = 0; i < 36; i++) {
            int row = i < 9 ? 3 : (i - 9) / 9, col = i % 9;
            int sx = GRID_X + col * 18, sy = INV_Y + row * 18 + 2;
            graphics.blit(ShopScreen.ATLAS, sx, sy, 0, 226, 18, 18);
            ShopPanel.item(screen, graphics, held(i), sx + 1, sy + 1);
            if (i == invSlot) graphics.renderOutline(sx, sy, 18, 18, 0xFFE0B040);
        }
        graphics.drawString(font, ShopPanel.text("from_stock"), 176, 50, ShopScreen.MUTED, false);
        graphics.drawString(font, ShopPanel.text("to_stock"), 176, 110, ShopScreen.MUTED, false);
        graphics.drawString(font, ShopPanel.text("proceeds", snapshot.getLong("proceeds"), snapshot.getLong("capacity")), 176, 142, ShopScreen.INK, false);
    }

    @Override
    public void tooltips(ShopScreen screen, GuiGraphics graphics, CompoundTag snapshot, int mouseX, int mouseY) {
        for (int i = 0; i < 27; i++) ShopPanel.tooltip(screen, graphics, stock(snapshot, i), GRID_X + (i % 9) * 18 + 1, STOCK_Y + (i / 9) * 18 + 1, mouseX, mouseY);
        for (int i = 0; i < 36; i++) {
            int row = i < 9 ? 3 : (i - 9) / 9;
            ShopPanel.tooltip(screen, graphics, held(i), GRID_X + (i % 9) * 18 + 1, INV_Y + row * 18 + 3, mouseX, mouseY);
        }
    }
}
