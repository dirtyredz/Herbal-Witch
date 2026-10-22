package io.github.dirtyredz.herbalwitch.stations.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyCreativeTab;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationCatalog;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * The apothecary tab: the five stations, then every preparation (bundles, powders, products, the oil
 * lantern). Owned here because stations already depend on preparations, never the reverse.
 */
public final class StationCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HerbalWitch.MOD_ID);

    public static final String TITLE_KEY = "itemGroup." + HerbalWitch.MOD_ID + ".apothecary";

    public static final RegistryObject<CreativeModeTab> APOTHECARY = TABS.register("apothecary", () -> CreativeModeTab.builder()
            .title(Component.translatable(TITLE_KEY))
            .icon(() -> new ItemStack(StationItems.item(StationCatalog.KETTLE)))
            .withTabsBefore(BotanyCreativeTab.GARDENING.getId())
            .displayItems((parameters, output) -> {
                StationItems.all().forEach(output::accept);
                PreparationItems.all().forEach(output::accept);
                AdvancedPreparations.all().forEach(output::accept);
            })
            .build());

    private StationCreativeTab() {
    }
}
