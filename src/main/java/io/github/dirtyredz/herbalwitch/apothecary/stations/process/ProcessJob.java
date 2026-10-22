package io.github.dirtyredz.herbalwitch.apothecary.stations.process;

import io.github.dirtyredz.herbalwitch.apothecary.stations.process.execution.ProcessCommit;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.execution.ProcessExecutionPolicy;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipeIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.Optional;

/**
 * One station's running job: which recipe, which inventory counts it reserves in place, and how far
 * it has got. The job re-matches only when the inventory's revision or the recipe generation
 * changes; a changed or removed recipe (or changed inputs) drops the reservation, forgets any
 * authorisation and resets progress, leaving every item where it is. Progress advances one per loaded
 * server tick while the execution policy allows it, heat (if needed) is present and the products fit;
 * an unloaded station simply does not tick, so there is no catch-up. Completion goes through the
 * policy, which applies the {@link ProcessCommit} (consume the reserved counts, add the result and
 * returns, once) and settles any payment around it.
 *
 * <p>Saved jobs carry a schema: 1 (ordinary, Milestone 3) loads unchanged; 2 adds the policy's
 * authorisation. A job of an unknown schema is kept byte for byte and does nothing until the
 * station's contents change, so a newer save is never misread and its items stay removable.
 */
public final class ProcessJob {
    public static final int SCHEMA = 2;
    public static final int LEGACY_SCHEMA = 1;

    /**
     * Adjusts the result from the inputs it is made of (carrying a water's purity into a tea). Pure:
     * capacity checks call it before completion, so it must never mutate or spend anything.
     */
    @FunctionalInterface
    public interface ResultDecorator {
        ItemStack decorate(ItemStack result, ProcessInventory inventory, int[] reservation);
    }

    private final RecipeType<? extends ProcessRecipe> type;
    private final ProcessLayout layout;
    private final boolean needsHeat;
    private final ResultDecorator decorator;
    private final ProcessExecutionPolicy policy;

    private ProcessRecipe recipe;
    private ResourceLocation recipeId;
    private String fingerprint = "";
    private int[] reservation;
    private boolean held;
    private int progress;
    private long seenRevision = -1;
    private long seenGeneration = -1;
    private ProcessStatus status = ProcessStatus.MISSING_INPUT;
    private CompoundTag unsupported;

    public ProcessJob(RecipeType<? extends ProcessRecipe> type, ProcessLayout layout, boolean needsHeat, ResultDecorator decorator,
                      ProcessExecutionPolicy policy) {
        this.type = type;
        this.layout = layout;
        this.needsHeat = needsHeat;
        this.decorator = decorator;
        this.policy = policy;
    }

    public ProcessJob(RecipeType<? extends ProcessRecipe> type, ProcessLayout layout, boolean needsHeat, ResultDecorator decorator) {
        this(type, layout, needsHeat, decorator, ProcessExecutionPolicy.ORDINARY);
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

    /** The matched recipe, or null. */
    public ProcessRecipe recipe() {
        return recipe;
    }

    public ProcessExecutionPolicy policy() {
        return policy;
    }

    /** Whether a saved job of an unsupported schema is being preserved (the station does nothing). */
    public boolean unsupported() {
        return unsupported != null;
    }

    /** One loaded server tick at {@code pos}. {@code hot} is whether heat reaches the station now. */
    public void tick(Level level, BlockPos pos, ProcessInventory inventory, boolean hot) {
        if (unsupported != null) {
            if (inventory.revision() == 0) {
                status = ProcessStatus.MISSING_INPUT;
                return;
            }
            unsupported = null; // the contents changed: the preserved job is gone with them
        }
        if (inventory.revision() != seenRevision || ProcessRecipeIndex.generation() != seenGeneration) {
            refresh(level, inventory);
        }
        if (recipe == null) {
            status = ProcessStatus.MISSING_INPUT;
            return;
        }
        ProcessExecutionPolicy.Site site = new ProcessExecutionPolicy.Site(level, pos);
        hold(inventory, policy.holds(recipe, site));
        Optional<ProcessStatus> gate = policy.gate(recipe, site);
        if (gate.isPresent()) {
            status = gate.get();
            return;
        }
        if (needsHeat && !hot) {
            status = ProcessStatus.NEEDS_HEAT;
            return;
        }
        if (!commit(inventory).fits()) {
            status = ProcessStatus.OUTPUT_BLOCKED;
            return;
        }
        status = ProcessStatus.WORKING;
        if (progress < recipe.description().duration()) {
            progress++;
        }
        if (progress >= recipe.description().duration()) {
            complete(inventory, site);
        }
    }

    /** Locks or releases the matched counts; losing the hold (an authorisation ended) restarts progress. */
    private void hold(ProcessInventory inventory, boolean hold) {
        if (hold == held) {
            return;
        }
        held = hold;
        if (hold) {
            inventory.reserve(reservation);
        } else {
            inventory.clearReservation();
            progress = 0;
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
            policy.reset();
        }
        recipe = found;
        recipeId = found.getId();
        fingerprint = found.fingerprint();
        reservation = counts;
        if (held) {
            inventory.reserve(counts);
        }
    }

    /** Drops the recipe, its reservation, its progress and any authorisation; every item stays. */
    public void cancel(ProcessInventory inventory) {
        recipe = null;
        recipeId = null;
        fingerprint = "";
        reservation = null;
        progress = 0;
        held = false;
        inventory.clearReservation();
        policy.reset();
    }

    /**
     * Ends an authorised batch without completing it (the operator's cancel): progress restarts and the
     * inputs are released; the recipe stays matched, so a new Start can follow.
     */
    public void release(ProcessInventory inventory) {
        policy.reset();
        progress = 0;
        held = false;
        inventory.clearReservation();
    }

    private ProcessCommit commit(ProcessInventory inventory) {
        ItemStack result = decorator.decorate(recipe.description().result(), inventory, reservation);
        return new ProcessCommit(inventory, layout, reservation, result, recipe.description().returns());
    }

    private void complete(ProcessInventory inventory, ProcessExecutionPolicy.Site site) {
        ProcessCommit commit = commit(inventory);
        Optional<ProcessStatus> refused = policy.complete(recipe, site, commit);
        if (refused.isPresent()) {
            status = refused.get(); // nothing changed; it completes once the policy allows it
            return;
        }
        cancel(inventory); // one batch done: the next re-matches (and a magical one needs a new start)
    }

    public CompoundTag save() {
        if (unsupported != null) {
            return unsupported.copy();
        }
        CompoundTag tag = new CompoundTag();
        tag.putInt("Schema", SCHEMA);
        if (recipeId != null) {
            tag.putString("Recipe", recipeId.toString());
            tag.putString("Fingerprint", fingerprint);
            tag.putIntArray("Reserved", reservation);
            tag.putInt("Progress", progress);
        }
        policy.save(tag);
        return tag;
    }

    /** Restores a saved job; it is revalidated against the loaded recipes on the next tick. */
    public void load(CompoundTag tag) {
        recipe = null;
        recipeId = null;
        fingerprint = "";
        reservation = null;
        held = false;
        progress = 0;
        seenRevision = -1;
        seenGeneration = -1;
        unsupported = null;
        policy.reset();
        if (tag.isEmpty()) {
            return;
        }
        int schema = tag.getInt("Schema");
        if (schema != LEGACY_SCHEMA && schema != SCHEMA) {
            unsupported = tag.copy();
            return;
        }
        if (tag.contains("Recipe")) {
            recipeId = ResourceLocation.tryParse(tag.getString("Recipe"));
            fingerprint = tag.getString("Fingerprint");
            reservation = tag.getIntArray("Reserved");
            progress = tag.getInt("Progress");
        }
        if (schema == SCHEMA) {
            policy.load(tag);
        }
    }
}
