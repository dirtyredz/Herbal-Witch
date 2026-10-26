package io.github.dirtyredz.herbalwitch.client.grimoire.guide.content;

import guideme.compiler.PageCompiler;
import guideme.compiler.TagCompiler;
import guideme.compiler.tags.MdxAttrs;
import guideme.document.block.LytBlockContainer;
import guideme.document.block.LytHBox;
import guideme.document.block.LytParagraph;
import guideme.document.block.LytVBox;
import guideme.libs.mdast.mdx.model.MdxJsxFlowElement;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** Live crafting grid with the same bounded tooltip slots as processing cards. */
public final class CraftingRecipesTag implements TagCompiler {
    @Override public Set<String> getTagNames() { return Set.of("HerbalCrafting"); }

    @Override
    public void compileBlockContext(PageCompiler compiler, LytBlockContainer parent, MdxJsxFlowElement element) {
        var id = MdxAttrs.getRequiredId(compiler, parent, element, "id");
        if (id == null) return;
        var level = Minecraft.getInstance().level;
        var recipes = level == null ? List.<CraftingRecipe>of() : level.getRecipeManager()
                .getAllRecipesFor(RecipeType.CRAFTING).stream()
                .filter(recipe -> BuiltInRegistries.ITEM.getKey(recipe.getResultItem(level.registryAccess()).getItem()).equals(id))
                .sorted(Comparator.comparing(recipe -> recipe.getId().toString())).toList();
        if (recipes.isEmpty()) {
            parent.append(LytParagraph.of("Recipe unavailable on this server: " + id));
            return;
        }
        for (var recipe : recipes) parent.append(render(compiler, recipe, level.registryAccess()));
    }

    private static LytVBox render(PageCompiler compiler, CraftingRecipe recipe, RegistryAccess registries) {
        var card = new LytVBox();
        card.setGap(3);
        card.append(LytParagraph.of(recipe instanceof ShapedRecipe ? "Crafting (shaped)" : "Crafting (shapeless)"));
        int width = recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : 3;
        int height = recipe instanceof ShapedRecipe shaped ? shaped.getHeight() : (recipe.getIngredients().size() + 2) / 3;
        var grid = new LytVBox();
        for (int y = 0; y < height; y++) {
            var row = new LytHBox();
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                row.append(new GuideRecipeSlot(compiler, index < recipe.getIngredients().size()
                        ? recipe.getIngredients().get(index) : Ingredient.EMPTY));
            }
            grid.append(row);
        }
        var row = new LytHBox();
        row.setGap(6);
        row.append(grid);
        row.append(LytParagraph.of("→"));
        row.append(new GuideRecipeSlot(compiler, recipe.getResultItem(registries)));
        card.append(row);
        return card;
    }
}
