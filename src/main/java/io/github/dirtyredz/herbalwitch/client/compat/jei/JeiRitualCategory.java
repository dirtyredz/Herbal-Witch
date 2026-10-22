package io.github.dirtyredz.herbalwitch.client.compat.jei;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.client.compat.ritual.RitualCardLayout;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
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

/** The altar's actions as one JEI category, laid out by {@link RitualCardLayout}; inputs only, results in words. */
public class JeiRitualCategory implements IRecipeCategory<AltarActionSpec> {
    public static final RecipeType<AltarActionSpec> TYPE = RecipeType.create(HerbalWitch.MOD_ID, "ritual", AltarActionSpec.class);
    private final IDrawable icon;

    public JeiRitualCategory(IGuiHelper gui, ItemStack altar) {
        this.icon = gui.createDrawableItemStack(altar);
    }

    @Override
    public RecipeType<AltarActionSpec> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(RitualCardLayout.TITLE);
    }

    @Override
    public int getWidth() {
        return RitualCardLayout.WIDTH;
    }

    @Override
    public int getHeight() {
        return RitualCardLayout.HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AltarActionSpec action, IFocusGroup focuses) {
        List<ItemStack> offering = RitualCardLayout.offering(action);
        for (int i = 0; i < offering.size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, RitualCardLayout.slotX(i) + 1, 1).addItemStack(offering.get(i));
        }
    }

    @Override
    public void draw(AltarActionSpec action, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        int y = RitualCardLayout.offering(action).isEmpty() ? 0 : RitualCardLayout.TEXT_Y;
        for (Component line : RitualCardLayout.lines(action)) {
            graphics.drawString(Minecraft.getInstance().font, line, 0, y, 0x404040, false);
            y += 10;
        }
    }
}
