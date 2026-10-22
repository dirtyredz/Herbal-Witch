package io.github.dirtyredz.herbalwitch.brewing.product.item;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.brewing.application.ContainerReturns;
import io.github.dirtyredz.herbalwitch.brewing.application.advanced.OilApplications;
import io.github.dirtyredz.herbalwitch.brewing.product.metadata.PreparationMetadata;
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

/**
 * A Milestone 7 oil, dabbed on over 32 ticks; once finished, server side, it applies once and the
 * bottle comes back. Venom oil coats the eligible blade in the off hand: without one it is refused
 * before and after use and nothing is consumed (the player is told why).
 */
public class AdvancedOilItem extends Item {
    public static final String NO_WEAPON = "item." + HerbalWitch.MOD_ID + ".preparations.oil.no_weapon";

    private final OilApplications.Oil oil;

    public AdvancedOilItem(Properties properties, OilApplications.Oil oil) {
        super(properties);
        this.oil = oil;
    }

    public OilApplications.Oil oil() {
        return oil;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND && oil == OilApplications.Oil.VENOM || !OilApplications.usable(player, oil)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable(NO_WEAPON), true);
            }
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (!(user instanceof Player player) || !OilApplications.usable(player, oil)) {
            return stack;
        }
        if (!level.isClientSide) {
            OilApplications.apply(player, oil);
        }
        return ContainerReturns.finish(stack, user, new ItemStack(Items.GLASS_BOTTLE));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BRUSH;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        ProductTooltips.append(this, lines);
        PreparationMetadata.appendLabel(stack, lines);
    }
}
