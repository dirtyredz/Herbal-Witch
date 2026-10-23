package io.github.dirtyredz.herbalwitch.client.stations.shop.widgets;

import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopRequest;
import io.github.dirtyredz.herbalwitch.client.stations.shop.ShopScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;

/** An unbound register: name and create a shop (initiated witches), or reconnect one of your detached shops. */
public final class CreatePanel implements ShopPanel {
    private String name = "";

    @Override
    public String id() {
        return "create";
    }

    @Override
    public void build(ShopScreen screen, int x, int y, CompoundTag snapshot) {
        EditBox box = screen.box(x, y + 26, 150, 32, name);
        box.setResponder(value -> name = value);
        var create = screen.button(x + 156, y + 26, 80, ShopPanel.text("create"), () -> screen.send(ShopRequest.Action.CREATE, null, null, 0, 0, 0, 0, false, name.strip()));
        create.active = snapshot.getBoolean("witch") && snapshot.getInt("owned") < snapshot.getInt("ownerLimit");
        int row = 0;
        for (CompoundTag detached : ShopPanel.list(snapshot, "detached")) {
            if (row >= 5) break;
            screen.button(x + 156, y + 72 + row * 16, 80, ShopPanel.text("reconnect"),
                    () -> screen.send(ShopRequest.Action.RECONNECT, detached.getUUID("id"), null, 0, 0, 0, 0, false, ""));
            row++;
        }
    }

    @Override
    public void labels(ShopScreen screen, GuiGraphics graphics, CompoundTag snapshot, int mouseX, int mouseY) {
        var font = screen.font();
        graphics.drawString(font, ShopPanel.text("unbound"), 8, 34, ShopScreen.INK, false);
        graphics.drawString(font, ShopPanel.text(snapshot.getBoolean("witch") ? "create_hint" : "create_witch_only"), 8, 46,
                snapshot.getBoolean("witch") ? ShopScreen.MUTED : ShopScreen.ALERT, false);
        graphics.drawString(font, ShopPanel.text("owned", snapshot.getInt("owned"), snapshot.getInt("ownerLimit")), 8, 76, ShopScreen.MUTED, false);
        graphics.drawString(font, ShopPanel.text("detached"), 8, 92, ShopScreen.INK, false);
        int row = 0;
        for (CompoundTag detached : ShopPanel.list(snapshot, "detached")) {
            if (row >= 5) break;
            graphics.drawString(font, detached.getString("name"), 8, 108 + row * 16, ShopScreen.INK, false);
            row++;
        }
    }
}
