package io.github.dirtyredz.herbalwitch.client.compat;

import io.github.dirtyredz.herbalwitch.stations.description.ProcessingCategories;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipeDescription;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * The one recipe-card layout EMI and JEI share, read from {@link ProcessRecipeDescription}: counted
 * ingredients on the top row, base liquid and package below them, an arrow, the result and the
 * returned containers, and a line with duration and heat. Neither viewer computes recipe logic; the
 * categories and their stations come from {@link ProcessingCategories}, shared with the grimoire.
 */
public final class ViewerLayout {
    public static final int WIDTH = 132;
    public static final int HEIGHT = 50;
    public static final int SLOT = 18;
    public static final int ARROW_X = 58;
    public static final int ARROW_Y = 10;
    public static final int RESULT_X = 88;
    public static final int RESULT_Y = 10;
    public static final int TEXT_Y = 40;

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

    /** "10 s" or "10 s, needs heat". */
    public static Component footer(ProcessRecipeDescription description) {
        return Component.translatable(ProcessingCategories.footerKey(description.heated()),
                ProcessingCategories.seconds(description.duration()));
    }
}
