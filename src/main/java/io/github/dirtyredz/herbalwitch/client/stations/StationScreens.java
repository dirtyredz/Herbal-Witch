package io.github.dirtyredz.herbalwitch.client.stations;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.client.stations.ProcessStationScreen.Layout;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.ProcessingCategories;
import io.github.dirtyredz.herbalwitch.apothecary.stations.block.StationMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;

/** Registers each station menu's screen and its atlas layout (positions match tools/art/interface/stations.mjs). */
public final class StationScreens {
    private StationScreens() {
    }

    private static <M extends StationMenu> void process(MenuType<M> type, Layout layout) {
        MenuScreens.<M, ProcessStationScreen<M>>register(type, (menu, inv, title) -> new ProcessStationScreen<>(menu, inv, title, layout));
    }

    private static ResourceLocation atlas(String station) {
        return ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "textures/gui/stations/" + station + ".png");
    }

    /** Call from client setup (enqueued on the main thread). */
    public static void register() {
        Layout rack = new Layout(atlas("drying_rack"), 79, 34, -1, 0, ProcessingCategories.DRYING.id());
        Layout mortar = new Layout(atlas("mortar"), 92, 34, -1, 0, ProcessingCategories.GRINDING.id());
        Layout kettle = new Layout(atlas("kettle"), 79, 34, 81, 54, ProcessingCategories.STEEPING.id());
        Layout pot = new Layout(atlas("infusion_pot"), 79, 34, 81, 54, ProcessingCategories.INFUSION.id());
        process(StationMenus.DRYING_RACK.get(), rack);
        process(StationMenus.MORTAR.get(), mortar);
        process(StationMenus.KETTLE.get(), kettle);
        process(StationMenus.INFUSION_POT.get(), pot);
        process(StationMenus.CAULDRON.get(), new Layout(atlas("cauldron"), 92, 34, 50, 36, ProcessingCategories.CAULDRON.id()));
        process(StationMenus.STILL.get(), new Layout(atlas("still"), 79, 34, 81, 54, ProcessingCategories.DISTILLATION.id()));
        process(StationMenus.BOTTLING_BENCH.get(), new Layout(atlas("bottling_bench"), 92, 34, -1, 0, ProcessingCategories.BOTTLING.id()));
        process(StationMenus.CHARM_WORKBENCH.get(), new Layout(atlas("charm_workbench"), 92, 34, -1, 0, ProcessingCategories.CHARM_WORK.id()));
        MenuScreens.register(StationMenus.HEARTH.get(), HearthScreen::new);
    }
}
