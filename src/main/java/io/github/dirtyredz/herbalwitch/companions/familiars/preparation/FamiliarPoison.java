package io.github.dirtyredz.herbalwitch.companions.familiars.preparation;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.combat.CombatEvents;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEffects;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarStore;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.platform.permission.PrincipalPermission;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import java.util.UUID;

public final class FamiliarPoison {
    private FamiliarPoison() {}
    public static EntityActionPermission.Denial permission(EntityActionContext.Action action, UUID owner, LivingEntity target) {
        if (action != EntityActionContext.Action.FAMILIAR_HARM) return EntityActionPermission.Denial.NONE;
        if (!(target instanceof Enemy) || target instanceof OwnableEntity pet && pet.getOwnerUUID() != null
                || target.getType().is(ControlEffects.IMMUNE)) return EntityActionPermission.Denial.PROTECTED;
        return target.level() instanceof ServerLevel level ? PrincipalPermission.mayUse(level, owner, target.blockPosition()) : EntityActionPermission.Denial.UNSUPPORTED;
    }
    public static void damage(LivingDamageEvent event) {
        if (event.getAmount() <= 0 || !(event.getSource().getEntity() instanceof Mob familiar)
                || event.getSource().getDirectEntity() != familiar || familiar.level().isClientSide) return;
        var record = FamiliarReconciliation.record(familiar).orElse(null); if (record == null) return;
        var state = record.state(); long clock = state.getCompound("progress").getLong("clock");
        boolean leaf = FamiliarBuffRules.active(state, "venomleaf"); var coating = state.getCompound("poisonCoating");
        if ((!leaf && (coating.getInt("ticks") <= 0 || coating.getInt("hits") <= 0)) || leaf && state.getLong("poisonAt") > clock) return;
        if (!EntityActionPermission.checkByPrincipal(EntityActionContext.Action.FAMILIAR_HARM, (ServerLevel) familiar.level(), record.owner(), event.getEntity()).allowed()) return;
        boolean applied = CombatEvents.applyVenom(familiar, event.getEntity(), record.owner(), EntityActionContext.Action.FAMILIAR_HARM);
        if (leaf && !applied) return;
        FamiliarStore.get(familiar.getServer()).update(record.change(s -> {
            s.putLong("poisonAt", clock + 20);
            if (!leaf) { coating.putInt("hits", coating.getInt("hits") - 1); if (coating.getInt("hits") <= 0) s.remove("poisonCoating"); else s.put("poisonCoating", coating); }
        }), false);
    }
}
