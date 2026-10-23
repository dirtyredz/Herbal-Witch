package io.github.dirtyredz.herbalwitch.magic.wand.item;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import io.github.dirtyredz.herbalwitch.magic.wand.use.WandUseService;
/** Consumed clicks prevent accidental container use or offhand fallback after a refused utility. */
public final class WandItem extends Item {
 public WandItem(Properties properties){super(properties);}
 @Override public boolean isEnchantable(ItemStack stack){return false;}
 @Override public InteractionResult onItemUseFirst(ItemStack stack,UseOnContext context){return useOn(context);}
 @Override public InteractionResult useOn(UseOnContext context){if(context.getPlayer() instanceof ServerPlayer actor)WandUseService.message(actor,WandUseService.use(actor,context.getHand()));return InteractionResult.sidedSuccess(context.getLevel().isClientSide);}
 @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){if(player instanceof ServerPlayer actor)WandUseService.message(actor,WandUseService.use(actor,hand));return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),level.isClientSide);}
 @Override public InteractionResult interactLivingEntity(ItemStack stack,Player player,LivingEntity target,InteractionHand hand){if(player instanceof ServerPlayer actor&&target instanceof Mob mob)WandUseService.message(actor,WandUseService.use(actor,hand,mob));return InteractionResult.sidedSuccess(player.level().isClientSide);}
 @Override public void appendHoverText(ItemStack stack,Level level,java.util.List<net.minecraft.network.chat.Component> lines,TooltipFlag flag){var mode=WandUseService.mode(stack);lines.add(net.minecraft.network.chat.Component.translatable(mode==null?"tooltip.herbalwitch.wand.invalid":"wand.herbalwitch.mode."+mode.id()));}
}
