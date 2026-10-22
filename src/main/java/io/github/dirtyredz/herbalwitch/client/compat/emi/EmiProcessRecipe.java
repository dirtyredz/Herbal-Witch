package io.github.dirtyredz.herbalwitch.client.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.dirtyredz.herbalwitch.client.compat.ViewerLayout;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRecipeDescription;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** One processing recipe as an EMI recipe, laid out by {@link ViewerLayout}. */
public class EmiProcessRecipe extends BasicEmiRecipe {
    private final ProcessRecipeDescription description;

    public EmiProcessRecipe(EmiRecipeCategory category, ProcessRecipe recipe) {
        super(category, recipe.getId(), ViewerLayout.WIDTH, ViewerLayout.HEIGHT);
        this.description = recipe.description();
        description.ingredients().forEach(c -> inputs.add(ingredient(c)));
        description.base().ifPresent(c -> inputs.add(ingredient(c)));
        description.packaging().ifPresent(c -> inputs.add(ingredient(c)));
        outputs.add(EmiStack.of(description.result()));
        description.returns().forEach(r -> outputs.add(EmiStack.of(r)));
    }

    private static EmiIngredient ingredient(ProcessRecipeDescription.Counted counted) {
        return EmiIngredient.of(counted.ingredient(), counted.count());
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        List<ProcessRecipeDescription.Counted> ingredients = description.ingredients();
        for (int i = 0; i < ingredients.size(); i++) {
            widgets.addSlot(ingredient(ingredients.get(i)), ViewerLayout.ingredientX(i), ViewerLayout.INGREDIENT_Y);
        }
        description.base().ifPresent(c -> widgets.addSlot(ingredient(c), ViewerLayout.BASE_X, ViewerLayout.LOWER_Y));
        description.packaging().ifPresent(c -> widgets.addSlot(ingredient(c), ViewerLayout.PACKAGING_X, ViewerLayout.LOWER_Y));
        widgets.addFillingArrow(ViewerLayout.ARROW_X, ViewerLayout.ARROW_Y, description.duration() * 50);
        if (description.heated()) {
            widgets.addTexture(EmiTexture.FULL_FLAME, ViewerLayout.ARROW_X + 5, ViewerLayout.ARROW_Y + 18);
        }
        widgets.addSlot(EmiStack.of(description.result()), ViewerLayout.RESULT_X, ViewerLayout.RESULT_Y).recipeContext(this);
        List<ItemStack> returns = description.returns();
        for (int i = 0; i < returns.size(); i++) {
            widgets.addSlot(EmiStack.of(returns.get(i)), ViewerLayout.returnX(i), ViewerLayout.RESULT_Y).recipeContext(this);
        }
        widgets.addText(ViewerLayout.footer(description), 0, ViewerLayout.TEXT_Y, 0x404040, false);
        ViewerLayout.requirements(description).ifPresent(r -> widgets.addText(r, 0, ViewerLayout.REQUIREMENTS_Y, 0x6A2A5A, false));
    }
}
