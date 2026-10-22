package io.github.dirtyredz.herbalwitch.platform.compat.curios;

import io.github.dirtyredz.herbalwitch.magic.charm.equipment.CharmEquipment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;

/**
 * Curios support for charms (Curios 5.x for Forge 1.20.1; compile-only API, loaded only when Curios
 * is present). It offers the wearer's Curios slots to {@link CharmEquipment} in a deterministic order
 * (slot types by identifier, then index), inspecting at most {@link CharmEquipment#MAX_SLOTS} slots;
 * the off hand still comes first and only one charm is ever active. Charms are allowed in the
 * {@code charm} slot type through the {@code curios:charm} item tag.
 */
public final class CuriosCharmAdapter {
    private CuriosCharmAdapter() {
    }

    public static void register() {
        CharmEquipment.extraSlots(CuriosCharmAdapter::stacks);
    }

    /** Matching stacks in the wearer's Curios slots, in order, inspecting at most the slot budget. */
    public static List<ItemStack> stacks(LivingEntity wearer, Predicate<ItemStack> test) {
        Optional<ICuriosItemHandler> handler = CuriosApi.getCuriosInventory(wearer).resolve();
        if (handler.isEmpty()) {
            return List.of();
        }
        Map<String, ICurioStacksHandler> sorted = new TreeMap<>(handler.get().getCurios());
        List<ItemStack> out = new ArrayList<>();
        int inspected = 0;
        for (ICurioStacksHandler slots : sorted.values()) {
            IDynamicStackHandler stacks = slots.getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                if (inspected++ >= CharmEquipment.MAX_SLOTS) {
                    return out;
                }
                ItemStack stack = stacks.getStackInSlot(i);
                if (test.test(stack)) {
                    out.add(stack);
                }
            }
        }
        return out;
    }
}
