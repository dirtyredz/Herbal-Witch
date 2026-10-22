package io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry;

import com.mojang.serialization.Codec;
import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.PestRepellentEvents;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.combat.CombatEvents;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEvents;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.repellent.TargetFilters;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.projectile.ProjectileLimits;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.HarmAttribution;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item.HerbalDrinkItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.foraging.ResinDropModifier;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.showcase.AdvancedPreparationShowcaseSection;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.showcase.PreparationShowcaseSection;
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
        AdvancedPreparations.ITEMS.register(modBus);
        PreparationBlockEntities.TYPES.register(modBus);
        PreparationEffects.EFFECTS.register(modBus);
        BrewingEntities.TYPES.register(modBus);
        LOOT_MODIFIERS.register(modBus);
        MinecraftForge.EVENT_BUS.addListener(PestRepellentEvents::onChangeTarget);
        registerAdvanced();
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> syncTeaCooldown(event.getEntity()));
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent event) -> syncTeaCooldown(event.getEntity()));
        ShowcaseRegistry.register(new PreparationShowcaseSection());
        ShowcaseRegistry.register(new AdvancedPreparationShowcaseSection());
    }

    /**
     * Milestone 7: the acquisition filters, hard-control and combat events, delayed-harm rechecks and
     * projectile bookkeeping; the entity permission boundary reads the configured preparation PvP.
     */
    private static void registerAdvanced() {
        IEventBus forge = MinecraftForge.EVENT_BUS;
        forge.addListener(TargetFilters::onChangeTarget);
        forge.addListener(ControlEvents::onAttack);
        forge.addListener(EventPriority.LOWEST, ControlEvents::onDamage);
        forge.addListener(ControlEvents::onExpired);
        forge.addListener(ControlEvents::onRemoved);
        forge.addListener(ControlEvents::onLivingTick);
        forge.addListener(EventPriority.LOWEST, CombatEvents::onDamage);
        forge.addListener(HarmAttribution::onHurt);
        forge.addListener((ServerStoppedEvent event) -> ProjectileLimits.clear());
        EntityActionPermission.pvpSetting(() -> WitchRules.server().workings().preparationPvp());
    }

    /** The saved energy-tea cooldown outlives the vanilla item cooldown: show it again on a new player object. */
    private static void syncTeaCooldown(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            HerbalDrinkItem.syncCooldown(serverPlayer, PreparationItems.product(EverydayProductCatalog.ENERGY_TEA));
        }
    }
}
