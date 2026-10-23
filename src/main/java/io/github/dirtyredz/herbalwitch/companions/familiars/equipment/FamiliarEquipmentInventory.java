package io.github.dirtyredz.herbalwitch.companions.familiars.equipment;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarRecord;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Detached views only. The bond shard is the sole inventory authority, including while absent. */
public final class FamiliarEquipmentInventory {
    private FamiliarEquipmentInventory() {}
    private static String key(int slot) {
        return switch (slot) { case FamiliarEquipmentRules.CHARM -> "charm"; case FamiliarEquipmentRules.SPECIES -> "gear"; default -> throw new IllegalArgumentException("Not an equipment slot"); };
    }
    public static ItemStack item(FamiliarRecord record, int slot) { return ItemStack.of(record.state().getCompound("equipment").getCompound(key(slot))); }
    public static ItemStack cargo(FamiliarRecord record) { return ItemStack.of(record.state().getCompound("cargo")); }
    public static FamiliarRecord equipment(FamiliarRecord record, ItemStack charm, ItemStack gear) {
        if (!FamiliarEquipmentRules.accepts(record.species(), 0, charm) || !FamiliarEquipmentRules.accepts(record.species(), 1, gear))
            throw new IllegalArgumentException("Incompatible familiar equipment");
        var equipment = new CompoundTag();
        if (!charm.isEmpty()) equipment.put("charm", charm.save(new CompoundTag()));
        if (!gear.isEmpty()) equipment.put("gear", gear.save(new CompoundTag()));
        return record.change(state -> state.put("equipment", equipment));
    }
    public static FamiliarRecord cargo(FamiliarRecord record, ItemStack stack) {
        if (stack.getCount() > FamiliarEquipmentRules.CARGO_LIMIT) throw new IllegalArgumentException("Familiar cargo exceeds one small stack");
        return record.change(state -> { if (stack.isEmpty()) state.remove("cargo"); else state.put("cargo", stack.save(new CompoundTag())); });
    }
}
