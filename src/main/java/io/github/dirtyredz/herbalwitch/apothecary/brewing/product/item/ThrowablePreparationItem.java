package io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.ThrowableApplications.Burst;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.metadata.PreparationMetadata;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.projectile.PreparationProjectile;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.projectile.ProjectileLimits;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

/**
 * A thrown preparation (a vial, or a dose of blinding dust). Throwing consumes one at launch and
 * never returns glass, even on a miss. It is refused before anything is consumed while the shared
 * throwing cooldown runs or the thrower's or the dimension's active preparations are at their limit
 * ({@link ProjectileLimits}).
 */
public class ThrowablePreparationItem extends Item {
    public static final int COOLDOWN = 20;
    public static final String LIMIT = "item." + HerbalWitch.MOD_ID + ".preparations.throw.limit";

    private final Burst burst;
    private final Supplier<List<Item>> family;

    /** @param family every throwable item, which share the throwing cooldown */
    public ThrowablePreparationItem(Properties properties, Burst burst, Supplier<List<Item>> family) {
        super(properties);
        this.burst = burst;
        this.family = family;
    }

    public Burst burst() {
        return burst;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server) {
            if (!ProjectileLimits.canLaunch(server, player)) {
                player.displayClientMessage(Component.translatable(LIMIT), true);
                return InteractionResultHolder.fail(stack);
            }
            PreparationProjectile projectile = new PreparationProjectile(level, player, burst);
            ItemStack thrown = stack.copy();
            thrown.setCount(1);
            projectile.setItem(thrown);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), -10.0F, 0.75F, 1.0F);
            level.addFreshEntity(projectile);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.5F,
                    0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        for (Item item : family.get()) {
            player.getCooldowns().addCooldown(item, COOLDOWN);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        ProductTooltips.append(this, lines);
        PreparationMetadata.appendLabel(stack, lines);
    }
}
