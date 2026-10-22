package io.github.dirtyredz.herbalwitch.apothecary.stations.grimoire;

import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.ProcessingRecipeBindings;

import io.github.dirtyredz.herbalwitch.platform.recipe.RecipeIngredients;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item.ProductTooltips;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContentSource;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import io.github.dirtyredz.herbalwitch.grimoire.content.reference.HerbForms;
import io.github.dirtyredz.herbalwitch.apothecary.stations.grimoire.StationEntries;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.ProcessingCategories;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRecipeDescription;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Projects every live processing recipe of the four supported types (from any namespace) into a
 * recipe entry, keyed by the recipe's own id. Nothing is copied: the entry keeps the recipe, and
 * its page reads {@link ProcessRecipe#description()} for ingredients, counts, base, package, result,
 * returns, time and heat. Links follow the preparation chain: the station, the herbs among the
 * ingredients and the recipes that make each ingredient.
 */
public final class StationRecipeEntries implements GrimoireContentSource {
    public static final String HINT = "grimoire." + HerbalWitch.MOD_ID + ".hint.recipe";
    private static volatile Set<Item> products;

    public static EntryKey key(ResourceLocation recipe) {
        return EntryKey.of(EntryKey.Kind.RECIPE, recipe.toString());
    }

    /** Every supported recipe: category by category in station order, each by id. */
    public static List<ProcessRecipe> recipes(RecipeManager manager) {
        List<ProcessRecipe> out = new ArrayList<>();
        for (ProcessingCategories.Category category : ProcessingCategories.ALL) {
            manager.getAllRecipesFor(ProcessingRecipeBindings.type(category)).stream()
                    .sorted(Comparator.comparing(r -> r.getId().toString()))
                    .forEach(out::add);
        }
        return out;
    }

    @Override
    public void contribute(GrimoireContent.Inputs inputs, Consumer<GrimoireEntry> out) {
        RecipeManager manager = inputs.recipes();
        for (ProcessRecipe recipe : recipes(manager)) {
            ProcessingCategories.Category category = ProcessingRecipeBindings.byType(recipe.getType()).orElseThrow();
            ProcessRecipeDescription d = recipe.description();
            ItemStack result = d.result();
            List<Component> body = new ArrayList<>();
            if (isProduct(result.getItem())) {
                body.add(Component.translatable(ProductTooltips.key(result.getItem())));
            }
            List<EntryKey> links = new ArrayList<>();
            links.add(StationEntries.key(category.station()));
            for (Ingredient ingredient : RecipeIngredients.allIngredients(d)) {
                for (ItemStack stack : ingredient.getItems()) {
                    HerbForms.herbOf(stack.getItem()).ifPresent(h -> links.add(HerbForms.key(h)));
                }
                producing(manager, ingredient).stream().filter(k -> !k.equals(key(recipe.getId()))).forEach(links::add);
            }
            out.accept(new GrimoireEntry(key(recipe.getId()), category.id(), result.getHoverName(), result, body, links,
                    Optional.of(Component.translatable(HINT)), false, Optional.of(recipe)));
        }
    }

    /** Keys of the recipes whose result the ingredient accepts. */
    public static List<EntryKey> producing(RecipeManager manager, Ingredient ingredient) {
        return recipes(manager).stream().filter(r -> ingredient.test(r.description().result()))
                .map(r -> key(r.getId())).toList();
    }

    /** Keys of the recipes that take any of the stacks as an ingredient, base or package. */
    public static List<EntryKey> usingAny(RecipeManager manager, List<ItemStack> stacks) {
        Set<EntryKey> keys = new LinkedHashSet<>();
        for (ProcessRecipe recipe : recipes(manager)) {
            if (RecipeIngredients.allIngredients(recipe.description()).stream().anyMatch(i -> stacks.stream().anyMatch(i))) {
                keys.add(key(recipe.getId()));
            }
        }
        return List.copyOf(keys);
    }

    private static boolean isProduct(Item item) {
        Set<Item> set = products;
        if (set == null) {
            products = set = EverydayProductCatalog.ALL.stream().map(PreparationItems::product).collect(Collectors.toUnmodifiableSet());
        }
        return set.contains(item);
    }


}
