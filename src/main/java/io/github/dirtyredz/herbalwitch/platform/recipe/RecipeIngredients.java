package io.github.dirtyredz.herbalwitch.platform.recipe;

import net.minecraft.world.item.crafting.Ingredient;
import java.util.ArrayList;
import java.util.List;

/** Ordered ingredient roles shared by projection, discovery and research. */
public final class RecipeIngredients {
    private RecipeIngredients() { }

    /** Primary ingredients, then the base and the package. */
    public static List<Ingredient> allIngredients(ProcessRecipeDescription d) {
        List<Ingredient> all = new ArrayList<>(primaries(d));
        all.addAll(secondaries(d));
        return all;
    }

    /** The counted station ingredients (not the base liquid or the package). */
    public static List<Ingredient> primaries(ProcessRecipeDescription d) {
        return d.ingredients().stream().map(ProcessRecipeDescription.Counted::ingredient).toList();
    }

    /** The base liquid and the package, when the recipe names them. */
    public static List<Ingredient> secondaries(ProcessRecipeDescription d) {
        List<Ingredient> out = new ArrayList<>();
        d.base().ifPresent(c -> out.add(c.ingredient()));
        d.packaging().ifPresent(c -> out.add(c.ingredient()));
        return out;
    }

}
