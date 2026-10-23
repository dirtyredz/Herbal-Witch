package io.github.dirtyredz.herbalwitch.client.stations.shop.widgets;

import io.github.dirtyredz.herbalwitch.client.stations.shop.ShopScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * One tab of the commerce screen. A panel keeps its own selections and typed text across rebuilds,
 * adds its widgets in {@link #build}, draws text and items in {@link #labels} (screen-relative) and
 * item tooltips in {@link #tooltips} (absolute). It reads only the server's projection.
 */
public interface ShopPanel {
    String id();

    void build(ShopScreen screen, int x, int y, CompoundTag snapshot);

    void labels(ShopScreen screen, GuiGraphics graphics, CompoundTag snapshot, int mouseX, int mouseY);

    /** A click at screen-relative coordinates; true when the panel used it. */
    default boolean clicked(ShopScreen screen, double x, double y) {
        return false;
    }

    default void tooltips(ShopScreen screen, GuiGraphics graphics, CompoundTag snapshot, int mouseX, int mouseY) {
    }

    static List<CompoundTag> list(CompoundTag snapshot, String key) {
        List<CompoundTag> out = new ArrayList<>();
        ListTag list = snapshot.getList(key, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) out.add(list.getCompound(i));
        return out;
    }

    static ItemStack stack(CompoundTag entry, String key) {
        return ItemStack.of(entry.getCompound(key));
    }

    static Component text(String key, Object... args) {
        return Component.translatable(ShopScreen.UI + key, args);
    }

    /** An item with its count drawn at screen-relative coordinates. */
    static void item(ShopScreen screen, GuiGraphics graphics, ItemStack stack, int x, int y) {
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(screen.font(), stack, x, y);
    }

    /** Shows the full vanilla tooltip (purity, label, charge) when the mouse is over an item drawn at screen-relative x, y. */
    static void tooltip(ShopScreen screen, GuiGraphics graphics, ItemStack stack, int x, int y, int mouseX, int mouseY) {
        int ax = screen.left() + x, ay = screen.top() + y;
        if (!stack.isEmpty() && mouseX >= ax && mouseX < ax + 16 && mouseY >= ay && mouseY < ay + 16) {
            graphics.renderTooltip(screen.font(), stack, mouseX, mouseY);
        }
    }
}
