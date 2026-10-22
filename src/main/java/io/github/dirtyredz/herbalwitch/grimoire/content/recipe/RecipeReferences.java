package io.github.dirtyredz.herbalwitch.grimoire.content.recipe;

import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.platform.recipe.RecipeIngredients;
import io.github.dirtyredz.herbalwitch.platform.recipe.RecipeView;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/** Ordered read-only queries; feature adapters supply their live recipe views. */
public final class RecipeReferences {
    private static final List<Function<RecipeManager, ? extends List<? extends RecipeView>>> PROVIDERS = new ArrayList<>();

    private RecipeReferences() { }

    public static void register(Function<RecipeManager, ? extends List<? extends RecipeView>> provider) {
        PROVIDERS.add(provider);
    }

    public static EntryKey key(ResourceLocation recipe) {
        return EntryKey.of(EntryKey.Kind.RECIPE, recipe.toString());
    }

    private static List<RecipeView> recipes(RecipeManager manager) {
        List<RecipeView> out = new ArrayList<>();
        PROVIDERS.forEach(provider -> out.addAll(provider.apply(manager)));
        return out;
    }

    public static List<EntryKey> producing(RecipeManager manager, Ingredient ingredient) {
        return recipes(manager).stream().filter(r -> ingredient.test(r.description().result()))
                .map(r -> key(r.getId())).toList();
    }

    public static List<EntryKey> usingAny(RecipeManager manager, List<ItemStack> stacks) {
        Set<EntryKey> keys = new LinkedHashSet<>();
        for (RecipeView recipe : recipes(manager)) {
            if (RecipeIngredients.allIngredients(recipe.description()).stream().anyMatch(i -> stacks.stream().anyMatch(i))) {
                keys.add(key(recipe.getId()));
            }
        }
        return List.copyOf(keys);
    }
}
