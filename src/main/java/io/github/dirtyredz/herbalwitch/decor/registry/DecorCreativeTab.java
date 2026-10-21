package io.github.dirtyredz.herbalwitch.decor.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.catalog.ArtSpikeCatalog;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** The cottage decor creative tab: every furniture item once, in catalogue order. */
public final class DecorCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HerbalWitch.MOD_ID);

    public static final String TITLE_KEY = "itemGroup." + HerbalWitch.MOD_ID + ".decor";

    public static final RegistryObject<CreativeModeTab> DECOR = TABS.register("decor", () -> CreativeModeTab.builder()
            .title(Component.translatable(TITLE_KEY))
            .icon(() -> new ItemStack(DecorItems.item(ArtSpikeCatalog.ARMCHAIR)))
            .displayItems((parameters, output) -> DecorItems.all().forEach(output::accept))
            .build());

    private DecorCreativeTab() {
    }
}
