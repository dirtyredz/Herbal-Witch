package io.github.dirtyredz.herbalwitch.magic.broom.registry;
import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.broom.item.BroomItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.*;
public final class BroomItems {
 public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HerbalWitch.MOD_ID);
 public static final RegistryObject<Item> BROOM = ITEMS.register("magic/broom/broom", () -> new BroomItem(new Item.Properties().stacksTo(1)));
 private BroomItems() { }
}
