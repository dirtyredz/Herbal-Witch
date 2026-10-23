package io.github.dirtyredz.herbalwitch.companions.familiars.ability;

import io.github.dirtyredz.herbalwitch.companions.familiars.ability.action.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.ability.sensing.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.menu.FamiliarMenuAccess;
import io.github.dirtyredz.herbalwitch.companions.familiars.observation.FamiliarObservationsStore;
import io.github.dirtyredz.herbalwitch.companions.familiars.work.FamiliarWork;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import java.util.*;

/** One scan job per loaded companion; explicit requests and automatic sensing share the global queue. */
public final class FamiliarAbilities {
    private static final Map<Mob, FamiliarSenseTask> JOBS = new WeakHashMap<>();
    private static final Map<Mob, FamiliarHerbCursor> CURSORS = new WeakHashMap<>();
    private static final Map<Mob, Integer> NEXT = new WeakHashMap<>();
    private FamiliarAbilities() {}
    public static boolean automatic(Mob mob) {
        return FamiliarReconciliation.record(mob).filter(r -> r.state().getBoolean("autoPowers")).isPresent()
                && mob.getHealth() >= mob.getMaxHealth() * .25F;
    }
    public static String toggle(ServerPlayer owner, UUID bond) {
        var store = FamiliarStore.get(owner.server); var record = store.record(bond).orElse(null);
        if (!FamiliarMenuAccess.detail(owner, record)) return "nearby_only";
        return store.update(record.change(s -> s.putBoolean("autoPowers", !s.getBoolean("autoPowers"))), true) ? "updated" : "unavailable";
    }
    public static String request(ServerPlayer owner, UUID bond, String key, UUID token) {
        var record = FamiliarStore.get(owner.server).record(bond).orElse(null);
        if (!FamiliarMenuAccess.detail(owner, record)) return "nearby_only";
        var mob = FamiliarReconciliation.loaded(owner.server, record);
        final FamiliarPower power;
        try { power = FamiliarPower.valueOf(key.toUpperCase(Locale.ROOT)); } catch (IllegalArgumentException invalid) { return "unavailable"; }
        if (mob == null || !FamiliarAbilityService.ready(mob, power)) return "unavailable";
        if (token == null) {
            if (power == FamiliarPower.VENOM) return "unavailable";
            queue(mob, power); return "searching";
        }
        var target = FamiliarObservationsStore.target(owner, mob, token).orElse(null);
        if (target == null) return "unavailable";
        var entity = target.entity() == null ? null : owner.serverLevel().getEntity(target.entity());
        boolean success = switch (power) {
            case GARDEN -> target.entity() == null && FamiliarGardenAid.apply(mob, target.block());
            case DISTRACT -> entity instanceof Mob victim && FamiliarCombatPowers.distract(mob, victim);
            case INTERCEPT -> entity instanceof Mob victim && FamiliarCombatPowers.intercept(mob, victim);
            case REPEL -> entity instanceof Mob victim && FamiliarCombatPowers.repel(mob, victim);
            case RETRIEVE -> entity instanceof net.minecraft.world.entity.item.ItemEntity item
                    && io.github.dirtyredz.herbalwitch.companions.familiars.ability.retrieval.FamiliarRetrieveItem.apply(owner, mob, item, target.stack());
            default -> false;
        };
        return success ? "updated" : "unavailable";
    }
    private static void queue(Mob mob, FamiliarPower power) {
        var work = FamiliarWork.in((ServerLevel) mob.level());
        var old = JOBS.remove(mob); if (old != null) work.remove(old);
        var task = new FamiliarSenseTask(mob, power, CURSORS.computeIfAbsent(mob, ignored -> new FamiliarHerbCursor()));
        JOBS.put(mob, task); work.scan(task);
    }
    public static void tick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)) return;
        var task = JOBS.get(mob);
        if (task != null) {
            if (task.done() || !task.valid()) { FamiliarWork.in(level).remove(task); JOBS.remove(mob); }
            else { FamiliarWork.in(level).scan(task); return; }
        }
        if (mob.tickCount % 40 != 0 || !automatic(mob)) return;
        var powers = FamiliarPower.values(); int start = NEXT.getOrDefault(mob, 0);
        for (int i = 0; i < powers.length; i++) {
            int index = (start + i) % powers.length; var power = powers[index];
            if (power.sensing() && FamiliarAbilityService.ready(mob, power)) {
                NEXT.put(mob, (index + 1) % powers.length); queue(mob, power); break;
            }
        }
    }
    public static void stopped(ServerStoppedEvent event) {
        JOBS.keySet().removeIf(mob -> mob.getServer() == event.getServer());
        CURSORS.keySet().removeIf(mob -> mob.getServer() == event.getServer());
        NEXT.keySet().removeIf(mob -> mob.getServer() == event.getServer());
    }
    public static void leave(net.minecraftforge.event.entity.EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(event.getLevel() instanceof ServerLevel level)) return;
        var task = JOBS.remove(mob); if (task != null) FamiliarWork.in(level).remove(task);
        CURSORS.remove(mob); NEXT.remove(mob);
    }
}
