package io.github.dirtyredz.herbalwitch.magic.ritual.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarCatalog;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The zero-slot altar menu, {@code magic/ritual/altar}. */
public final class RitualMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, HerbalWitch.MOD_ID);

    public static final RegistryObject<MenuType<AltarMenu>> ALTAR = MENUS.register(AltarCatalog.PATH, () -> IForgeMenuType.create(AltarMenu::new));

    private RitualMenus() {
    }
}
