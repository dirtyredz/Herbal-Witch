package io.github.dirtyredz.herbalwitch.brewing.product.registry;

import com.mojang.serialization.Codec;
import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.brewing.effect.PestRepellentEvents;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.item.HerbalDrinkItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import io.github.dirtyredz.herbalwitch.brewing.product.foraging.ResinDropModifier;
import io.github.dirtyredz.herbalwitch.brewing.product.showcase.PreparationShowcaseSection;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Wires preparations: bundles, powders, products, the oil lantern, effects and resin foraging. */
public final class PreparationRegistration {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, HerbalWitch.MOD_ID);
    public static final RegistryObject<Codec<ResinDropModifier>> RESIN =
            LOOT_MODIFIERS.register("preparations/resin", () -> ResinDropModifier.CODEC);

    private PreparationRegistration() {
    }

    /** Called once from the mod constructor. */
    public static void register(IEventBus modBus) {
        PreparationBlocks.BLOCKS.register(modBus);
        PreparationItems.ITEMS.register(modBus);
        PreparationBlockEntities.TYPES.register(modBus);
        PreparationEffects.EFFECTS.register(modBus);
        LOOT_MODIFIERS.register(modBus);
        MinecraftForge.EVENT_BUS.addListener(PestRepellentEvents::onChangeTarget);
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> syncTeaCooldown(event.getEntity()));
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent event) -> syncTeaCooldown(event.getEntity()));
        ShowcaseRegistry.register(new PreparationShowcaseSection());
    }

    /** The saved energy-tea cooldown outlives the vanilla item cooldown: show it again on a new player object. */
    private static void syncTeaCooldown(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            HerbalDrinkItem.syncCooldown(serverPlayer, PreparationItems.product(EverydayProductCatalog.ENERGY_TEA));
        }
    }
}
