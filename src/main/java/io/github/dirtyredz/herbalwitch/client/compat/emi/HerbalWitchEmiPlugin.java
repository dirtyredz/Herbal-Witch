package io.github.dirtyredz.herbalwitch.client.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import io.github.dirtyredz.herbalwitch.client.compat.ritual.RitualCardLayout;
import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualItems;
import io.github.dirtyredz.herbalwitch.stations.description.ProcessingCategories;
import io.github.dirtyredz.herbalwitch.stations.process.recipe.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.stations.registry.StationItems;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * EMI support: one category per processing type with its station as the workstation, from
 * {@link ProcessingCategories}, and the altar's rituals (altar and circle as workstations). Discovered by EMI only when EMI is installed; no
 * transfer handlers. Every recipe is shown whatever the player has discovered in the grimoire.
 */
@EmiEntrypoint
public class HerbalWitchEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        for (ProcessingCategories.Category category : ProcessingCategories.ALL) {
            EmiStack station = EmiStack.of(StationItems.item(category.station()));
            EmiRecipeCategory emi = new EmiRecipeCategory(category.typeId(), station);
            registry.addCategory(emi);
            registry.addWorkstation(emi, station);
            addRecipes(registry, emi, category.type().get());
        }
        EmiStack altar = EmiStack.of(RitualItems.ALTAR.get());
        EmiRecipeCategory rituals = new EmiRecipeCategory(RitualCardLayout.CATEGORY, altar);
        registry.addCategory(rituals);
        registry.addWorkstation(rituals, altar);
        registry.addWorkstation(rituals, EmiStack.of(RitualItems.CIRCLE.get()));
        RitualCardLayout.actions().forEach(a -> registry.addRecipe(new EmiRitualRecipe(rituals, a)));
    }

    private static <R extends ProcessRecipe> void addRecipes(EmiRegistry registry, EmiRecipeCategory emi, RecipeType<R> type) {
        for (R recipe : registry.getRecipeManager().getAllRecipesFor(type)) {
            registry.addRecipe(new EmiProcessRecipe(emi, recipe));
        }
    }
}
