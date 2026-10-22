package io.github.dirtyredz.herbalwitch.grimoire.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.GrimoireCatalog;
import io.github.dirtyredz.herbalwitch.grimoire.access.menu.GrimoireMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The zero-slot reader menu, {@code grimoire/reader}. */
public final class GrimoireMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, HerbalWitch.MOD_ID);

    public static final RegistryObject<MenuType<GrimoireMenu>> READER =
            MENUS.register(GrimoireCatalog.READER_PATH, () -> IForgeMenuType.create(GrimoireMenu::new));

    private GrimoireMenus() {
    }
}
