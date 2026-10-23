package io.github.dirtyredz.herbalwitch.apothecary.shop.registry;

import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopServerConfig;
import io.github.dirtyredz.herbalwitch.apothecary.shop.customer.ShopCustomerScheduler;
import io.github.dirtyredz.herbalwitch.apothecary.shop.customer.demand.NpcDemandService;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link.ShopLinkEvents;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link.ShopProjection;
import io.github.dirtyredz.herbalwitch.apothecary.shop.guide.ShopEntries;
import io.github.dirtyredz.herbalwitch.apothecary.shop.guide.ShopShowcaseSection;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopPlayerReceipt;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopReconciliation;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopRecoveryCommand;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopPackets;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopSessionService;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationCreativeTab;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * Wires apothecary commerce (Milestone 11): the register, stock shelf, stock jar and order board, their
 * identity-only block entities and menu, the commerce configuration, the request/update messages
 * (registered after every earlier feature's), server lifecycle and Forge events, recovery commands,
 * the grimoire source and the Apothecary tab entries. Stations never depend on commerce; commerce
 * contributes to the stations' tab through the creative-tab event. Bootstrap only.
 */
public final class ShopRegistration {
    private ShopRegistration() {
    }

    public static void register(IEventBus modBus) {
        ShopBlocks.BLOCKS.register(modBus);
        ShopItems.ITEMS.register(modBus);
        ShopBlockEntities.TYPES.register(modBus);
        ShopMenus.MENUS.register(modBus);
        ShopServerConfig.register(modBus);
        modBus.addListener(ShopRegistration::creativeTab);
        ShopPackets.register();
        ShopProjection.listen(ShopSessionService::published);
        ShopReconciliation.npcEvidence(NpcDemandService::debited);
        ShopServerConfig.listen(() -> {
            var server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) ShopCustomerScheduler.invalidate(server);
        });
        IEventBus forge = MinecraftForge.EVENT_BUS;
        forge.addListener(ShopServerEvents::started);
        forge.addListener(ShopServerEvents::tick);
        forge.addListener(ShopServerEvents::stopped);
        forge.addListener(ShopReconciliation::login);
        forge.addListener(ShopSessionService::logout);
        forge.addListener(ShopPlayerReceipt::clonePlayer);
        forge.addListener(ShopLinkEvents::rightClick);
        forge.addListener(ShopLinkEvents::breakBlock);
        forge.addListener(ShopRecoveryCommand::register);
        GrimoireContent.register(ShopEntries.PRIORITY, new ShopEntries());
    }

    /** A narrow section in the free end of the first witchcraft row, after the preparations, so no row reflows. */
    public static void showcase() {
        ShowcaseRegistry.registerAfter("preparations", new ShopShowcaseSection());
    }

    private static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(StationCreativeTab.APOTHECARY.getKey())) {
            ShopItems.all().forEach(event::accept);
        }
    }
}
