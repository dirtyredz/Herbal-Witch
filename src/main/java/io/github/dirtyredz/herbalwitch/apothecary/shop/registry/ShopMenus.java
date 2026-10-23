package io.github.dirtyredz.herbalwitch.apothecary.shop.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The one zero-slot commerce menu; its views are client panels over the same validated session. */
public final class ShopMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, HerbalWitch.MOD_ID);
    public static final RegistryObject<MenuType<ShopMenu>> MENU = MENUS.register("shop/menu", () -> IForgeMenuType.create(ShopMenu::new));

    private ShopMenus() {
    }
}
