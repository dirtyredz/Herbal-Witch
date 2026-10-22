package io.github.dirtyredz.herbalwitch.brewing.product.item;

import io.github.dirtyredz.herbalwitch.brewing.application.ContainerReturns;
import io.github.dirtyredz.herbalwitch.brewing.application.advanced.DrinkApplications;
import io.github.dirtyredz.herbalwitch.brewing.application.advanced.DrinkApplications.Draught;
import io.github.dirtyredz.herbalwitch.brewing.product.metadata.PreparationMetadata;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
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
 * A bottled utility draught, drunk like a potion (32 ticks) by anyone; once finished, server side, its
 * application runs once and the glass bottle comes back. A healing draught is refused (nothing
 * consumed) unless the drinker is hurt, and keeps the healing cooldown.
 */
public class AdvancedDrinkItem extends Item {
    public static final int USE_TICKS = 32;

    private final Draught draught;

    public AdvancedDrinkItem(Properties properties, Draught draught) {
        super(properties);
        this.draught = draught;
    }

    public Draught draught() {
        return draught;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!DrinkApplications.usable(player, draught)) {
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (!DrinkApplications.usable(user, draught)) {
            return stack;
        }
        if (!level.isClientSide) {
            DrinkApplications.apply(user, draught);
            if (user instanceof ServerPlayer player) {
                CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
                player.awardStat(Stats.ITEM_USED.get(this));
                if (draught == Draught.HEALING) {
                    player.getCooldowns().addCooldown(this, DrinkApplications.HEALING_COOLDOWN);
                }
            }
        }
        return ContainerReturns.finish(stack, user, new ItemStack(Items.GLASS_BOTTLE));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        ProductTooltips.append(this, lines);
        PreparationMetadata.appendLabel(stack, lines);
    }
}
