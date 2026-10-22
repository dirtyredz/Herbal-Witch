package io.github.dirtyredz.herbalwitch.grimoire.reading.ingredients;

import io.github.dirtyredz.herbalwitch.stations.process.recipe.ProcessRecipeDescription;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns a recipe's requirements and a carried inventory into the inputs of
 * {@link MissingIngredients}, using Minecraft's own matching: {@link Ingredient#test} on real
 * stacks (so a water bottle and another potion differ by NBT), and stack compatibility
 * ({@link ItemStack#isSameItemSameTags}) to merge split stacks into variants. Returned containers
 * are outputs and never count. Nothing here mutates or reserves anything.
 */
public final class IngredientRequirements {
    /** Where a requirement goes at the station. */
    public enum Role {
        INGREDIENT,
        BASE,
        PACKAGING
    }

    /** One requirement, in page order: the counted ingredients, then base, then package. */
    public record Requirement(Role role, Ingredient ingredient, int count) {
    }

    /** What the page shows for a requirement. */
    public record Count(int required, int available) {
    }

    private IngredientRequirements() {
    }

    public static List<Requirement> of(ProcessRecipeDescription description) {
        List<Requirement> out = new ArrayList<>();
        description.ingredients().forEach(c -> out.add(new Requirement(Role.INGREDIENT, c.ingredient(), c.count())));
        description.base().ifPresent(c -> out.add(new Requirement(Role.BASE, c.ingredient(), c.count())));
        description.packaging().ifPresent(c -> out.add(new Requirement(Role.PACKAGING, c.ingredient(), c.count())));
        return out;
    }

    /** Merges stack-compatible stacks: one representative per variant and its total count. */
    public static List<ItemStack> variants(List<ItemStack> carried) {
        List<ItemStack> variants = new ArrayList<>();
        for (ItemStack stack : carried) {
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
        return variants;
    }

    /** Required and available per requirement, against one batch of the carried inventory. */
    public static List<Count> count(ProcessRecipeDescription description, List<ItemStack> carried) {
        List<Requirement> requirements = of(description);
        List<ItemStack> variants = variants(carried);
        int[] required = new int[requirements.size()];
        boolean[][] matches = new boolean[requirements.size()][variants.size()];
        int[] counts = variants.stream().mapToInt(ItemStack::getCount).toArray();
        for (int r = 0; r < requirements.size(); r++) {
            required[r] = requirements.get(r).count();
            for (int v = 0; v < variants.size(); v++) {
                matches[r][v] = requirements.get(r).ingredient().test(variants.get(v));
            }
        }
        MissingIngredients.Allocation allocation = MissingIngredients.allocate(required, matches, counts);
        List<Count> out = new ArrayList<>();
        for (int r = 0; r < required.length; r++) {
            out.add(new Count(required[r], allocation.available(r)));
        }
        return out;
    }
}
