package io.github.dirtyredz.herbalwitch.companions.familiars.equipment;

import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarEquipmentCatalog;
import io.github.dirtyredz.herbalwitch.magic.charm.equipment.CharmEquipment;
import io.github.dirtyredz.herbalwitch.magic.charm.item.ProtectiveCharmItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/** Every menu path delegates here; task cargo is deliberately not an editable slot. */
public final class FamiliarEquipmentRules {
    public static final int CHARM = 0, SPECIES = 1, SLOTS = 2, CARGO_LIMIT = 16;
    private FamiliarEquipmentRules() {}
    public static boolean accepts(String species, int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOTS) return false;
        if (stack.isEmpty()) return true;
        if (stack.getCount() != 1) return false;
        if (slot == CHARM) return CharmEquipment.charm(stack) && ProtectiveCharmItem.remaining(stack) > 0
                && io.github.dirtyredz.herbalwitch.magic.charm.registry.CharmItems.all().contains(stack.getItem());
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.getNamespace().equals("herbalwitch")
                && FamiliarEquipmentCatalog.ALL.stream().anyMatch(gear -> gear.accepts(species) && gear.path().equals(id.getPath()));
    }
    public static boolean equipped(String species, ItemStack stack) { return !stack.isEmpty() && accepts(species, SPECIES, stack); }
    public static int cargoCapacity(String species, ItemStack gear) { return equipped(species, gear) ? 16 : 8; }
    public static int cooldown(String species, String action, ItemStack gear, int base) {
        if (!equipped(species, gear)) return base;
        return switch (species + "/" + action) {
            case "cat/threat", "owl/herb", "snake/venom", "bat/dark" -> 160;
            case "rabbit/garden" -> 480;
            default -> base;
        };
    }
}
