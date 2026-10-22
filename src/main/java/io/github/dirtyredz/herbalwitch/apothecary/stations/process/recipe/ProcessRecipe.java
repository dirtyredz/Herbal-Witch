package io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe;

import io.github.dirtyredz.herbalwitch.platform.recipe.RecipeView;

import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRecipeDescription;

import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessLayout;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessMatcher;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

/**
 * Common recipe behaviour of the four processing types (drying, grinding, steeping, infusion): an
 * immutable {@link ProcessRecipeDescription} and reservation matching against a station's slots.
 * Matching never mutates anything; the station consumes on completion. Each type has its own
 * subclass, recipe type and serializer.
 */
public abstract class ProcessRecipe implements Recipe<Container>, RecipeView {
    private final ResourceLocation id;
    private final ProcessRecipeDescription description;
    private final String fingerprint;

    protected ProcessRecipe(ResourceLocation id, ProcessRecipeDescription description) {
        this.id = id;
        this.description = description;
        this.fingerprint = ProcessRecipeJson.write(description).toString();
    }

    public ProcessRecipeDescription description() {
        return description;
    }

    /** Identity of the recipe's content: a reloaded recipe with another fingerprint cancels a running job. */
    public String fingerprint() {
        return fingerprint;
    }

    /**
     * The per-slot reservation that runs this recipe on the inventory, or null if it does not match.
     * Shapeless and exact: every ingredient takes its own input slot holding at least its count, and
     * every occupied input slot is used; the base and packaging slots hold exactly what the recipe
     * names (or nothing when it names nothing).
     */
    public int[] reserve(ProcessInventory inventory, ProcessLayout layout) {
        int[] reservation = new int[inventory.getSlots()];
        int[] inputs = layout.inputs();
        int n = description.ingredients().size();
        boolean[][] matches = new boolean[n][inputs.length];
        int[] required = new int[n];
        boolean[] occupied = new boolean[inputs.length];
        for (int s = 0; s < inputs.length; s++) {
            occupied[s] = !inventory.getStackInSlot(inputs[s]).isEmpty();
        }
        for (int i = 0; i < n; i++) {
            ProcessRecipeDescription.Counted counted = description.ingredients().get(i);
            required[i] = counted.count();
            for (int s = 0; s < inputs.length; s++) {
                matches[i][s] = counted.test(inventory.getStackInSlot(inputs[s]));
            }
        }
        int[] assigned = ProcessMatcher.assign(matches, occupied);
        if (assigned == null) {
            return null;
        }
        for (int i = 0; i < n; i++) {
            reservation[inputs[assigned[i]]] = required[i];
        }
        if (!reserveSingle(inventory, layout.base(), description.base().orElse(null), reservation)
                || !reserveSingle(inventory, layout.packaging(), description.packaging().orElse(null), reservation)) {
            return null;
        }
        return reservation;
    }

    private static boolean reserveSingle(ProcessInventory inventory, int slot, ProcessRecipeDescription.Counted counted,
                                         int[] reservation) {
        if (counted == null) {
            return slot < 0 || inventory.getStackInSlot(slot).isEmpty();
        }
        if (slot < 0 || !counted.test(inventory.getStackInSlot(slot))) {
            return false;
        }
        reservation[slot] = counted.count();
        return true;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false; // stations match through reserve(); vanilla crafting never runs these
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        return description.result();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return description.result();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> all = NonNullList.create();
        description.ingredients().forEach(c -> all.add(c.ingredient()));
        description.base().ifPresent(c -> all.add(c.ingredient()));
        description.packaging().ifPresent(c -> all.add(c.ingredient()));
        return all;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    /** Not a recipe-book recipe: viewers show it through their own categories. */
    @Override
    public boolean isSpecial() {
        return true;
    }
}
