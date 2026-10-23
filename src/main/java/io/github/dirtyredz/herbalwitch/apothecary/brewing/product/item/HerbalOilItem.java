package io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item;

import io.github.dirtyredz.herbalwitch.HerbalWitch;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.metadata.PreparationMetadata;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.ContainerReturns;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.OilApplication;
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
 * A bottled oil. Fishing blend and pest repellent are dabbed on yourself (32 ticks) and the bottle
 * comes back; lantern oil is only poured into an oil lantern (the lantern handles that use).
 */
public class HerbalOilItem extends Item {
    public static final String NO_WEAPON = "item." + HerbalWitch.MOD_ID + ".preparations.oil.no_weapon";

    private final OilApplication.Oil oil;
    private final boolean labels;

    public HerbalOilItem(Properties properties, OilApplication.Oil oil) {
        this(properties, oil, false);
    }

    public HerbalOilItem(Properties properties, OilApplication.Oil oil, boolean labels) {
        super(properties);
        this.oil = oil;
        this.labels = labels;
    }

    public OilApplication.Oil oil() {
        return oil;
    }
    @Override public net.minecraft.world.InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (oil != OilApplication.Oil.VENOM) return super.interactLivingEntity(stack, player, target, hand);
        if (!(player instanceof net.minecraft.server.level.ServerPlayer owner)) return net.minecraft.world.InteractionResult.sidedSuccess(true);
        var result = io.github.dirtyredz.herbalwitch.apothecary.brewing.application.FamiliarPreparationApplications.apply(owner,
                io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.AdvancedProductCatalog.VENOM_OIL, stack, target, hand);
        if (result != io.github.dirtyredz.herbalwitch.apothecary.brewing.application.FamiliarPreparationApplications.Result.APPLIED)
            return net.minecraft.world.InteractionResult.FAIL;
        owner.setItemInHand(hand, ContainerReturns.finish(stack, owner, new ItemStack(Items.GLASS_BOTTLE)));
        return net.minecraft.world.InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (oil == OilApplication.Oil.LANTERN) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        if (hand != InteractionHand.MAIN_HAND && oil == OilApplication.Oil.VENOM || !OilApplication.usable(player, oil)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable(NO_WEAPON), true);
            }
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (!(user instanceof Player player) || oil == OilApplication.Oil.LANTERN || !OilApplication.usable(player, oil)) {
            return stack;
        }
        if (!level.isClientSide) {
            OilApplication.apply(player, oil);
        }
        return ContainerReturns.finish(stack, user, new ItemStack(Items.GLASS_BOTTLE));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return oil == OilApplication.Oil.LANTERN ? 0 : 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return oil == OilApplication.Oil.LANTERN ? UseAnim.NONE : UseAnim.BRUSH;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        ProductTooltips.append(this, lines);
        if (labels) PreparationMetadata.appendLabel(stack, lines);
    }
}
