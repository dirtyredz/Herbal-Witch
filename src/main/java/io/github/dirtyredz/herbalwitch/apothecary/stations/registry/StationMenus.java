package io.github.dirtyredz.herbalwitch.apothecary.stations.registry;

import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.broom_stand.BroomStandMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.wand_bench.WandBenchMenu;
import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.bottling_bench.BottlingBenchMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.cauldron.CauldronMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.charm_workbench.CharmWorkbenchMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.still.StillMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.drying_rack.DryingRackMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.hearth.HearthMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.infusion_pot.InfusionPotMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.kettle.KettleMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.mortar.MortarMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** One menu type per station. */
public final class StationMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, HerbalWitch.MOD_ID);

    public static final RegistryObject<MenuType<DryingRackMenu>> DRYING_RACK =
            MENUS.register("stations/drying_rack", () -> IForgeMenuType.create(DryingRackMenu::new));
    public static final RegistryObject<MenuType<MortarMenu>> MORTAR =
            MENUS.register("stations/mortar", () -> IForgeMenuType.create(MortarMenu::new));
    public static final RegistryObject<MenuType<KettleMenu>> KETTLE =
            MENUS.register("stations/kettle", () -> IForgeMenuType.create(KettleMenu::new));
    public static final RegistryObject<MenuType<HearthMenu>> HEARTH =
            MENUS.register("stations/hearth", () -> IForgeMenuType.create(HearthMenu::new));
    public static final RegistryObject<MenuType<InfusionPotMenu>> INFUSION_POT =
            MENUS.register("stations/infusion_pot", () -> IForgeMenuType.create(InfusionPotMenu::new));
    public static final RegistryObject<MenuType<CauldronMenu>> CAULDRON =
            MENUS.register("stations/cauldron", () -> IForgeMenuType.create(CauldronMenu::new));
    public static final RegistryObject<MenuType<StillMenu>> STILL =
            MENUS.register("stations/still", () -> IForgeMenuType.create(StillMenu::new));
    public static final RegistryObject<MenuType<BottlingBenchMenu>> BOTTLING_BENCH =
            MENUS.register("stations/bottling_bench", () -> IForgeMenuType.create(BottlingBenchMenu::new));
    public static final RegistryObject<MenuType<CharmWorkbenchMenu>> CHARM_WORKBENCH =
            MENUS.register("stations/charm_workbench", () -> IForgeMenuType.create(CharmWorkbenchMenu::new));

    public static final RegistryObject<MenuType<BroomStandMenu>> BROOM_STAND = MENUS.register(
            "stations/tools/broom_stand/station", () -> IForgeMenuType.create(BroomStandMenu::new));

    public static final RegistryObject<MenuType<WandBenchMenu>> WAND_BENCH = MENUS.register(
            "stations/tools/wand_bench/station", () -> IForgeMenuType.create(WandBenchMenu::new));

    private StationMenus() {
    }
}
