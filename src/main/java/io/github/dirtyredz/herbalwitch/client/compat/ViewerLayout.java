package io.github.dirtyredz.herbalwitch.client.compat;

import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.apothecary.stations.description.ProcessingCategories;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipeDescription;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * The one recipe-card layout EMI and JEI share, read from {@link ProcessRecipeDescription}: counted
 * ingredients on the top row, base liquid and package below them, an arrow, the result and the
 * returned containers, a line with duration and heat, and for a witch recipe a line with its
 * effective energy cost (Milestone 7). Neither viewer computes recipe logic; the
 * categories and their stations come from {@link ProcessingCategories}, shared with the grimoire.
 */
public final class ViewerLayout {
    public static final int WIDTH = 132;
    public static final int HEIGHT = 60;
    public static final int SLOT = 18;
    public static final int ARROW_X = 58;
    public static final int ARROW_Y = 10;
    public static final int RESULT_X = 88;
    public static final int RESULT_Y = 10;
    public static final int TEXT_Y = 40;
    /** The requirements line under the footer (witch recipes). */
    public static final int REQUIREMENTS_Y = 50;

    private ViewerLayout() {
    }

    public static int ingredientX(int index) {
        return index * SLOT;
    }

    public static final int INGREDIENT_Y = 0;
    public static final int BASE_X = 0;
    public static final int PACKAGING_X = SLOT;
    public static final int LOWER_Y = SLOT + 2;

    public static int returnX(int index) {
        return RESULT_X + SLOT + 4 + index * SLOT;
    }

    /** Every stack an ingredient accepts, at its required count. */
    public static List<ItemStack> stacks(ProcessRecipeDescription.Counted counted) {
        return Arrays.stream(counted.ingredient().getItems()).map(stack -> stack.copyWithCount(counted.count())).toList();
    }

    /** "Witch: 10 energy" for a witch recipe at the server's current multiplier, or empty for an ordinary one. */
    public static java.util.Optional<Component> requirements(ProcessRecipeDescription description) {
        if (!description.requirements().magical()) {
            return java.util.Optional.empty();
        }
        int energy = description.requirements().effectiveEnergy(WitchRules.current().workings().processingPercent());
        return java.util.Optional.of(Component.translatable(ProcessingCategories.REQUIREMENTS_KEY, energy));
    }

    /** "10 s" or "10 s, needs heat". */
    public static Component footer(ProcessRecipeDescription description) {
        return Component.translatable(ProcessingCategories.footerKey(description.heated()),
                ProcessingCategories.seconds(description.duration()));
    }
}
