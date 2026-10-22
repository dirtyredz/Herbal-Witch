package io.github.dirtyredz.herbalwitch.companions.animals.trust;

import io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalServerConfig;
import net.minecraft.world.entity.Mob;
import java.util.UUID;

/** Named server-only care mutations. Interaction services validate authority and inventory before calling. */
public final class AnimalTrustService {
    private AnimalTrustService() {}

    public static AnimalRelationshipView relationship(Mob mob, UUID viewer) {
        return AnimalCapability.of(mob).flatMap(AnimalCapability::state).map(s -> s.relationship(viewer)).orElse(AnimalRelationshipView.EMPTY);
    }

    public static long clock(Mob mob) { return AnimalCapability.of(mob).flatMap(AnimalCapability::state).map(AnimalState::clock).orElse(0L); }

    public static boolean feed(Mob mob, UUID player) {
        if (mob.level().isClientSide) return false;
        return AnimalCapability.of(mob).filter(AnimalCapability::writable).map(cap -> {
            boolean changed = cap.initialize().feed(player, AnimalServerConfig.current().relationshipsPerAnimal());
            if (changed) cap.changed();
            return changed;
        }).orElse(false);
    }

    public static void healed(Mob mob) {
        mutate(mob, state -> state.healUntil = state.clock + TrustRules.HEAL_INTERVAL);
    }

    public static void calm(Mob mob, UUID actor) {
        mutate(mob, state -> { state.calmSource = actor; state.calmUntil = state.clock + TrustRules.CALM_TICKS; });
    }

    public static void endCalm(Mob mob) {
        if (!mob.level().isClientSide) AnimalCapability.of(mob).filter(AnimalCapability::writable).ifPresent(cap -> cap.state().ifPresent(state -> {
            if (state.calmSource != null) { state.endCalm(); cap.changed(); }
        }));
    }

    public static boolean keeper(Mob mob, UUID actor) {
        if (mob.level().isClientSide || actor == null) return false;
        return AnimalCapability.of(mob).filter(AnimalCapability::writable).map(cap -> {
            AnimalState state = cap.initialize();
            if (state.keeper != null || !TrustRules.earned(state.relationship(actor), false)) return false;
            state.keeper = actor; cap.changed(); return true;
        }).orElse(false);
    }

    /** Transport's reserved generation; never an owner or relationship change. */
    public static boolean generation(Mob mob, long expected, long value) {
        if (value < 1 || value <= expected) return false;
        return AnimalCapability.of(mob).filter(AnimalCapability::writable).map(cap -> {
            AnimalState state = cap.initialize();
            if (state.generation != expected) return false;
            state.generation = value; cap.changed(); return true;
        }).orElse(false);
    }

    private static void mutate(Mob mob, java.util.function.Consumer<AnimalState> change) {
        if (!mob.level().isClientSide) AnimalCapability.of(mob).filter(AnimalCapability::writable).ifPresent(cap -> {
            change.accept(cap.initialize()); cap.changed();
        });
    }
}
