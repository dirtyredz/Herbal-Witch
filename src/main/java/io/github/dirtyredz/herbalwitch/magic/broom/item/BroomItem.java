package io.github.dirtyredz.herbalwitch.magic.broom.item;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import io.github.dirtyredz.herbalwitch.magic.broom.persistence.BroomItemData;
/** Physical tool; feature services own interactions. */
public class BroomItem extends Item {
 public BroomItem(Properties properties) { super(properties); }
 @Override public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) { return io.github.dirtyredz.herbalwitch.magic.broom.interaction.BroomDeployment.use(context); }
 @Override public void appendHoverText(ItemStack stack,net.minecraft.world.level.Level level,java.util.List<net.minecraft.network.chat.Component> lines,net.minecraft.world.item.TooltipFlag flag){
  var data=BroomItemData.read(stack);if(data.isEmpty())lines.add(net.minecraft.network.chat.Component.translatable("tooltip.herbalwitch.broom.invalid"));else {lines.add(net.minecraft.network.chat.Component.translatable("tooltip.herbalwitch.broom.condition",data.get().condition()));lines.add(net.minecraft.network.chat.Component.translatable("tooltip.herbalwitch.broom.use"));}
 }
 @Override public boolean isBarVisible(ItemStack stack) { return BroomItemData.read(stack).map(d -> d.wear() > 0).orElse(true); }
 @Override public int getBarWidth(ItemStack stack) { return BroomItemData.read(stack).map(d -> Math.round(13F * d.condition() / 600)).orElse(0); }
 @Override public int getBarColor(ItemStack stack) { return 0x609542; }
 @Override public boolean isEnchantable(ItemStack stack) { return false; }
}
