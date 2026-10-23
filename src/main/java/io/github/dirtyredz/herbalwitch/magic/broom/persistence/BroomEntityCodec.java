package io.github.dirtyredz.herbalwitch.magic.broom.persistence;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
import io.github.dirtyredz.herbalwitch.magic.broom.registry.BroomItems;
/** Physical identity only. No velocity, passenger, control nonce or offline flight counter. */
public final class BroomEntityCodec {
 public record Record(UUID owner,ItemStack item) { public Record { item=item.copy(); } }
 private BroomEntityCodec(){}
 public static CompoundTag encode(UUID owner,ItemStack item){var t=new CompoundTag();t.putInt("schema",1);t.putUUID("owner",owner);t.put("item",item.save(new CompoundTag()));return t;}
 public static Optional<Record> decode(CompoundTag t){
  if(!t.contains("schema",Tag.TAG_INT)||t.getInt("schema")!=1||!t.hasUUID("owner")||!t.contains("item",Tag.TAG_COMPOUND))return Optional.empty();
  ItemStack item=ItemStack.of(t.getCompound("item"));
  return item.is(BroomItems.BROOM.get())&&BroomItemData.read(item).isPresent()?Optional.of(new Record(t.getUUID("owner"),item)):Optional.empty();
 }
}
