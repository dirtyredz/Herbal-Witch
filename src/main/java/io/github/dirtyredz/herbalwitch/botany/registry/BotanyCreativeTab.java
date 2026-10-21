package io.github.dirtyredz.herbalwitch.botany.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorCreativeTab;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** The gardening creative tab: the herb bed, then each herb's seeds and fresh herb. */
public final class BotanyCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HerbalWitch.MOD_ID);

    public static final String TITLE_KEY = "itemGroup." + HerbalWitch.MOD_ID + ".gardening";

    public static final RegistryObject<CreativeModeTab> GARDENING = TABS.register("gardening", () -> CreativeModeTab.builder()
            .title(Component.translatable(TITLE_KEY))
            .icon(() -> new ItemStack(BotanyItems.fresh(HerbCatalog.LAVENDER)))
            .withTabsBefore(DecorCreativeTab.DECOR.getId())
            .displayItems((parameters, output) -> BotanyItems.all().forEach(output::accept))
            .build());

    private BotanyCreativeTab() {
    }
}
