package io.github.dirtyredz.herbalwitch;

import io.github.dirtyredz.herbalwitch.data.HerbalWitchDataProviders;
import io.github.dirtyredz.herbalwitch.decor.registry.ArtSpikeRegistration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Bootstrap only: wires feature registries and the datagen listener. No gameplay logic. Client-only
 * wiring registers itself from {@code client/} and never loads on a dedicated server.
 */
@Mod(HerbalWitch.MOD_ID)
public final class HerbalWitch {
    public static final String MOD_ID = "herbalwitch";

    public HerbalWitch() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ArtSpikeRegistration.register(modBus);
        modBus.addListener(HerbalWitchDataProviders::gatherData);
    }
}
