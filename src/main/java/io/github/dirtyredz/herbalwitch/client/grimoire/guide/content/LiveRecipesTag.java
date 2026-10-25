package io.github.dirtyredz.herbalwitch.client.grimoire.guide.content;

import guideme.compiler.PageCompiler;
import guideme.compiler.TagCompiler;
import guideme.compiler.tags.MdxAttrs;
import guideme.document.block.LytBlockContainer;
import guideme.document.block.LytParagraph;
import guideme.libs.mdast.mdx.model.MdxJsxFlowElement;
import io.github.dirtyredz.herbalwitch.platform.recipe.RecipeView;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/** Resolves each card from the connected world's recipe manager, never from bundled recipe JSON. */
public final class LiveRecipesTag implements TagCompiler {
    @Override
    public Set<String> getTagNames() { return Set.of("HerbalRecipe"); }

    @Override
    public void compileBlockContext(PageCompiler compiler, LytBlockContainer parent, MdxJsxFlowElement element) {
        ResourceLocation id = MdxAttrs.getRequiredId(compiler, parent, element, "id");
        if (id == null) return;
        var level = Minecraft.getInstance().level;
        var recipe = find(level == null ? null : level.getRecipeManager(), id);
        if (recipe.isEmpty()) {
            parent.append(LytParagraph.of("Recipe unavailable on this server: " + id));
            return;
        }
        parent.append(ProcessingRecipeRenderer.render(compiler, recipe.get()));
    }

    static Optional<RecipeView> find(RecipeManager manager, ResourceLocation id) {
        return find(manager == null ? key -> Optional.empty() : manager::byKey, id);
    }

    static Optional<RecipeView> find(Function<ResourceLocation, ? extends Optional<?>> lookup, ResourceLocation id) {
        return lookup.apply(id)
                .filter(RecipeView.class::isInstance).map(RecipeView.class::cast);
    }
}
