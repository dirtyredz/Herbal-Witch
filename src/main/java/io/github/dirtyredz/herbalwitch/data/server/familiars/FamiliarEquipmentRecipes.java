package io.github.dirtyredz.herbalwitch.data.server.familiars;

import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarEquipmentCatalog;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarItems;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationRecipes;
import io.github.dirtyredz.herbalwitch.data.server.stations.recipes.ProcessingRecipeBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import java.util.function.Consumer;

public final class FamiliarEquipmentRecipes {
    private FamiliarEquipmentRecipes() {}
    public static void addTo(Consumer<FinishedRecipe> writer) {
        for (var gear : FamiliarEquipmentCatalog.ALL)
            ProcessingRecipeBuilder.of(StationRecipes.CHARM_WORK_SERIALIZER.get(), 200).witch(5)
                    .input(gear.key().equals("bat_chime") ? Items.IRON_NUGGET : Items.LEATHER).input(Items.STRING, 2).input(BuiltInRegistries.ITEM.get(new ResourceLocation(gear.ingredient())))
                    .result(FamiliarItems.gear(gear.key())).save(writer, "processing/charm_work/" + gear.path());
    }
}
