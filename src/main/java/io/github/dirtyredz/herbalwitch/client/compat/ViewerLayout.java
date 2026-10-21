package io.github.dirtyredz.herbalwitch.client.compat;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationSpec;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipeDescription;
import io.github.dirtyredz.herbalwitch.stations.registry.StationRecipes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * The one recipe-card layout EMI and JEI share, read from {@link ProcessRecipeDescription}: counted
 * ingredients on the top row, base liquid and package below them, an arrow, the result and the
 * returned containers, and a line with duration and heat. Neither viewer computes recipe logic.
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

    /** One viewer category: a recipe type, its id segment and its station (the catalyst). */
    public record Category(String id, Supplier<? extends RecipeType<? extends ProcessRecipe>> type, StationSpec station) {
        public String titleKey() {
            return "category." + HerbalWitch.MOD_ID + ".processing." + id;
        }
    }

    public static final List<Category> CATEGORIES = List.of(
            category("drying", StationRecipes.DRYING, StationCatalog.DRYING_RACK),
            category("grinding", StationRecipes.GRINDING, StationCatalog.MORTAR),
            category("steeping", StationRecipes.STEEPING, StationCatalog.KETTLE),
            category("infusion", StationRecipes.INFUSION, StationCatalog.INFUSION_POT));

    private ViewerLayout() {
    }

    private static Category category(String id, RegistryObject<? extends RecipeType<? extends ProcessRecipe>> type, StationSpec station) {
        return new Category(id, type, station);
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
        String seconds = String.valueOf(description.duration() / 20.0).replaceAll("\\.0$", "");
        return description.heated()
                ? Component.translatable("category." + HerbalWitch.MOD_ID + ".processing.heated", seconds)
                : Component.translatable("category." + HerbalWitch.MOD_ID + ".processing.time", seconds);
    }
}
