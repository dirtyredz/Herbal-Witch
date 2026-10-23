package io.github.dirtyredz.herbalwitch.companions.familiars.preparation;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.FamiliarPreparationApplications;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.*;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarStore;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import static io.github.dirtyredz.herbalwitch.apothecary.brewing.application.FamiliarPreparationApplications.Result.*;

/** The item owns container returns. Salve continues through M8's existing care interaction. */
public final class FamiliarPreparationConsumer {
    private FamiliarPreparationConsumer() {}
    public static FamiliarPreparationApplications.Result apply(ServerPlayer owner, ProductSpec product, ItemStack stack, LivingEntity target, InteractionHand hand) {
        boolean oil = product.equals(AdvancedProductCatalog.VENOM_OIL);
        if (!oil && !FamiliarPreparationCatalog.ALL.contains(product) || !(target instanceof Mob mob)
                || !owner.isAlive() || owner.isSpectator() || mob.level() != owner.level() || !mob.isAlive() || mob.isRemoved()
                || !owner.canReach(mob, 0) || !owner.hasLineOfSight(mob) || stack != owner.getItemInHand(hand)
                || !stack.is(AdvancedPreparations.item(product))) return UNAVAILABLE;
        var record = FamiliarReconciliation.record(mob).orElse(null);
        if (record == null || !record.owner().equals(owner.getUUID())
                || !EntityActionPermission.check(EntityActionContext.Action.ANIMAL_CARE, owner, mob).allowed()) return UNAVAILABLE;
        var state = record.state(); long clock = state.getCompound("progress").getLong("clock");
        String key = oil ? "venom_oil" : product.id();
        if (state.getCompound("preparationGates").getLong(key) > clock
                || oil && FamiliarBuffRules.active(state, "venomleaf")) return UNAVAILABLE;
        var next = record.change(s -> {
            var gates = s.getCompound("preparationGates"); gates.putLong(key, clock + FamiliarBuffRules.APPLICATION_GATE); s.put("preparationGates", gates);
            if (oil) { var coating = new net.minecraft.nbt.CompoundTag(); coating.putInt("ticks", 1200); coating.putInt("hits", 8); s.put("poisonCoating", coating); }
            else {
                var buffs = s.getCompound("buffs"); buffs.putInt(product.id(), FamiliarBuffRules.DURATION); s.put("buffs", buffs);
                if (product == FamiliarPreparationCatalog.VENOMLEAF) s.remove("poisonCoating");
            }
        });
        if (!FamiliarStore.get(owner.server).update(next, false)) return UNAVAILABLE;
        FamiliarBuffEffects.project(mob, next); return APPLIED;
    }
}
