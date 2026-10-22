package io.github.dirtyredz.herbalwitch;

import io.github.dirtyredz.herbalwitch.botany.registry.BotanyRegistration;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationRegistration;
import io.github.dirtyredz.herbalwitch.data.HerbalWitchDataProviders;
import io.github.dirtyredz.herbalwitch.decor.registry.ArtSpikeRegistration;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireRegistration;
import io.github.dirtyredz.herbalwitch.platform.compat.CompatibilityRegistration;
import io.github.dirtyredz.herbalwitch.showcase.command.ShowcaseCommandRegistration;
import io.github.dirtyredz.herbalwitch.stations.registry.StationRegistration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Bootstrap only: wires feature registries (each registers its own showcase section, so the call
 * order is the showcase layout order; the grimoire also opens the mod's network channel), the
 * showcase command and the datagen listener. No gameplay
 * logic. Client-only wiring registers itself from {@code client/} and never loads on a dedicated server.
 */
@Mod(HerbalWitch.MOD_ID)
public final class HerbalWitch {
    public static final String MOD_ID = "herbalwitch";

    public HerbalWitch() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ArtSpikeRegistration.register(modBus);
        BotanyRegistration.register(modBus);
        StationRegistration.register(modBus);
        PreparationRegistration.register(modBus);
        GrimoireRegistration.register(modBus);
        ShowcaseCommandRegistration.register();
        CompatibilityRegistration.register();
        modBus.addListener(HerbalWitchDataProviders::gatherData);
    }
}
