package io.github.dirtyredz.herbalwitch.companions.familiars.bond;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import java.util.*;

/** Loaded-time progression; a single stable projection adds health without healing. */
public final class FamiliarBondProgress {
    private static final UUID HEALTH = UUID.fromString("49713e2d-2515-42aa-9c66-72fd53594179");
    private static final Map<Mob, Vec3> POSITIONS = new WeakHashMap<>();
    private FamiliarBondProgress() {}
    public static int level(int xp) { return xp >= 350 ? 5 : xp >= 200 ? 4 : xp >= 100 ? 3 : xp >= 40 ? 2 : 1; }
    public static FamiliarRecord award(FamiliarRecord record, int amount, boolean ritual) {
        if (amount <= 0) return record;
        var progress = record.state().getCompound("progress"); long clock = progress.getLong("clock");
        if (clock - progress.getLong("window") >= 24000) { progress.putLong("window", clock); progress.putInt("earned", 0); }
        int bonus = !ritual && record.state().getCompound("buffs").getInt("spiritbond") > 0 ? 1 : 0;
        int gain = Math.min(350 - record.state().getInt("xp"), ritual ? amount : Math.min(amount + bonus, Math.max(0, 10 - progress.getInt("earned"))));
        if (gain <= 0) return record;
        if (!ritual) progress.putInt("earned", progress.getInt("earned") + gain);
        return record.change(s -> { s.putInt("xp", s.getInt("xp") + gain); s.put("progress", progress); });
    }
    public static void award(Mob mob, int amount) {
        FamiliarReconciliation.record(mob).ifPresent(r -> {
            var next = award(r, amount, false);
            if (next != r) FamiliarStore.get(mob.getServer()).update(next, false);
        });
    }
    public static void project(Mob mob, FamiliarRecord record) {
        var health = mob.getAttribute(Attributes.MAX_HEALTH); if (health == null) return;
        double amount = level(record.state().getInt("xp")) - 1 + (record.state().getBoolean("heartwood") ? 4 : 0);
        var before = health.getModifier(HEALTH);
        if (before != null && before.getAmount() == amount || before == null && amount == 0) return;
        health.removeModifier(HEALTH);
        if (amount > 0) health.addTransientModifier(new AttributeModifier(HEALTH, "Familiar bond health", amount, AttributeModifier.Operation.ADDITION));
        if (mob.getHealth() > mob.getMaxHealth()) mob.setHealth(mob.getMaxHealth());
    }
    public static void tick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide) return;
        var record = FamiliarReconciliation.record(mob).orElse(null); if (record == null) { POSITIONS.remove(mob); return; }
        project(mob, record);
        var progress = record.state().getCompound("progress"); long clock = Math.min(Long.MAX_VALUE - 24000, progress.getLong("clock") + 1);
        progress.putLong("clock", clock);
        var previous = POSITIONS.put(mob, mob.position()); var owner = OnlinePlayers.find(record.owner()).orElse(null);
        double distance = previous == null ? 0 : previous.distanceTo(mob.position());
        // Discontinuities, teleport-sized jumps and unloaded/offline time grant no travel.
        if (owner != null && owner.level() == mob.level() && owner.distanceToSqr(mob) <= 16 * 16 && distance <= 2
                && distance <= mob.getDeltaMovement().length() + .15 && !mob.isPassenger()) {
            progress.putDouble("distance", Math.min(200, progress.getDouble("distance") + distance));
        }
        boolean travel = progress.getDouble("distance") >= 200 && clock - progress.getLong("travelAt") >= 1200;
        if (travel) { progress.putDouble("distance", 0); progress.putLong("travelAt", clock); }
        var next = record.change(s -> s.put("progress", progress));
        if (travel) next = award(next, 2, false);
        FamiliarStore.get(mob.getServer()).update(next, false);
    }
}
