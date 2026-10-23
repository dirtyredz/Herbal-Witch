package io.github.dirtyredz.herbalwitch.companions.familiars.preparation;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationEffects;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarRecord;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.FamiliarCommand;
import io.github.dirtyredz.herbalwitch.companions.familiars.home.FamiliarHomeTarget;
import net.minecraft.nbt.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import java.util.*;

/** Canonical timers project stable modifiers and short vanilla/M7 effects; external effects remain owned by their source. */
public final class FamiliarBuffEffects {
    private static final UUID HEALTH = UUID.fromString("ad0b6641-aa6c-4c0f-9f9a-1785f06290d1"), ARMOR = UUID.fromString("44613d2a-e0cd-459a-91c0-5dc59a61f762"), SPEED = UUID.fromString("964454f8-79da-4d37-b87f-b1119ba11bd9");
    private static final Map<Mob, Map<MobEffect, MobEffectInstance>> OWNED = new WeakHashMap<>();
    private FamiliarBuffEffects() {}
    public static boolean homeGuard(Mob mob, FamiliarRecord record) {
        if (record.command() != FamiliarCommand.PROTECT_HOME || !(mob.level() instanceof net.minecraft.server.level.ServerLevel level)
                || level.getEntity(mob.getUUID()) != mob) return false;
        var home = FamiliarHomeTarget.inspect(mob, record);
        int radius = io.github.dirtyredz.herbalwitch.companions.familiars.config.FamiliarServerConfig.current().guardRadius();
        return home.position() != null && mob.position().distanceToSqr(home.position()) <= radius * radius;
    }
    public static void project(Mob mob, FamiliarRecord record) {
        var state = record.state(); boolean guardian = FamiliarBuffRules.active(state, "guardian");
        modifier(mob, Attributes.MAX_HEALTH, HEALTH, guardian ? 4 : 0, AttributeModifier.Operation.ADDITION);
        modifier(mob, Attributes.ARMOR, ARMOR, (guardian ? 2 : 0) + (FamiliarBuffRules.active(state, "hearthkeeper") && homeGuard(mob, record) ? 2 : 0), AttributeModifier.Operation.ADDITION);
        modifier(mob, Attributes.MOVEMENT_SPEED, SPEED, FamiliarBuffRules.active(state, "swiftpaw") ? .2 : 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        if (mob.getHealth() > mob.getMaxHealth()) mob.setHealth(mob.getMaxHealth());
        effect(mob, PreparationEffects.CONCEALMENT.get(), FamiliarBuffRules.active(state, "moonveil"));
        effect(mob, MobEffects.FIRE_RESISTANCE, FamiliarBuffRules.active(state, "emberguard"));
    }
    private static void modifier(Mob mob, Attribute attribute, UUID id, double amount, AttributeModifier.Operation operation) {
        var value = mob.getAttribute(attribute); if (value == null) return; var current = value.getModifier(id);
        if (current != null && current.getAmount() == amount || current == null && amount == 0) return;
        value.removeModifier(id); if (amount != 0) value.addTransientModifier(new AttributeModifier(id, "Familiar preparation", amount, operation));
    }
    private static void effect(Mob mob, MobEffect effect, boolean active) {
        var owned = OWNED.computeIfAbsent(mob, ignored -> new HashMap<>()); var current = mob.getEffect(effect);
        boolean ours = current != null && current == owned.get(effect) && current.getDuration() <= 2;
        if (!active) { if (ours) mob.removeEffect(effect); owned.remove(effect); return; }
        if (current == null || ours) {
            mob.addEffect(new MobEffectInstance(effect, 2, 0, false, false, true)); owned.put(effect, mob.getEffect(effect));
        }
    }
    /** Omit only our known short projections from the payload; the bond is their single timer authority. */
    public static void stripProjected(Mob mob, CompoundTag payload) {
        var owned = OWNED.get(mob); if (owned == null || !payload.contains("ActiveEffects", Tag.TAG_LIST)) return;
        var effects = payload.getList("ActiveEffects", Tag.TAG_COMPOUND);
        for (int i = effects.size() - 1; i >= 0; i--) {
            var instance = MobEffectInstance.load(effects.getCompound(i)); if (instance == null) continue;
            var current = mob.getEffect(instance.getEffect());
            if (current != null && current == owned.get(instance.getEffect()) && current.getDuration() <= 2) effects.remove(i);
        }
    }
}
