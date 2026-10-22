package io.github.dirtyredz.herbalwitch.stations.process.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Optional;

/**
 * Everything a viewer or the station needs to know about one processing recipe, immutable: counted
 * ingredients (shapeless, each in its own slot), the optional base liquid and packaging slots, the
 * result, containers returned on completion, the duration in ticks, whether heat is required and
 * what the recipe asks of whoever runs it ({@link ProcessRequirements}: a witch and energy, or
 * nothing). Station UIs, the grimoire, EMI and JEI all read this; none recompute recipe logic.
 */
public record ProcessRecipeDescription(List<Counted> ingredients, Optional<Counted> base, Optional<Counted> packaging,
                                       ItemStack result, List<ItemStack> returns, int duration, boolean heated,
                                       ProcessRequirements requirements) {
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

    /** An ordinary recipe (no witch, no energy). */
    public ProcessRecipeDescription(List<Counted> ingredients, Optional<Counted> base, Optional<Counted> packaging, ItemStack result,
                                    List<ItemStack> returns, int duration, boolean heated) {
        this(ingredients, base, packaging, result, returns, duration, heated, ProcessRequirements.NONE);
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
