package io.github.dirtyredz.herbalwitch.companions.familiars.registry;

import io.github.dirtyredz.herbalwitch.companions.familiars.menu.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.*;

public final class FamiliarMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, "herbalwitch");
    public static final RegistryObject<MenuType<FamiliarRosterMenu>> ROSTER = MENUS.register("familiars/roster", () -> IForgeMenuType.create(FamiliarRosterMenu::new));
    public static final RegistryObject<MenuType<FamiliarDetailMenu>> DETAIL = MENUS.register("familiars/detail", () -> IForgeMenuType.create(FamiliarDetailMenu::new));
    private FamiliarMenus() {}
}
