package io.github.dirtyredz.herbalwitch.apothecary.botany.foraging;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolActions;

/**
 * Who may forage extra drops: a player breaking a block of the given tag by hand or with an
 * ordinary tool. Shears and Silk Touch collect the block itself instead, and explosions, water,
 * pistons or mobs never forage.
 */
public final class ForageEligibility {
    private ForageEligibility() {
    }

    public static boolean isPlayerHarvest(LootContext context, TagKey<Block> sources) {
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        Entity breaker = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (state == null || !state.is(sources) || !(breaker instanceof Player)) {
            return false;
        }
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        return tool == null || !collectsWhole(tool);
    }

    static boolean collectsWhole(ItemStack tool) {
        return tool.is(Items.SHEARS) || tool.is(Tags.Items.SHEARS) || tool.canPerformAction(ToolActions.SHEARS_DIG)
                || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;
    }
}
