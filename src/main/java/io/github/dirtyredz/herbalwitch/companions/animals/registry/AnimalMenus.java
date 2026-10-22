package io.github.dirtyredz.herbalwitch.companions.animals.registry;

import io.github.dirtyredz.herbalwitch.companions.animals.inspection.menu.AnimalMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class AnimalMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, "herbalwitch");
    public static final RegistryObject<MenuType<AnimalMenu>> CARD = MENUS.register("animals/card", () -> IForgeMenuType.create(AnimalMenu::new));
    private AnimalMenus() {}
}
