package io.github.dirtyredz.herbalwitch.magic.wand.registry;
import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.wand.item.WandItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.*;
public final class WandItems {
 public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HerbalWitch.MOD_ID);
 public static final RegistryObject<Item> WAND = ITEMS.register("magic/wand/wand", () -> new WandItem(new Item.Properties().stacksTo(1)));
 private WandItems() { }
}
