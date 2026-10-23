package io.github.dirtyredz.herbalwitch.companions.familiars.bond;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.transaction.FamiliarPlayerReceipt;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarActionCatalog;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarOffering;
import io.github.dirtyredz.herbalwitch.magic.witch.status.*;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

/** The three permanent/progression rituals share the same durable player payment boundary. */
public final class FamiliarUpgrades {
    private FamiliarUpgrades() {}
    public static boolean eligible(ServerPlayer owner, UUID bond, BlockPos altar, AltarActionSpec action) {
        var store = FamiliarStore.get(owner.server); var record = store.record(bond).orElse(null);
        if (record == null || !store.writable(bond) || !record.owner().equals(owner.getUUID()) || FamiliarPlayerReceipt.pending(owner)) return false;
        var mob = FamiliarReconciliation.loaded(owner.server, record);
        if (mob == null || FamiliarReconciliation.record(mob).isEmpty() || mob.level() != owner.level() || !owner.isAlive()
                || !WitchStatusService.isWitch(owner) || mob.distanceToSqr(Vec3.atCenterOf(altar)) > 16
                || owner.distanceToSqr(Vec3.atCenterOf(altar)) > 16 || !owner.hasLineOfSight(mob)
                || !EntityActionPermission.check(EntityActionContext.Action.ANIMAL_CARE, owner, mob).allowed()) return false;
        var state = record.state();
        if (action == FamiliarActionCatalog.HARMONY) return state.getInt("xp") < 350 && (!state.contains("harmonyAt")
                || state.getCompound("progress").getLong("clock") >= state.getLong("harmonyAt"));
        return FamiliarBondProgress.level(state.getInt("xp")) >= 3 && (action == FamiliarActionCatalog.HEARTWOOD && !state.getBoolean("heartwood")
                || action == FamiliarActionCatalog.SPIRIT_WELL && !state.getBoolean("spiritWell"));
    }
    public static boolean apply(ServerPlayer owner, UUID bond, BlockPos altar, AltarActionSpec action, AltarOffering offering) {
        if (!eligible(owner, bond, altar, action) || !offering.intact(owner) || WitchStatusService.check(owner, action.energy() * 100) != EnergyService.Result.OK) return false;
        var store = FamiliarStore.get(owner.server);
        boolean success = io.github.dirtyredz.herbalwitch.companions.familiars.bond.transaction.FamiliarPaidStateChange.commit(owner, bond, action.energy() * 100, offering, current -> {
            var next = action == FamiliarActionCatalog.HARMONY ? FamiliarBondProgress.award(current, 10, true) : current;
            return next.change(state -> {
                if (action == FamiliarActionCatalog.HEARTWOOD) state.putBoolean("heartwood", true);
                else if (action == FamiliarActionCatalog.SPIRIT_WELL) state.putBoolean("spiritWell", true);
                else state.putLong("harmonyAt", state.getCompound("progress").getLong("clock") + 24000);
            });
        });
        if (success) {
            var record = store.record(bond).orElseThrow(); var mob = FamiliarReconciliation.loaded(owner.server, record);
            if (mob != null) FamiliarBondProgress.project(mob, record);
        }
        return success;
    }
}
