package io.github.dirtyredz.herbalwitch.companions.familiars.ability;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEffects;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.preparation.FamiliarBuffEffects;
import io.github.dirtyredz.herbalwitch.magic.witch.status.*;
import io.github.dirtyredz.herbalwitch.platform.permission.PrincipalPermission;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import java.util.function.BooleanSupplier;

/** All powers validate again when scheduled work executes, and pay only for a concrete result. */
public final class FamiliarAbilityService {
    private FamiliarAbilityService() {}
    public static long clock(FamiliarRecord record) { return record.state().getCompound("progress").getLong("clock"); }
    public static boolean guarding(FamiliarRecord record) { return record.command() == FamiliarCommand.GUARD_WITCH || record.command() == FamiliarCommand.PROTECT_HOME; }
    public static boolean ready(Mob mob, FamiliarPower power) {
        var record = FamiliarReconciliation.record(mob).orElse(null);
        if (record == null || record.command() == FamiliarCommand.PASSIVE || mob.isNoAi() || ControlEffects.held(mob) || !power.available(mob, record)
                || power.guardOnly() && !guarding(record) || record.state().getCompound("powerCooldowns").getLong(power.key()) > clock(record)
                || !PrincipalPermission.mayUse((ServerLevel) mob.level(), record.owner(), mob.blockPosition()).allowed()) return false;
        var owner = OnlinePlayers.find(record.owner()).orElse(null);
        if (owner != null) return owner.isAlive() && !owner.isSpectator() && owner.level() == mob.level() && WitchStatusService.check(owner, power.cost(record)) == EnergyService.Result.OK;
        return power.guardOnly() && record.command() == FamiliarCommand.PROTECT_HOME && FamiliarOfflineGuards.allowed(mob, record)
                && FamiliarBuffEffects.homeGuard(mob, record) && record.state().getInt("reserve") >= power.cost(record);
    }
    public static boolean perform(Mob mob, FamiliarPower power, BooleanSupplier result) {
        return perform(mob, power, ignored -> {}, result);
    }
    public static boolean perform(Mob mob, FamiliarPower power, java.util.function.Consumer<net.minecraft.nbt.CompoundTag> change, BooleanSupplier result) {
        if (!ready(mob, power)) return false;
        var store = FamiliarStore.get(mob.getServer()); var record = FamiliarReconciliation.record(mob).orElseThrow();
        var owner = OnlinePlayers.find(record.owner()).orElse(null); int units = power.cost(record);
        var next = record.change(s -> {
            change.accept(s);
            var cooldowns = s.getCompound("powerCooldowns"); cooldowns.putLong(power.key(), clock(record) + power.cooldown(mob, record)); s.put("powerCooldowns", cooldowns);
            if (owner == null) s.putInt("reserve", s.getInt("reserve") - units);
        });
        try { FamiliarCodec.encode(next, io.github.dirtyredz.herbalwitch.companions.familiars.config.FamiliarServerConfig.current().recordBytes()); }
        catch (java.io.IOException | RuntimeException tooLarge) { return false; }
        BooleanSupplier apply = () -> {
            if (!result.getAsBoolean()) return false;
            // Action implementations do not mutate this bond; their concrete effect and cooldown commit together on this thread.
            return store.update(next, false);
        };
        return owner == null ? apply.getAsBoolean() : WitchStatusService.spend(owner, units, apply::getAsBoolean);
    }
}
