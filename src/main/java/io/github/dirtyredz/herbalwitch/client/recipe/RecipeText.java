package io.github.dirtyredz.herbalwitch.client.recipe;

import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.ProcessingCategories;
import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRecipeDescription;
import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRequirements;
import io.github.dirtyredz.herbalwitch.platform.time.TickTime;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** Shared reader/viewer presentation. Current server rules are supplied by the caller. */
public final class RecipeText {
    private RecipeText() { }

    public static List<ItemStack> stacks(ProcessRecipeDescription.Counted counted) {
        return stacks(counted.ingredient(), counted.count());
    }

    public static List<ItemStack> stacks(Ingredient ingredient, int count) {
        return Arrays.stream(ingredient.getItems()).map(stack -> stack.copyWithCount(count)).toList();
    }

    public static Optional<Component> requirements(ProcessRequirements requirements, int processingPercent) {
        return requirements.magical() ? Optional.of(Component.translatable(ProcessingCategories.REQUIREMENTS_KEY,
                requirements.effectiveEnergy(processingPercent))) : Optional.empty();
    }

    public static Component footer(int duration, boolean heated) {
        return Component.translatable(ProcessingCategories.footerKey(heated), TickTime.fractionalSeconds(duration));
    }
}
