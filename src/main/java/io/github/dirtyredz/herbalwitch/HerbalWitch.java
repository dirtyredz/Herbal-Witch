package io.github.dirtyredz.herbalwitch;

import io.github.dirtyredz.herbalwitch.botany.registry.BotanyRegistration;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationRegistration;
import io.github.dirtyredz.herbalwitch.client.HerbalWitchClient;
import io.github.dirtyredz.herbalwitch.data.HerbalWitchDataProviders;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorRegistration;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireRegistration;
import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualRegistration;
import io.github.dirtyredz.herbalwitch.magic.witch.registry.WitchRegistration;
import io.github.dirtyredz.herbalwitch.platform.compat.CompatibilityRegistration;
import io.github.dirtyredz.herbalwitch.showcase.command.ShowcaseCommandRegistration;
import io.github.dirtyredz.herbalwitch.stations.registry.StationRegistration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Bootstrap only: wires feature registries (each registers its own showcase section, so the call
 * order is the showcase layout order; the grimoire opens the mod's network channel and the witch and
 * ritual features add their messages after it, so message ids follow this order), the showcase
 * command and the datagen listener. No gameplay logic. Client-only wiring registers itself from
 * {@code client/} and never loads on a dedicated server; its configuration is registered here
 * through the client-only branch.
 */
@Mod(HerbalWitch.MOD_ID)
public final class HerbalWitch {
    public static final String MOD_ID = "herbalwitch";

    public HerbalWitch() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        DecorRegistration.register(modBus);
        BotanyRegistration.register(modBus);
        StationRegistration.register(modBus);
        PreparationRegistration.register(modBus);
        GrimoireRegistration.register(modBus);
        WitchRegistration.register(modBus);
        RitualRegistration.register(modBus);
        ShowcaseCommandRegistration.register();
        CompatibilityRegistration.register();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> HerbalWitchClient::registerConfig);
        modBus.addListener(HerbalWitchDataProviders::gatherData);
    }
}
