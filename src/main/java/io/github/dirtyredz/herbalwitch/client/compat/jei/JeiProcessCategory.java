package io.github.dirtyredz.herbalwitch.client.compat.jei;

import io.github.dirtyredz.herbalwitch.client.compat.ViewerLayout;
import io.github.dirtyredz.herbalwitch.apothecary.stations.description.ProcessingCategories;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipeDescription;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** One processing type as a JEI category, laid out by {@link ViewerLayout}. */
public class JeiProcessCategory implements IRecipeCategory<ProcessRecipe> {
    private final RecipeType<ProcessRecipe> type;
    private final ProcessingCategories.Category category;
    private final IDrawable icon;

    public JeiProcessCategory(RecipeType<ProcessRecipe> type, ProcessingCategories.Category category, IGuiHelper gui, ItemStack station) {
        this.type = type;
        this.category = category;
        this.icon = gui.createDrawableItemStack(station);
    }

    @Override
    public RecipeType<ProcessRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(category.titleKey());
    }

    @Override
    public int getWidth() {
        return ViewerLayout.WIDTH;
    }

    @Override
    public int getHeight() {
        return ViewerLayout.HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ProcessRecipe recipe, IFocusGroup focuses) {
        ProcessRecipeDescription d = recipe.description();
        List<ProcessRecipeDescription.Counted> ingredients = d.ingredients();
        for (int i = 0; i < ingredients.size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, ViewerLayout.ingredientX(i) + 1, ViewerLayout.INGREDIENT_Y + 1)
                    .addItemStacks(ViewerLayout.stacks(ingredients.get(i)));
        }
        d.base().ifPresent(c -> builder.addSlot(RecipeIngredientRole.INPUT, ViewerLayout.BASE_X + 1, ViewerLayout.LOWER_Y + 1)
                .addItemStacks(ViewerLayout.stacks(c)));
        d.packaging().ifPresent(c -> builder.addSlot(RecipeIngredientRole.INPUT, ViewerLayout.PACKAGING_X + 1, ViewerLayout.LOWER_Y + 1)
                .addItemStacks(ViewerLayout.stacks(c)));
        builder.addSlot(RecipeIngredientRole.OUTPUT, ViewerLayout.RESULT_X + 1, ViewerLayout.RESULT_Y + 1).addItemStack(d.result());
        List<ItemStack> returns = d.returns();
        for (int i = 0; i < returns.size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, ViewerLayout.returnX(i) + 1, ViewerLayout.RESULT_Y + 1).addItemStack(returns.get(i));
        }
    }

    @Override
    public void draw(ProcessRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        graphics.drawString(Minecraft.getInstance().font, "->", ViewerLayout.ARROW_X + 6, ViewerLayout.ARROW_Y + 4, 0x404040, false);
        graphics.drawString(Minecraft.getInstance().font, ViewerLayout.footer(recipe.description()), 0, ViewerLayout.TEXT_Y, 0x404040, false);
        ViewerLayout.requirements(recipe.description()).ifPresent(r -> graphics.drawString(Minecraft.getInstance().font, r, 0,
                ViewerLayout.REQUIREMENTS_Y, 0x6A2A5A, false));
    }
}
