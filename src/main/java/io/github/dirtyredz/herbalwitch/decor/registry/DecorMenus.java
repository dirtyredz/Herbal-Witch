package io.github.dirtyredz.herbalwitch.decor.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.behavior.storage.FurnitureStorageMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Decor's menu types: the one storage menu, sized by its opening packet. */
public final class DecorMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, HerbalWitch.MOD_ID);

    public static final RegistryObject<MenuType<FurnitureStorageMenu>> STORAGE = MENUS.register("decor/storage",
            () -> IForgeMenuType.create(FurnitureStorageMenu::client));

    private DecorMenus() {
    }
}
