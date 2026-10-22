package io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A cauldron mixture: exactly four doses of one preparation, an ingredient for the bottling bench and
 * nothing else (it cannot be drunk or thrown). One shared class for every mixture item; the item's
 * registry id says which preparation it holds. Stacks to 16.
 */
public class PreparedMixtureItem extends Item {
    public PreparedMixtureItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        ProductTooltips.append(this, lines);
    }
}
