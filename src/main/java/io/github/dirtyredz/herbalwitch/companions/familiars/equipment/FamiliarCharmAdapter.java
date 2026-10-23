package io.github.dirtyredz.herbalwitch.companions.familiars.equipment;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarStore;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.magic.charm.equipment.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingEvent;
import java.util.Optional;

/** Canonical charm selection feeds M7's single damage handler; creature Curios/off-hand slots are never inspected. */
public final class FamiliarCharmAdapter {
    private FamiliarCharmAdapter() {}
    public static void install() {
        CharmEquipment.ownedSlot(wearer -> wearer instanceof Mob mob && FamiliarReconciliation.governed(mob), wearer -> {
            var record = FamiliarReconciliation.record((Mob) wearer).orElse(null);
            return record == null ? Optional.empty() : Optional.of(FamiliarEquipmentInventory.item(record, FamiliarEquipmentRules.CHARM));
        });
    }
    public static void tick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide) return;
        var record = FamiliarReconciliation.record(mob).orElse(null); if (record == null) return;
        var charm = FamiliarEquipmentInventory.item(record, FamiliarEquipmentRules.CHARM); if (charm.isEmpty()) return;
        var result = CharmCharge.wear(charm, record.state().getInt("charmTicks"), 1);
        var next = FamiliarEquipmentInventory.equipment(record, charm, FamiliarEquipmentInventory.item(record, FamiliarEquipmentRules.SPECIES))
                .change(state -> state.putInt("charmTicks", result.partialTicks()));
        if (FamiliarStore.get(mob.getServer()).update(next, false) && result.broke())
            mob.level().playSound(null, mob.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.NEUTRAL, 0.8F, 1.2F);
    }
}
