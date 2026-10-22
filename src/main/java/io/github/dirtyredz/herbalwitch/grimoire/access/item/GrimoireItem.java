package io.github.dirtyredz.herbalwitch.grimoire.access.item;

import io.github.dirtyredz.herbalwitch.grimoire.access.menu.GrimoireAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The portable grimoire. Using it opens the holder's own knowledge; the item carries no
 * discoveries (no NBT), so any grimoire opens whoever holds it, and giving one away or consuming it
 * in a recipe shares or loses nothing.
 */
public class GrimoireItem extends Item {
    public static final String TOOLTIP = "item.herbalwitch.grimoire.book.tooltip";

    public GrimoireItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            GrimoireAccess.book().open(serverPlayer);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable(TOOLTIP).withStyle(ChatFormatting.GRAY));
    }
}
