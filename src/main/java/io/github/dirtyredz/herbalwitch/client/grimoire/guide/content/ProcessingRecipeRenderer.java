package io.github.dirtyredz.herbalwitch.client.grimoire.guide.content;

import guideme.compiler.PageCompiler;
import guideme.document.block.LytParagraph;
import guideme.document.block.LytHBox;
import guideme.document.block.LytSlot;
import guideme.document.block.LytVBox;
import guideme.document.flow.LytFlowLink;
import guideme.document.flow.LytFlowText;
import guideme.indices.ItemIndex;
import io.github.dirtyredz.herbalwitch.client.recipe.RecipeText;
import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRecipeDescription;
import io.github.dirtyredz.herbalwitch.platform.recipe.RecipeView;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/** Counted alternatives, base, packaging, returns and result semantics retain their feature-owned facts. */
public final class ProcessingRecipeRenderer {
    private ProcessingRecipeRenderer() { }

    public static LytVBox render(PageCompiler compiler, RecipeView recipe) {
        var card = new LytVBox();
        card.setGap(3);
        var facts = recipe.description();
        card.append(LytParagraph.of(facts.result().getHoverName().getString()));
        for (var ingredient : facts.ingredients()) counted(card, compiler, "Ingredient", ingredient);
        facts.base().ifPresent(value -> counted(card, compiler, "Base liquid", value));
        facts.packaging().ifPresent(value -> counted(card, compiler, "Packaging", value));
        stack(card, compiler, "Result", facts.result());
        for (var returned : facts.returns()) stack(card, compiler, "Returned container", returned);
        card.append(LytParagraph.of(RecipeText.footer(facts.duration(), facts.heated()).getString()));
        if (facts.requirements().magical()) {
            card.append(LytParagraph.of("Requires witchcraft; energy cost is server configured. See the station before starting."));
        }
        recipe.resultNote().ifPresent(note -> card.append(LytParagraph.of(note.getString())));
        return card;
    }

    private static void counted(LytVBox card, PageCompiler compiler, String role, ProcessRecipeDescription.Counted ingredient) {
        var alternatives = RecipeText.stacks(ingredient);
        if (alternatives.isEmpty()) card.append(LytParagraph.of(role + ": unavailable ingredient"));
        for (int i = 0; i < alternatives.size(); i++) {
            stack(card, compiler, i == 0 ? role : "or", alternatives.get(i));
        }
    }

    private static void stack(LytVBox card, PageCompiler compiler, String role, ItemStack stack) {
        var row = new LytHBox();
        row.setGap(4);
        row.append(new LytSlot(stack));
        var line = new LytParagraph();
        line.append(LytFlowText.of(role + ": " + stack.getCount() + " × "));
        var route = compiler.getIndex(ItemIndex.class).get(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        if (route != null) {
            var link = new LytFlowLink();
            link.setPageLink(route);
            link.appendText(stack.getHoverName().getString());
            line.append(link);
        } else {
            line.append(LytFlowText.of(stack.getHoverName().getString()));
        }
        row.append(line);
        card.append(row);
    }
}
