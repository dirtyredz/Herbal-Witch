package io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.List;

/** The one-line description every product carries ({@code <item key>.tooltip}). */
public final class ProductTooltips {
    private ProductTooltips() {
    }

    public static String key(Item item) {
        return item.getDescriptionId() + ".tooltip";
    }

    public static void append(Item item, List<Component> lines) {
        lines.add(Component.translatable(key(item)).withStyle(ChatFormatting.GRAY));
    }
}
