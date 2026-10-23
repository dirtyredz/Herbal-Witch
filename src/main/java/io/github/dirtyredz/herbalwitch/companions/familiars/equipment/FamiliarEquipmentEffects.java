package io.github.dirtyredz.herbalwitch.companions.familiars.equipment;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraftforge.event.entity.living.LivingEvent;
import java.util.UUID;

/** One stable transient modifier, recalculated from the record without touching native armor. */
public final class FamiliarEquipmentEffects {
    public static final UUID HARNESS = UUID.fromString("2019f96c-91df-4394-9432-3103a4450a7a");
    private FamiliarEquipmentEffects() {}
    public static void tick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide || !FamiliarReconciliation.governed(mob)) return;
        var armor = mob.getAttribute(Attributes.ARMOR); if (armor == null) return;
        var record = FamiliarReconciliation.record(mob).orElse(null);
        boolean equipped = record != null && record.species().equals("wolf")
                && FamiliarEquipmentRules.equipped("wolf", FamiliarEquipmentInventory.item(record, FamiliarEquipmentRules.SPECIES));
        if (equipped && armor.getModifier(HARNESS) == null) armor.addTransientModifier(new AttributeModifier(HARNESS, "Familiar wolf harness", 2, AttributeModifier.Operation.ADDITION));
        else if (!equipped) armor.removeModifier(HARNESS);
    }
}
