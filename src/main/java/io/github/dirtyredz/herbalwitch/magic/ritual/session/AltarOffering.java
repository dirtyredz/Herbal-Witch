package io.github.dirtyredz.herbalwitch.magic.ritual.session;

import io.github.dirtyredz.herbalwitch.platform.inventory.MissingIngredients;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An altar offering taken from the payer's own main inventory and offhand only (never containers,
 * never a second inventory): at the start the exact stacks are captured by slot, item, data and
 * count; while the ritual runs they must stay put (moving or replacing one cancels it); at
 * completion they are checked again and consumed in one step. Nothing is reserved or moved before
 * then, so a cancelled ritual has consumed nothing.
 */
public final class AltarOffering {
    /** One resolved requirement: the item, how many, and (for a written book) that it is a valid signed book. */
    public record Requirement(Item item, int count) {
        public boolean accepts(ItemStack stack) {
            if (stack.isEmpty() || !stack.is(item)) {
                return false;
            }
            return item != Items.WRITTEN_BOOK || WrittenBookItem.makeSureTagIsValid(stack.getTag());
        }
    }

    /** A captured stack: its slot, a copy of what it held, and how many the offering takes from it. */
    public record Taken(int slot, ItemStack snapshot, int count) {
    }

    /** What a requirement has against what it needs, for the screen. */
    public record Count(int have, int need) {
    }

    private final List<Taken> taken;

    private AltarOffering(List<Taken> taken) {
        this.taken = List.copyOf(taken);
    }

    /** An offering that takes nothing (meditation, an administrative designation). */
    public static AltarOffering none() {
        return new AltarOffering(List.of());
    }

    public List<Taken> taken() {
        return taken;
    }

    /** The action's requirements, resolved against the item registry. */
    public static List<Requirement> requirements(AltarActionSpec action) {
        List<Requirement> out = new ArrayList<>();
        for (AltarActionSpec.Offering o : action.offering()) {
            Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(o.item()));
            if (item == null || item == Items.AIR) {
                throw new IllegalStateException("offering item " + o.item() + " is not registered");
            }
            out.add(new Requirement(item, o.count()));
        }
        return out;
    }

    /** The inventory slots an offering may come from: main inventory, then offhand. */
    public static List<Integer> slots() {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            out.add(i);
        }
        out.add(Inventory.SLOT_OFFHAND);
        return out;
    }

    /**
     * Picks one stack per requirement (each requirement needs its count from one stack), first fit in
     * slot order; empty when any requirement is missing.
     */
    public static Optional<AltarOffering> capture(Player payer, List<Requirement> requirements) {
        Inventory inventory = payer.getInventory();
        Map<Integer, Integer> used = new LinkedHashMap<>();
        List<Taken> taken = new ArrayList<>();
        for (Requirement r : requirements) {
            Taken pick = null;
            for (int slot : slots()) {
                ItemStack stack = inventory.getItem(slot);
                if (r.accepts(stack) && stack.getCount() - used.getOrDefault(slot, 0) >= r.count()) {
                    pick = new Taken(slot, stack.copy(), r.count());
                    break;
                }
            }
            if (pick == null) {
                return Optional.empty();
            }
            used.merge(pick.slot(), pick.count(), Integer::sum);
            taken.add(pick);
        }
        return Optional.of(new AltarOffering(taken));
    }

    /** Whether every captured stack is still where it was: the same item and data, and enough of it. */
    public boolean intact(Player payer) {
        Map<Integer, Integer> needed = new LinkedHashMap<>();
        taken.forEach(t -> needed.merge(t.slot(), t.count(), Integer::sum));
        for (Taken t : taken) {
            ItemStack now = payer.getInventory().getItem(t.slot());
            if (!ItemStack.isSameItemSameTags(now, t.snapshot()) || now.getCount() < needed.get(t.slot())) {
                return false;
            }
        }
        return true;
    }

    /** Copies of the slots this offering touches, for a rollback. */
    public Map<Integer, ItemStack> backup(Player payer) {
        Map<Integer, ItemStack> out = new LinkedHashMap<>();
        taken.forEach(t -> out.putIfAbsent(t.slot(), payer.getInventory().getItem(t.slot()).copy()));
        return out;
    }

    public static void restore(Player payer, Map<Integer, ItemStack> backup) {
        backup.forEach((slot, stack) -> payer.getInventory().setItem(slot, stack.copy()));
    }

    /** Removes the captured items (call only after {@link #intact}). */
    public void consume(Player payer) {
        for (Taken t : taken) {
            payer.getInventory().getItem(t.slot()).shrink(t.count());
        }
        payer.getInventory().setChanged();
    }

    /**
     * Whether one grimoire fits the recipient's main inventory, after this offering is consumed when
     * the payer is the recipient. The gift needs an empty slot (it never stacks); nothing is dropped.
     */
    public boolean giftFits(Player recipient, boolean payerIsRecipient) {
        Inventory inventory = recipient.getInventory();
        Map<Integer, Integer> removed = new LinkedHashMap<>();
        if (payerIsRecipient) {
            taken.forEach(t -> removed.merge(t.slot(), t.count(), Integer::sum));
        }
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty() || stack.getCount() - removed.getOrDefault(i, 0) <= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Have and need per requirement over the main inventory and offhand, with the grimoire's
     * shopping-list allocator (one stack variant per requirement, shared stacks counted once).
     */
    public static List<Count> counts(Player player, List<Requirement> requirements) {
        List<ItemStack> variants = new ArrayList<>();
        for (int slot : slots()) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack same = variants.stream().filter(v -> ItemStack.isSameItemSameTags(v, stack)).findFirst().orElse(null);
            if (same == null) {
                variants.add(stack.copy());
            } else {
                same.grow(stack.getCount());
            }
        }
        int[] required = requirements.stream().mapToInt(Requirement::count).toArray();
        boolean[][] matches = new boolean[requirements.size()][variants.size()];
        for (int r = 0; r < requirements.size(); r++) {
            for (int v = 0; v < variants.size(); v++) {
                matches[r][v] = requirements.get(r).accepts(variants.get(v));
            }
        }
        MissingIngredients.Allocation allocation = MissingIngredients.allocate(required, matches,
                variants.stream().mapToInt(ItemStack::getCount).toArray());
        List<Count> out = new ArrayList<>();
        for (int r = 0; r < required.length; r++) {
            out.add(new Count(allocation.available(r), required[r]));
        }
        return out;
    }
}
