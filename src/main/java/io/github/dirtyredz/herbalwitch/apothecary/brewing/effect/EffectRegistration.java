package io.github.dirtyredz.herbalwitch.apothecary.brewing.effect;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.combat.CombatEvents;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEvents;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.repellent.TargetFilters;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.projectile.ProjectileLimits;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.HarmAttribution;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

/** Installs preparation effect listeners in their established order and priorities. */
public final class EffectRegistration {
    private EffectRegistration() { }

    /**
     * Milestone 7: the acquisition filters, hard-control and combat events, delayed-harm rechecks and
     * projectile bookkeeping; the entity permission boundary reads the configured preparation PvP.
     */
    public static void register() {
        IEventBus forge = MinecraftForge.EVENT_BUS;
        forge.addListener(PestRepellentEvents::onChangeTarget);
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

}
