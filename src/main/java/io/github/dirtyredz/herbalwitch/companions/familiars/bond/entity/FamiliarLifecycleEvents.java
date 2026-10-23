package io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarStore;
import net.minecraft.world.entity.*;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;

public final class FamiliarLifecycleEvents {
    private FamiliarLifecycleEvents() {}
    public static void capabilities(RegisterCapabilitiesEvent event) { event.register(FamiliarBinding.class); }
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Mob mob && EntityType.getKey(mob.getType()).toString().startsWith("herbalwitch:familiars/entity/")) {
            var provider = new FamiliarBindingProvider(); event.addCapability(FamiliarBinding.ID, provider); event.addListener(provider::invalidate);
        }
    }
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        FamiliarStore.get(event.getServer()).indexBatch(); FamiliarReconciliation.indexed(event.getServer());
    }
    public static void stopped(ServerStoppedEvent event) {
        FamiliarReconciliation.stopped(event.getServer()); FamiliarStore.stopped(event.getServer());
    }
    /** Suspend without setting a persistent NoAI bit while the complete identity index is unavailable. */
    public static void living(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Mob mob && FamiliarReconciliation.suspended(mob)) event.setCanceled(true);
    }
    public static void attack(net.minecraftforge.event.entity.living.LivingAttackEvent event) {
        if (event.getEntity() instanceof Mob mob && FamiliarReconciliation.suspended(mob)) event.setCanceled(true);
    }
    public static void observe(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide || mob.tickCount % 10 != 0) return;
        FamiliarReconciliation.record(mob).ifPresent(record -> {
            var state = record.state(); String dimension = mob.level().dimension().location().toString();
            if (state.getLong("position") == mob.blockPosition().asLong() && state.getString("dimension").equals(dimension)
                    && state.getFloat("health") == mob.getHealth() && state.getFloat("maxHealth") == mob.getMaxHealth()
                    && state.getString("name").equals(mob.getName().getString())) return;
            FamiliarStore.get(mob.getServer()).update(record.change(next -> {
                next.putLong("position", mob.blockPosition().asLong()); next.putString("dimension", dimension);
                next.putFloat("health", mob.getHealth()); next.putFloat("maxHealth", mob.getMaxHealth());
                next.putString("name", mob.getName().getString());
            }), false);
        });
    }
}
