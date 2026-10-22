package io.github.dirtyredz.herbalwitch.apothecary.botany.registry;

import io.github.dirtyredz.herbalwitch.apothecary.botany.conditions.HerbConditionReloadListener;
import io.github.dirtyredz.herbalwitch.apothecary.botany.foraging.BotanyLootModifiers;
import io.github.dirtyredz.herbalwitch.apothecary.botany.showcase.GardenShowcaseSection;
import io.github.dirtyredz.herbalwitch.apothecary.botany.showcase.HerbShowcaseSection;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

/** Wires botany: herb plants, seeds, fresh herbs, the herb bed, its tab, foraging and reloadable conditions. */
public final class BotanyRegistration {
    private BotanyRegistration() {
    }

    /** Called once from the mod constructor. */
    public static void register(IEventBus modBus) {
        BotanyBlocks.BLOCKS.register(modBus);
        BotanyItems.ITEMS.register(modBus);
        BotanyCreativeTab.TABS.register(modBus);
        BotanyLootModifiers.SERIALIZERS.register(modBus);
        MinecraftForge.EVENT_BUS.addListener(HerbConditionReloadListener::onAddReloadListeners);
        ShowcaseRegistry.register(new HerbShowcaseSection());
        ShowcaseRegistry.register(new GardenShowcaseSection());
    }
}
