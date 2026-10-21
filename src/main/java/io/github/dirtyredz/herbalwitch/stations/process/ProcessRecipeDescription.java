package io.github.dirtyredz.herbalwitch.stations.process;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Optional;

/**
 * Everything a viewer or the station needs to know about one processing recipe, immutable: counted
 * ingredients (shapeless, each in its own slot), the optional base liquid and packaging slots, the
 * result, containers returned on completion, the duration in ticks and whether heat is required.
 * Station UIs, EMI and JEI all read this; none recompute recipe logic.
 */
public record ProcessRecipeDescription(List<Counted> ingredients, Optional<Counted> base, Optional<Counted> packaging,
                                       ItemStack result, List<ItemStack> returns, int duration, boolean heated) {
    /** An ingredient that must be present at least {@code count} times in one slot. */
    public record Counted(Ingredient ingredient, int count) {
        public boolean test(ItemStack stack) {
            return !stack.isEmpty() && ingredient.test(stack) && stack.getCount() >= count;
        }
    }

    public ProcessRecipeDescription {
        ingredients = List.copyOf(ingredients);
        returns = returns.stream().map(ItemStack::copy).toList();
        result = result.copy();
    }

    @Override
    public ItemStack result() {
        return result.copy();
    }

    @Override
    public List<ItemStack> returns() {
        return returns.stream().map(ItemStack::copy).toList();
    }
}
