package io.github.dirtyredz.herbalwitch.magic.broom.item;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import io.github.dirtyredz.herbalwitch.magic.broom.persistence.BroomItemData;
/** Physical tool; feature services own interactions. */
public class BroomItem extends Item {
 public BroomItem(Properties properties) { super(properties); }
 @Override public boolean isBarVisible(ItemStack stack) { return BroomItemData.read(stack).map(d -> d.wear() > 0).orElse(true); }
 @Override public int getBarWidth(ItemStack stack) { return BroomItemData.read(stack).map(d -> Math.round(13F * d.condition() / 600)).orElse(0); }
 @Override public int getBarColor(ItemStack stack) { return 0x609542; }
 @Override public boolean isEnchantable(ItemStack stack) { return false; }
}
