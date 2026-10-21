package io.github.dirtyredz.herbalwitch.stations.process;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.List;

/**
 * One station's running job: which recipe, which inventory counts it has reserved in place, and how
 * far it has got. The job re-matches only when the inventory's revision or the recipe generation
 * changes; a changed or removed recipe (or changed inputs) cancels the reservation and resets
 * progress, leaving every item where it is. Progress advances one per loaded server tick while
 * heat (if needed) is present and the products fit; an unloaded station simply does not tick, so
 * there is no catch-up. Completion consumes the reserved counts and adds the result and returns
 * together, once.
 */
public final class ProcessJob {
    public static final int SCHEMA = 1;

    /** Adjusts the result from the inputs it is made of (e.g. carrying the water's purity into a tea). */
    @FunctionalInterface
    public interface ResultDecorator {
        ItemStack decorate(ItemStack result, ProcessInventory inventory, int[] reservation);
    }

    private final RecipeType<? extends ProcessRecipe> type;
    private final ProcessLayout layout;
    private final boolean needsHeat;
    private final ResultDecorator decorator;

    private ProcessRecipe recipe;
    private ResourceLocation recipeId;
    private String fingerprint = "";
    private int[] reservation;
    private int progress;
    private long seenRevision = -1;
    private long seenGeneration = -1;
    private ProcessStatus status = ProcessStatus.MISSING_INPUT;

    public ProcessJob(RecipeType<? extends ProcessRecipe> type, ProcessLayout layout, boolean needsHeat, ResultDecorator decorator) {
        this.type = type;
        this.layout = layout;
        this.needsHeat = needsHeat;
        this.decorator = decorator;
    }

    public ProcessJob(RecipeType<? extends ProcessRecipe> type, ProcessLayout layout, boolean needsHeat) {
        this(type, layout, needsHeat, (result, inventory, reservation) -> result);
    }

    public ProcessStatus status() {
        return status;
    }

    public int progress() {
        return progress;
    }

    public int duration() {
        return recipe == null ? 0 : recipe.description().duration();
    }

    public ProcessRecipe recipe() {
        return recipe;
    }

    /** One loaded server tick. {@code hot} is whether heat reaches the station now. */
    public void tick(Level level, ProcessInventory inventory, boolean hot) {
        if (inventory.revision() != seenRevision || ProcessRecipeIndex.generation() != seenGeneration) {
            refresh(level, inventory);
        }
        if (recipe == null) {
            status = ProcessStatus.MISSING_INPUT;
            return;
        }
        if (needsHeat && !hot) {
            status = ProcessStatus.NEEDS_HEAT;
            return;
        }
        if (!canComplete(inventory)) {
            status = ProcessStatus.OUTPUT_BLOCKED;
            return;
        }
        status = ProcessStatus.WORKING;
        progress++;
        if (progress >= recipe.description().duration()) {
            complete(inventory);
        }
    }

    private void refresh(Level level, ProcessInventory inventory) {
        seenRevision = inventory.revision();
        seenGeneration = ProcessRecipeIndex.generation();
        var match = ProcessRecipeIndex.find(level, type, inventory, layout);
        if (match.isEmpty()) {
            cancel(inventory);
            return;
        }
        ProcessRecipe found = match.get().recipe();
        int[] counts = match.get().reservation();
        boolean same = found.getId().equals(recipeId) && found.fingerprint().equals(fingerprint) && Arrays.equals(counts, reservation);
        if (!same) {
            progress = 0;
        }
        recipe = found;
        recipeId = found.getId();
        fingerprint = found.fingerprint();
        reservation = counts;
        inventory.reserve(counts);
    }

    private void cancel(ProcessInventory inventory) {
        recipe = null;
        recipeId = null;
        fingerprint = "";
        reservation = null;
        progress = 0;
        inventory.clearReservation();
    }

    private ItemStack product(ProcessInventory inventory) {
        return decorator.decorate(recipe.description().result(), inventory, reservation);
    }

    private boolean canComplete(ProcessInventory inventory) {
        List<ProcessCompletion.Amount> returns = recipe.description().returns().stream().map(ProcessInventory::amountOf).toList();
        if (layout.output() < 0 || (!returns.isEmpty() && layout.returns() < 0)) {
            return false;
        }
        ProcessCompletion.Amount returnSlot = layout.returns() < 0 ? ProcessCompletion.Amount.EMPTY : inventory.amount(layout.returns());
        return ProcessCompletion.canComplete(inventory.amount(layout.output()), ProcessInventory.amountOf(product(inventory)),
                returnSlot, returns);
    }

    private void complete(ProcessInventory inventory) {
        ItemStack result = product(inventory);
        int[] counts = reservation;
        List<ItemStack> returns = recipe.description().returns();
        cancel(inventory);
        for (int slot = 0; slot < counts.length; slot++) {
            if (counts[slot] > 0) {
                inventory.consume(slot, counts[slot]);
            }
        }
        inventory.insertInternal(layout.output(), result);
        for (ItemStack r : returns) {
            inventory.insertInternal(layout.returns(), r);
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Schema", SCHEMA);
        if (recipeId != null) {
            tag.putString("Recipe", recipeId.toString());
            tag.putString("Fingerprint", fingerprint);
            tag.putIntArray("Reserved", reservation);
            tag.putInt("Progress", progress);
        }
        return tag;
    }

    /** Restores a saved job; it is revalidated against the loaded recipes on the next tick. */
    public void load(CompoundTag tag) {
        recipe = null;
        recipeId = null;
        fingerprint = "";
        reservation = null;
        progress = 0;
        seenRevision = -1;
        seenGeneration = -1;
        if (tag.getInt("Schema") == SCHEMA && tag.contains("Recipe")) {
            recipeId = ResourceLocation.tryParse(tag.getString("Recipe"));
            fingerprint = tag.getString("Fingerprint");
            reservation = tag.getIntArray("Reserved");
            progress = tag.getInt("Progress");
        }
    }

    /**
     * Assigns each ingredient its own matching slot so that every occupied slot is used; returns the
     * slot index per ingredient, or null. {@code matches[i][s]}: ingredient i is satisfied by slot s.
     */
    public static int[] assign(boolean[][] matches, boolean[] occupied) {
        int occupiedCount = 0;
        for (boolean o : occupied) {
            occupiedCount += o ? 1 : 0;
        }
        if (matches.length != occupiedCount) {
            return null;
        }
        int[] chosen = new int[matches.length];
        return assign(matches, occupied, new boolean[occupied.length], chosen, 0) ? chosen : null;
    }

    private static boolean assign(boolean[][] matches, boolean[] occupied, boolean[] used, int[] chosen, int i) {
        if (i == matches.length) {
            return true;
        }
        for (int s = 0; s < occupied.length; s++) {
            if (occupied[s] && !used[s] && matches[i][s]) {
                used[s] = true;
                chosen[i] = s;
                if (assign(matches, occupied, used, chosen, i + 1)) {
                    return true;
                }
                used[s] = false;
            }
        }
        return false;
    }
}
