package io.github.dirtyredz.herbalwitch.client.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.dirtyredz.herbalwitch.client.compat.ritual.RitualCardLayout;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** One altar action as an EMI recipe, laid out by {@link RitualCardLayout}; inputs only, its result in words. */
public class EmiRitualRecipe extends BasicEmiRecipe {
    private final AltarActionSpec action;

    public EmiRitualRecipe(EmiRecipeCategory category, AltarActionSpec action) {
        super(category, ResourceLocation.fromNamespaceAndPath(RitualCardLayout.CATEGORY.getNamespace(), "/" + action.identity()),
                RitualCardLayout.WIDTH, RitualCardLayout.HEIGHT);
        this.action = action;
        RitualCardLayout.offering(action).forEach(s -> inputs.add(EmiStack.of(s)));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        List<ItemStack> offering = RitualCardLayout.offering(action);
        for (int i = 0; i < offering.size(); i++) {
            widgets.addSlot(EmiStack.of(offering.get(i)), RitualCardLayout.slotX(i), 0);
        }
        int y = offering.isEmpty() ? 0 : RitualCardLayout.TEXT_Y;
        for (Component line : RitualCardLayout.lines(action)) {
            widgets.addText(line, 0, y, 0x404040, false);
            y += 10;
        }
    }
}
