package io.github.dirtyredz.herbalwitch.magic.charm.equipment;

import io.github.dirtyredz.herbalwitch.magic.charm.item.ProtectiveCharmItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Which charm is active on a wearer: exactly one, never an aggregate. The off hand comes first;
 * otherwise, when an equipment adapter is installed (Curios), the first charm in its slots in its own
 * deterministic order, inspecting at most {@link #MAX_SLOTS} slots. Charms elsewhere are inactive and
 * do not drain.
 */
public final class CharmEquipment {
    /** Most extra equipment slots inspected per lookup. */
    public static final int MAX_SLOTS = 8;
    private static volatile BiFunction<LivingEntity, Predicate<ItemStack>, List<ItemStack>> extraSlots = (wearer, test) -> List.of();

    private CharmEquipment() {
    }

    /** Installed by an optional equipment adapter: the matching stacks of its slots, in order, at most {@link #MAX_SLOTS} inspected. */
    public static void extraSlots(BiFunction<LivingEntity, Predicate<ItemStack>, List<ItemStack>> lookup) {
        extraSlots = lookup;
    }

    public static boolean charm(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ProtectiveCharmItem;
    }

    /** The one active charm stack (live, mutable), if any. */
    public static Optional<ItemStack> active(LivingEntity wearer) {
        ItemStack offhand = wearer.getOffhandItem();
        if (charm(offhand)) {
            return Optional.of(offhand);
        }
        return extraSlots.apply(wearer, CharmEquipment::charm).stream().filter(CharmEquipment::charm).findFirst();
    }
}
