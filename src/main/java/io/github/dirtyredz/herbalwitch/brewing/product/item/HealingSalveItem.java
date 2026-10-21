package io.github.dirtyredz.herbalwitch.brewing.product.item;

import io.github.dirtyredz.herbalwitch.brewing.application.ContainerReturns;
import io.github.dirtyredz.herbalwitch.brewing.application.SalveApplication;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/** Rubbed on yourself over 32 ticks when hurt; heals 2 hearts and returns the bowl. */
public class HealingSalveItem extends Item {
    public HealingSalveItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!SalveApplication.canApply(player)) {
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (!(user instanceof Player player) || !SalveApplication.canApply(player)) {
            return stack;
        }
        if (!level.isClientSide) {
            SalveApplication.apply(player, this);
        }
        return ContainerReturns.finish(stack, user, new ItemStack(Items.BOWL));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return SalveApplication.USE_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        ProductTooltips.append(this, lines);
    }
}
