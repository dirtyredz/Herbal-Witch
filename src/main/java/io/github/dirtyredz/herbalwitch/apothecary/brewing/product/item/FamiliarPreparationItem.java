package io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.ContainerReturns;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.advanced.FamiliarPreparationApplications;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.ProductSpec;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.metadata.PreparationMetadata;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A familiar preparation. It is made now; it is given to a bonded familiar once familiar bonding
 * exists (Milestone 9 installs {@link FamiliarPreparationApplications}). Until then every use is
 * refused with "Familiar application becomes available with familiar bonding" and nothing is consumed.
 */
public class FamiliarPreparationItem extends Item {
    public static final String UNAVAILABLE = "item." + HerbalWitch.MOD_ID + ".preparations.familiar.unavailable";

    private final ProductSpec product;

    public FamiliarPreparationItem(Properties properties, ProductSpec product) {
        super(properties);
        this.product = product;
    }

    public ProductSpec product() {
        return product;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable(UNAVAILABLE), true);
        }
        return InteractionResultHolder.fail(player.getItemInHand(hand));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(player instanceof ServerPlayer server)) {
            return InteractionResult.sidedSuccess(true);
        }
        FamiliarPreparationApplications.Result result = FamiliarPreparationApplications.apply(server, product, stack, target, hand);
        if (result != FamiliarPreparationApplications.Result.APPLIED) {
            server.displayClientMessage(Component.translatable(UNAVAILABLE), true);
            return InteractionResult.FAIL;
        }
        ItemStack held = ContainerReturns.finish(stack, server, new ItemStack(Items.GLASS_BOTTLE));
        server.setItemInHand(hand, held);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        ProductTooltips.append(this, lines);
        if (!FamiliarPreparationApplications.available()) {
            lines.add(Component.translatable(UNAVAILABLE).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
        PreparationMetadata.appendLabel(stack, lines);
    }
}
