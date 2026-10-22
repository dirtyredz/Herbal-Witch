package io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.ContainerReturns;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.DrinkApplication;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.DrinkRecovery;
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
 * A bottled kettle drink, drunk like a potion (32 ticks). On finishing, once and server side, the
 * drink's application runs and the glass bottle comes back. Anyone can drink it. Item data such as
 * a water purity tag rides on the stack (so different data never stack together) and other mods
 * read it from the finished-use event. Energy tea obeys the player's saved cooldown
 * ({@link DrinkRecovery#teaCooldown}): a cup started or finished while it runs does nothing and
 * consumes nothing, and the vanilla item cooldown only mirrors it (restored after login and respawn).
 */
public class HerbalDrinkItem extends Item {
    public static final int USE_TICKS = 32;

    private final DrinkApplication.Drink drink;

    public HerbalDrinkItem(Properties properties, DrinkApplication.Drink drink) {
        super(properties);
        this.drink = drink;
    }

    public DrinkApplication.Drink drink() {
        return drink;
    }

    /** Whether a server player's saved energy-tea cooldown refuses this drink now (and re-shows it). */
    private boolean coolingDown(LivingEntity user) {
        if (drink != DrinkApplication.Drink.ENERGY || !(user instanceof ServerPlayer player)) {
            return false;
        }
        int remaining = DrinkRecovery.teaCooldown(player);
        if (remaining > 0) {
            player.getCooldowns().addCooldown(this, remaining);
            return true;
        }
        return false;
    }

    /** Mirrors a player's saved energy-tea cooldown on the vanilla item cooldown (login, respawn). */
    public static void syncCooldown(ServerPlayer player, Item energyTea) {
        int remaining = DrinkRecovery.teaCooldown(player);
        if (remaining > 0) {
            player.getCooldowns().addCooldown(energyTea, remaining);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && coolingDown(player)) {
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (!level.isClientSide) {
            if (coolingDown(user)) {
                return stack;
            }
            DrinkApplication.apply(user, drink);
            if (user instanceof ServerPlayer player) {
                CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
                player.awardStat(Stats.ITEM_USED.get(this));
                if (drink == DrinkApplication.Drink.ENERGY) {
                    player.getCooldowns().addCooldown(this, DrinkRecovery.teaCooldownLength());
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
    }
}
