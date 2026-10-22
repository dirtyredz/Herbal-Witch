package io.github.dirtyredz.herbalwitch.decor.registry;

import io.github.dirtyredz.herbalwitch.decor.grimoire.DecorEntries;

import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;

import io.github.dirtyredz.herbalwitch.decor.behavior.seating.SeatEvents;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.showcase.DecorShowcaseSamples;
import io.github.dirtyredz.herbalwitch.decor.showcase.DecorShowcaseSection;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Turns the delivered furniture catalogue into registered blocks and items, each piece once in the
 * cottage design, plus the shared runtime types the behaviours need (the seat entity), their event
 * wiring, the creative tab and the showcase section. Bootstrap only: which class a piece becomes is
 * {@link DecorBlockFactory}'s.
 */
public final class DecorRegistration {
    private DecorRegistration() {
    }

    /** Called once from the mod constructor. */
    public static void register(IEventBus modBus) {
        GrimoireContent.register(DecorEntries.PRIORITY, new DecorEntries());
        for (FurnitureSpec spec : FurnitureCatalog.ALL) {
            DecorBlocks.register(spec, () -> DecorBlockFactory.create(spec));
        }
        DecorItems.registerBlockItems();
        DecorBlocks.BLOCKS.register(modBus);
        DecorItems.ITEMS.register(modBus);
        DecorBlockEntities.TYPES.register(modBus);
        DecorMenus.MENUS.register(modBus);
        DecorEntities.ENTITIES.register(modBus);
        DecorCreativeTab.TABS.register(modBus);
        SeatEvents.register(MinecraftForge.EVENT_BUS);
        ShowcaseRegistry.register(new DecorShowcaseSection(DecorShowcaseSamples::stock));
    }
}
