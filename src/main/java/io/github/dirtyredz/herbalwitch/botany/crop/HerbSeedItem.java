package io.github.dirtyredz.herbalwitch.botany.crop;

import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Herb seeds: they plant the herb's age-0 plant, but only on tilled farmland or a herb bed. Wild
 * patches may stand on natural soil; a player's planting may not.
 */
public class HerbSeedItem extends ItemNameBlockItem {
    private final HerbSpec herb;

    public HerbSeedItem(HerbSpec herb, Block plant, Properties properties) {
        super(plant, properties);
        this.herb = herb;
    }

    public HerbSpec herb() {
        return herb;
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        BlockState below = context.getLevel().getBlockState(context.getClickedPos().below());
        return HerbGrowth.isCultivatedSoil(below) && super.canPlace(context, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        HerbTooltips.append(herb, lines);
    }
}
