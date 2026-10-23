package io.github.dirtyredz.herbalwitch.platform.permission.entity;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * Who caused a delayed harmful effect on a player (a splash or coating poison), so each later tick is
 * rechecked against the current rules: if PvP was turned off, the teams changed or the target became
 * protected since, the next tick is cancelled and the effect removed. One bounded record per target
 * (actor and expiry) in the entity's persistent data; vanilla effects stay vanilla.
 */
public final class HarmAttribution {
    private static final String KEY = HerbalWitch.MOD_ID + ":harm_source";

    private HarmAttribution() {
    }

    /** Records that {@code actor} applied a delayed harmful effect lasting {@code ticks}. */
    public static void record(LivingEntity target, UUID actor, int ticks) {
        record(target, actor, ticks, EntityActionContext.Action.HARM);
    }
    public static void record(LivingEntity target, UUID actor, int ticks, EntityActionContext.Action action) {
        if (actor == null) {
            return;
        }
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Actor", actor);
        tag.putLong("Until", target.level().getGameTime() + ticks);
        if (action != EntityActionContext.Action.HARM) tag.putString("Action", action.name());
        target.getPersistentData().put(KEY, tag);
    }

    /** The recorded actor, while the record lasts. */
    public static Optional<UUID> actor(LivingEntity target) {
        CompoundTag data = target.getPersistentData();
        if (!data.contains(KEY)) {
            return Optional.empty();
        }
        CompoundTag tag = data.getCompound(KEY);
        if (!tag.hasUUID("Actor") || tag.getLong("Until") < target.level().getGameTime()) {
            data.remove(KEY);
            return Optional.empty();
        }
        return Optional.of(tag.getUUID("Actor"));
    }

    /** Forge bus: a poison tick on an attributed target is rechecked; a refused one does nothing. */
    public static void onHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (!(target.level() instanceof ServerLevel level) || !event.getSource().is(DamageTypes.MAGIC)
                || !target.hasEffect(MobEffects.POISON)) {
            return;
        }
        Optional<UUID> actor = actor(target);
        EntityActionContext.Action action = EntityActionContext.Action.HARM;
        var stored = target.getPersistentData().getCompound(KEY);
        try { if (stored.contains("Action")) action = EntityActionContext.Action.valueOf(stored.getString("Action")); }
        catch (IllegalArgumentException unknown) { event.setCanceled(true); return; }
        if (actor.isPresent() && !EntityActionPermission.checkByPrincipal(action, level, actor.get(), target).allowed()) {
            event.setCanceled(true);
            target.removeEffect(MobEffects.POISON);
            target.getPersistentData().remove(KEY);
        }
    }
}
