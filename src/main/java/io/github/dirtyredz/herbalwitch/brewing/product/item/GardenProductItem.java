package io.github.dirtyredz.herbalwitch.brewing.product.item;

import io.github.dirtyredz.herbalwitch.brewing.application.ContainerReturns;
import io.github.dirtyredz.herbalwitch.brewing.application.GardenApplication;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A garden product used on a block: dusts (fertilizer, flower and mushroom growth) are used up;
 * tonics (growth infusion, soil tonic) also return their bottle. Nothing is consumed unless the
 * action changed the world.
 */
public class GardenProductItem extends Item {
    private final GardenApplication.Garden kind;
    private final boolean bottled;

    public GardenProductItem(Properties properties, GardenApplication.Garden kind, boolean bottled) {
        super(properties);
        this.kind = kind;
        this.bottled = bottled;
    }

    public GardenApplication.Garden kind() {
        return kind;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!GardenApplication.apply(context, kind)) {
            return InteractionResult.FAIL;
        }
        ItemStack held = context.getItemInHand();
        ItemStack after = ContainerReturns.finish(held, context.getPlayer(), bottled ? new ItemStack(Items.GLASS_BOTTLE) : ItemStack.EMPTY);
        if (after != held && context.getPlayer() != null) {
            context.getPlayer().setItemInHand(context.getHand(), after);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        ProductTooltips.append(this, lines);
    }
}
