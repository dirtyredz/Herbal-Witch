package io.github.dirtyredz.herbalwitch.data.server.stations;

import io.github.dirtyredz.herbalwitch.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationSpec;
import io.github.dirtyredz.herbalwitch.stations.registry.StationItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.data.server.recipes.HerbalWitchRecipeProvider.has;

/**
 * Station assembly, each yielding one, shaped as the station's silhouette, with acquisition
 * advancements: rack (4 sticks, 2 string, 2 wooden slabs), mortar (3 smooth stone, flint, stick),
 * kettle (5 iron, 1 copper), hearth (6 bricks, campfire), infusion pot (4 bricks, 2 iron, glass bottle).
 */
public final class StationCraftingProvider {
    private StationCraftingProvider() {
    }

    private static ShapedRecipeBuilder shaped(StationSpec spec) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, StationItems.item(spec));
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        shaped(StationCatalog.DRYING_RACK).pattern("TST").pattern("s s").pattern("TST")
                .define('T', Tags.Items.RODS_WOODEN).define('S', ItemTags.WOODEN_SLABS).define('s', Tags.Items.STRING)
                .unlockedBy("has_string", has(Items.STRING)).save(writer);
        shaped(StationCatalog.MORTAR).pattern(" T ").pattern("SFS").pattern(" S ")
                .define('T', Tags.Items.RODS_WOODEN).define('F', Items.FLINT).define('S', Items.SMOOTH_STONE)
                .unlockedBy("has_flint", has(Items.FLINT)).save(writer);
        shaped(StationCatalog.KETTLE).pattern("I C").pattern("III").pattern(" I ")
                .define('I', Tags.Items.INGOTS_IRON).define('C', Tags.Items.INGOTS_COPPER)
                .unlockedBy("has_copper", has(Items.COPPER_INGOT)).save(writer);
        shaped(StationCatalog.HEARTH).pattern("B B").pattern("BCB").pattern("B B")
                .define('B', Items.BRICK).define('C', Items.CAMPFIRE)
                .unlockedBy("has_campfire", has(Items.CAMPFIRE)).save(writer);
        shaped(StationCatalog.INFUSION_POT).pattern("IGI").pattern("B B").pattern("B B")
                .define('I', Tags.Items.INGOTS_IRON).define('G', Items.GLASS_BOTTLE).define('B', Items.BRICK)
                .unlockedBy("has_brick", has(Items.BRICK)).save(writer);
    }
}
