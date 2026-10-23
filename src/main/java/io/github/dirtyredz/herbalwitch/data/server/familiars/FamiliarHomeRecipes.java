package io.github.dirtyredz.herbalwitch.data.server.familiars;

import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarHomeKind;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarHomes;
import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchBlockTagsProvider;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import java.util.function.Consumer;
import static io.github.dirtyredz.herbalwitch.data.server.recipes.HerbalWitchRecipeProvider.has;

public final class FamiliarHomeRecipes {
    private FamiliarHomeRecipes() {}
    public static void addTo(Consumer<FinishedRecipe> out) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, FamiliarHomes.get(FamiliarHomeKind.BED).item().get())
                .requires(Items.DARK_OAK_PLANKS, 3).requires(Items.GREEN_WOOL, 2).requires(Items.STRING, 2).unlockedBy("has_string", has(Items.STRING)).save(out);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, FamiliarHomes.get(FamiliarHomeKind.NEST).item().get())
                .requires(Items.STICK, 4).requires(Items.WHEAT, 2).requires(Items.STRING, 2).unlockedBy("has_string", has(Items.STRING)).save(out);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, FamiliarHomes.get(FamiliarHomeKind.PERCH).item().get())
                .requires(Items.DARK_OAK_SLAB, 2).requires(Items.STICK, 2).requires(Items.IRON_NUGGET).unlockedBy("has_stick", has(Items.STICK)).save(out);
    }
    public static void blocks(HerbalWitchBlockTagsProvider.Sink sink) { FamiliarHomes.ALL.forEach(e -> sink.tag(BlockTags.MINEABLE_WITH_AXE).add(e.block().get())); }
}
