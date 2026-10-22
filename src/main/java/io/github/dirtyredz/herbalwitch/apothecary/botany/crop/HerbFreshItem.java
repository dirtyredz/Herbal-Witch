package io.github.dirtyredz.herbalwitch.apothecary.botany.crop;

import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** A freshly harvested herb: an ingredient, not food (toxic herbs included). Shows its traits. */
public class HerbFreshItem extends Item {
    private final HerbSpec herb;

    public HerbFreshItem(HerbSpec herb, Properties properties) {
        super(properties);
        this.herb = herb;
    }

    public HerbSpec herb() {
        return herb;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        HerbTooltips.append(herb, lines);
    }
}
