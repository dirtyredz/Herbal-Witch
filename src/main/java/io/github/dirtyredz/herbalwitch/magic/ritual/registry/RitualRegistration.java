package io.github.dirtyredz.herbalwitch.magic.ritual.registry;

import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.magic.ritual.action.AltarActions;
import io.github.dirtyredz.herbalwitch.magic.ritual.action.AttunementAction;
import io.github.dirtyredz.herbalwitch.magic.ritual.action.FoundingAction;
import io.github.dirtyredz.herbalwitch.magic.ritual.action.GardenBlessingAction;
import io.github.dirtyredz.herbalwitch.magic.ritual.action.HomeProtectionAction;
import io.github.dirtyredz.herbalwitch.magic.ritual.action.InitiationAction;
import io.github.dirtyredz.herbalwitch.magic.ritual.action.MeditationAction;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.ritual.grimoire.AltarEntries;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarMenuSync;
import io.github.dirtyredz.herbalwitch.magic.ritual.network.AltarPackets;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessionService;
import io.github.dirtyredz.herbalwitch.magic.ritual.showcase.AltarShowcaseSection;
import io.github.dirtyredz.herbalwitch.magic.ritual.showcase.CircleShowcaseSection;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationCreativeTab;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Wires the functional altar (Milestone 6): its block, item, block entity and zero-slot menu; the
 * Apothecary tab contribution; the two altar messages; the four actions on the shared session
 * lifecycle and its interruptions; the screens' private updates; the grimoire pages; and the
 * showcase section. Any accepted change of the witch rules ends running rituals.
 */
public final class RitualRegistration {
    private RitualRegistration() {
    }

    /** Called once from the mod constructor, after the witch registration (message ids follow registration order). */
    public static void register(IEventBus modBus) {
        RitualBlocks.BLOCKS.register(modBus);
        RitualItems.ITEMS.register(modBus);
        RitualBlockEntities.TYPES.register(modBus);
        RitualMenus.MENUS.register(modBus);
        modBus.addListener(RitualRegistration::creativeTab);

        AltarPackets.register((sender, request) -> AltarActions.handle(sender, request));
        GrimoireContent.register(new AltarEntries());
        AltarSessionService.handle(AltarActionSpec.FOUNDING, new FoundingAction());
        AltarSessionService.handle(AltarActionSpec.INITIATION, new InitiationAction());
        AltarSessionService.handle(AltarActionSpec.MEDITATION, new MeditationAction());
        AltarSessionService.handle(AltarActionSpec.ATTUNEMENT, new AttunementAction());
        AltarSessionService.handle(AltarActionSpec.GARDEN_BLESSING, new GardenBlessingAction());
        AltarSessionService.handle(AltarActionSpec.HOME_PROTECTION, new HomeProtectionAction());
        WitchRules.listen(rules -> AltarSessionService.cancelAll("rules_changed"));

        IEventBus forge = MinecraftForge.EVENT_BUS;
        forge.addListener(AltarSessionService::serverStarted);
        forge.addListener(AltarSessionService::serverStopped);
        forge.addListener(AltarSessionService::serverTick);
        forge.addListener(AltarSessionService::hurt);
        forge.addListener(AltarSessionService::died);
        forge.addListener(AltarSessionService::loggedOut);
        forge.addListener(AltarSessionService::changedDimension);
        forge.addListener(AltarSessionService::tagsUpdated);
        forge.addListener(AltarMenuSync::playerTick);
        forge.addListener(AltarActions::loggedOut);

        ShowcaseRegistry.register(new AltarShowcaseSection());
        ShowcaseRegistry.register(new CircleShowcaseSection());
    }

    private static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(StationCreativeTab.APOTHECARY.getKey())) {
            event.accept(RitualItems.ALTAR.get());
            event.accept(RitualItems.CIRCLE.get());
        }
    }
}
